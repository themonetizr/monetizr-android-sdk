package io.monetizr.monetizrsdk

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.DisplayMetrics
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Monetizr Sdk entry point.
 *
 * For easier integration we are using singleton-pattern and companion objects
 */
class MonetizrSdk {

    /**
     * Settings are being provided in config.properties and
     * Invoked from Application provider on host application launch
     */
    init {
        // Get access to application context and grab application settings
        val app = ApplicationProvider.application as Context
        apikey = ConfigHelper.getConfigValue(app, "monetizr_api_key")
        apiAddress = ConfigHelper.getConfigValue(app, "monetizr_api_url")

        // Fallback for apikey and for api url to default values if no config was found
        if (apikey == "") {
            Log.i("MonetizrSDK", "Api key was not provided from config file")

            apikey = "4D2E54389EB489966658DDD83E2D1"
        }

        if (apiAddress == "") {
            Log.i("MonetizrSDK", "Api address was not provided from config file")
            apiAddress = "https://api3.themonetizr.com/api/"
        }
    }

    /**
     * Singleton instance access point
     */
    companion object {
        // Application access key, allows to connect to oAuth 2 based api using generated token
        var apikey: String = ""

        // Api host address, does come from settings, but can be changed
        var apiAddress: String = ""

        // Debuggable library for development purposes, printing out logs
        var debuggable: Boolean = false

        // Variables are being used to track if application is launched for the first time
        // as well as if some events does happen for the first time
        private var initialLaunch: Boolean = true
        var firstCheckout: Boolean = true
        var firstImpressionClick: Boolean = true

        /**
         * Show product for specified tag. Method is singleton pattern and called as MonetizrSdk.showProductForTag('product-tag')
         *
         * @param  productTag  {String}  Tag that user want to show. Tags are being provided by monetizr team
         */
        fun showProductForTag(productTag: String) {

            // Die silently to not interfere with application if something goes wrong
            try {
                val application = ApplicationProvider.application as Context

                // Check for internet connectivity
                if (isNetworkAvailable(application)) {
                    requestProductInformation(application, productTag, apikey)

                    // Store device data, as device is not persons information, it is not GDPR regulation
                    Telemetrics.deviceData()

                    // This is an initial application launch, store timers and launched products information
                    if (initialLaunch) {
                        // Session is misleading as it tracks when application was started but logs only when sdk has been initialized
                        Telemetrics.session(ApplicationProvider.sessionStart)
                        Telemetrics.firstimpression()
                        initialLaunch = false
                    }

                    // Check if this is the first installation
                    val monetizrSdkPreference = PreferenceManager.getDefaultSharedPreferences(application)
                    val isFirstRun = monetizrSdkPreference.getBoolean("MonetizrSdkFirstrun", true)

                    // Checking for application first launch, install event
                    if (isFirstRun) {
                        // Run once on initial installation
                        val editor = monetizrSdkPreference.edit()
                        editor.putBoolean("MonetizrSdkFirstrun", false)
                        editor.apply()

                        // Save telemetric data on installation
                        Telemetrics.install()
                    }

                    // Check for application update, if application with monetiizr is being updated
                    val appVersion = monetizrSdkPreference.getString("MonetizrSdkBundleVersion", "0")
                    val ctx = ActivityProvider.currentActivity
                    val pInfo = application.packageManager.getPackageInfo((ctx as Activity).packageName, 0)
                    val version = pInfo.versionName
                    if (appVersion != version) {
                        Telemetrics.update()

                        // Update package info
                        val edit = monetizrSdkPreference.edit()
                        edit.putString("MonetizrSdkBundleVersion", version)
                        edit.apply()
                    }
                } else {
                    if (debuggable) {
                        Log.i("MonetizrSDK", "Did not have internet access")
                    }
                }
            } catch (e: Exception) {
                if (debuggable) {
                    Log.i("MonetizrSDK", "Did not have access to application context")
                }
            }
        }

        /**
         * Format date to server datetime string
         *
         * @param  date  {Date}  Date object to transform to server timestamp with timezone
         * @return {String} Datetime formatted to server timestamp
         */
        fun getStringTimeStampWithDate(date: Date): String {
            val dateFormat = SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                Locale.getDefault()
            )
            dateFormat.timeZone = TimeZone.getDefault()
            return dateFormat.format(date)
        }

        /**
         * Checking for network availability. It is not possible to store data without network access, at this moment at least
         *
         * @param   context    {Context}  Application context from which product has been called
         * @return  {Boolean}             True/False for network state in device
         */
        private fun isNetworkAvailable(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo: NetworkInfo
            activeNetworkInfo = cm.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }

        /**
         * Make a request for product, get information from API about requested product.
         * Method does get information and makes invokes product activity
         *
         * @param   context     {Context}  Application context from which this method was invoked
         * @param   productTag  {String}   Product that is being requested
         * @param   apiKey      {String}   Provided API auth key
         */
        private fun requestProductInformation(context: Context, productTag: String, apiKey: String) {
            val ctx = ActivityProvider.currentActivity
            val display = (ctx as Activity).windowManager.defaultDisplay

            // Get some device matrics, to request correct image size
            val realMetrics = DisplayMetrics()
            display.getRealMetrics(realMetrics)
            val realWidth = realMetrics.widthPixels
            val language = Locale.getDefault().displayLanguage
            val url = apiAddress + "products/tag/" + productTag + "?size=" + realWidth + "&language=" + language

            // Make a json request to APPI
            val jsonObjectRequest = object : JsonObjectRequest(
                Method.GET, url, null,
                Response.Listener { response ->
                    showProductWindow(response, productTag)
                    Telemetrics.clickreward(productTag)
                },
                Response.ErrorListener { error ->
                    if (debuggable) {
                        // Die silently, so it does not provide any bad experience
                        Log.i("MonetizrSDK", "Received api error $error")
                        error.printStackTrace()
                    }
                }) {

                // Override headers to pass authorization
                override fun getHeaders(): MutableMap<String, String> {

                    val header = mutableMapOf<String, String>()
                    header["Authorization"] = "Bearer $apiKey"
                    return header
                }
            }

            // Access the RequestQueue through singleton class.
            SingletonRequest.getInstance(context).addToRequestQueue(jsonObjectRequest)
        }

        /**
         * If request was successful, invoke product activity that takes care of product view
         *
         * @param   productInfo  {JSONObject}   Product information from API, as json
         * @param   productTag   {String}       P5roduct that invoked the method
         */
        private fun showProductWindow(productInfo: JSONObject, productTag: String) {
            val currentActivity = ActivityProvider.currentActivity
            val intent = Intent(currentActivity, ProductActivity::class.java).apply {
                putExtra("product", productInfo.toString())
                putExtra("product_tag", productTag)
            }

            val customArguments = Bundle()
            val activityContext = currentActivity as Context
            startActivity(activityContext, intent, customArguments)
        }
    }
}