package com.themonetizr.monetizrsdk

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.themonetizr.monetizrsdk.api.Telemetrics

/**
 * Service that manages to run in background and log application destroy to catch event and submit session length
 */
class ClearedService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (MonetizrSdk.debuggable) {
            Log.i("MonetizrSDK", "Service Started")
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (MonetizrSdk.debuggable) {
            Log.i("MonetizrSDK", "Service Destroyed")
        }
    }

    /**
     * Adding functionality when service is being destroyed, in this case logging session length
     */
    override fun onTaskRemoved(rootIntent: Intent) {
        if (MonetizrSdk.debuggable) {
            Log.i("MonetizrSDK", "Application destroyed")
        }

        // Application is destroyed
        stopSelf()
        Telemetrics.sessionEnd()
    }
}