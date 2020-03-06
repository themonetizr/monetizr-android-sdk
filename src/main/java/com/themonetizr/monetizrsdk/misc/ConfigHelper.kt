package com.themonetizr.monetizrsdk.misc

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.themonetizr.monetizrsdk.MonetizrSdk
import java.io.IOException

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
        try {
            val app = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            val bundle = app.metaData

            val result = bundle.getString(name)
            if (result == null) {
                if (MonetizrSdk.debuggable) {
                    Log.e(TAG, "Failed to get value from manifest meta data")
                }
            } else {
                return result
            }
        } catch (e: IOException) {
            if (MonetizrSdk.debuggable) {
                Log.e(TAG, "Failed get value from manifest meta data")
            }
        }
        return ""
    }
}
