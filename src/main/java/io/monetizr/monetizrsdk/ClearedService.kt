package io.monetizr.monetizrsdk

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

/**
 * Service that manages to run in background and log application destroy to catch event and submit session length
 */
class ClearedService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i("MonetizrSDK", "Service Started")
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("MonetizrSDK", "Service Destroyed")
    }

    /**
     * Adding functionality when service is being destroyed, in this case logging session length
     */
    override fun onTaskRemoved(rootIntent: Intent) {
        Log.i("MonetizrSDK", "Application destroyed")
        // Application is destroyed
        stopSelf()
        Telemetrics.sessionEnd()
    }
}