package io.monetizr.monetizrsdk.api

import android.content.Context
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class WebApi constructor(context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: WebApi? = null

        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: WebApi(context).also {
                    INSTANCE = it
                }
            }
    }

    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(context.applicationContext)
    }

    private fun <T> addToRequestQueue(req: Request<T>) {
        req.retryPolicy = DefaultRetryPolicy(20 * 1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        requestQueue.add(req)
    }

    fun makeRequest(url: String, method: Int, body: JSONObject?, key: String, success: (JSONObject) -> Unit, fail: (Throwable) -> Unit) {
        val jsonObjectRequest = object : JsonObjectRequest(
            method, url, body,
            Response.Listener { response -> success(response) },
            Response.ErrorListener { error -> fail(error) }) {
            override fun getHeaders(): MutableMap<String, String> {
                val header = mutableMapOf<String, String>()
                header["Authorization"] = "Bearer $key"
                return header
            }
        }

        addToRequestQueue(jsonObjectRequest)
    }
}