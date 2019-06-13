package io.monetizr.monetizrsdk

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentProvider
import android.content.Context
import io.monetizr.monetizrsdk.ApplicationProvider.appStart
import io.monetizr.monetizrsdk.ApplicationProvider.sessionStart
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Taken advice and implementation from https://github.com/florent37/ApplicationProvider
 *
 * This approach allows not to initialize component, but get context from application on its launch.
 * It allows for easier implementation without application override
 *
 * Application provider exposes three variables: application context, sessionStart datetime and apStart milliseconds
 */
object ApplicationProvider {
    internal val applicationListeners = ConcurrentLinkedQueue<(Application) -> Unit>()

    @JvmStatic
    fun listen(listener: (Application) -> Unit) {
        val app = privateAppliation
        if(app != null){
            listener(app)
        } else {
            applicationListeners.add(listener)
        }
    }

    /**
     * Application context taken from hosting application
     */
    @JvmStatic
    val application: Application?
        get() {
            return privateAppliation
        }

    /**
     * Milliseconds when system was started
     */
    @JvmStatic
    var appStart: Long = 0

    /**
     * Session started datetime
     */
    @JvmStatic
    var sessionStart: String = ""
}

@SuppressLint("StaticFieldLeak")
private var privateAppliation: Application? = null
    private set(value) {
        field = value
        if(value != null){
            ApplicationProvider.applicationListeners.forEach {
                it.invoke(value)
            }
        }
    }

val application: Application?
    get() = privateAppliation ?: initAndGetAppCtxWithReflection()

/**
 * This methods is only run if appCtx is accessed while AppCtxInitProvider hasn't been
 * initialized. This may happen in case you're accessing it outside the default process, or in case
 * you are accessing it in a [ContentProvider] with a higher priority than AppCtxInitProvider
 * (900 at the time of writing).
 *
 * //from https://github.com/LouisCAD/Splitties/tree/master/appctx
 */
@SuppressLint("PrivateApi")
private fun initAndGetAppCtxWithReflection(): Application? {
    // Fallback, should only run once per non default process.
    val activityThread = Class.forName("android.app.ActivityThread")
    val ctx = activityThread.getDeclaredMethod("currentApplication").invoke(null) as? Context
    if (ctx is Application) {
        privateAppliation = ctx
        return ctx
    }
    return null
}

/**
 * Initial application values
 */
class AppContextProvider : EmptyProvider() {
    override fun onCreate(): Boolean {

        // Setting initial application values when host application starts
        val ctx = context
        appStart = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        val date = cal.time
        val dateFormat = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.getDefault()
        )
        dateFormat.timeZone = TimeZone.getDefault()
        sessionStart = dateFormat.format(date)


        if (ctx is Application) {
            privateAppliation = ctx
        }

        // Initialize settings values, gain access to configuration settings
        MonetizrSdk()

        return true
    }
}