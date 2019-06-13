package io.monetizr.monetizrsdk

import android.content.Context
import android.content.res.Resources
import android.util.Log
import java.io.IOException
import java.util.*

/**
 * Read configuration from hidden config files to prevent api settings exposure
 */
object ConfigHelper {

    private const val TAG = "MonetizrSDK"

    /**
     * Get configuration values from settings
     *
     * @param  context  {Context}  Application context, to access settings
     * @param  name     {String}   Name of the configuration value
     */
    fun getConfigValue(context: Context, name: String): String {
        val resources = context.resources

        try {
            val rawResource = resources.openRawResource(R.raw.config)
            val properties = Properties()
            properties.load(rawResource)
            return properties.getProperty(name)
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Unable to find the config file: " + e.message)
        } catch (e: IOException) {
            Log.e(TAG, "Failed to open config file.")
        }

        return ""
    }
}
