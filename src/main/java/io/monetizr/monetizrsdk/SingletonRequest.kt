package io.monetizr.monetizrsdk

import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.Volley

class SingletonRequest constructor(context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: SingletonRequest? = null
        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SingletonRequest(context).also {
                    INSTANCE = it
                }
            }
    }

    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(context.applicationContext)
    }

    fun <T> addToRequestQueue(req: Request<T>) {
        req.retryPolicy = DefaultRetryPolicy(20 * 1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        requestQueue.add(req)
    }
}