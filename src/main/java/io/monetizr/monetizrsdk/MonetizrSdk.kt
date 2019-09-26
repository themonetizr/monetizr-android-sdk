package io.monetizr.monetizrsdk

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
<<<<<<< Updated upstream
import android.os.Bundle
=======
import android.net.NetworkCapabilities
import android.os.Build
>>>>>>> Stashed changes
import android.preference.PreferenceManager
import android.util.DisplayMetrics
import android.util.Log
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import com.android.volley.Request
import io.monetizr.monetizrsdk.api.Telemetrics
import io.monetizr.monetizrsdk.api.WebApi
import io.monetizr.monetizrsdk.misc.ConfigHelper
import io.monetizr.monetizrsdk.misc.Parameters
import io.monetizr.monetizrsdk.provider.ActivityProvider
import io.monetizr.monetizrsdk.provider.ApplicationProvider
import io.monetizr.monetizrsdk.ui.activity.ProductActivity
import org.json.JSONObject
import java.util.*
<<<<<<< Updated upstream
import android.net.NetworkCapabilities
import android.os.Build
import io.monetizr.monetizrsdk.dto.Product
import io.monetizr.monetizrsdk.ui.activity.ProductActivity

/**
 * Monetizr Sdk entry point.
 *
 * For easier integration we are using singleton-pattern and companion objects
 */
=======

>>>>>>> Stashed changes
class MonetizrSdk {

    init {
        val app = ApplicationProvider.application as Context
        apikey = ConfigHelper.getConfigValue(app, "monetizr_api_key")
        apiAddress = ConfigHelper.getConfigValue(app, "monetizr_api_url")

        if (apikey == "") {
            Log.i("MonetizrSDK", "Api key was not provided from config file")

            apikey = "4D2E54389EB489966658DDD83E2D1"
        }

        if (apiAddress == "") {
            Log.i("MonetizrSDK", "Api address was not provided from config file")
            apiAddress = "https://api3.themonetizr.com/api/"
        }
    }

    companion object {
        var apikey: String = ""
        var apiAddress: String = ""
        var debuggable: Boolean = false
        private var initialLaunch: Boolean = true
        var firstCheckout: Boolean = true
<<<<<<< Updated upstream
        var firstImpressionClick: Boolean = true
=======
>>>>>>> Stashed changes
        var progressDialog: AlertDialog? = null

        /**
         * Show product for specified tag. Method is singleton pattern and called as MonetizrSdk.showProductForTag('product-tag')
         *
         * @param  productTag  {String}  Tag that user want to show. Tags are being provided by monetizr team
         */
        fun showProductForTag(productTag: String) {
            try {
                val application = ApplicationProvider.application as Context

                if (isNetworkAvailable(application)) {
                    createProgressDialog()
                    showProgressBar()
                    requestProductInformation(application, productTag, apikey)

                    // TODO make single request
                    Telemetrics.sendDeviceInfo()

                    if (initialLaunch) {
                        Telemetrics.session(ApplicationProvider.sessionStart)
                        Telemetrics.sndFirstImpression()
                        initialLaunch = false
                    }

<<<<<<< Updated upstream
                    // Check if this is the first installation
                    val monetizrSdkPreference =
                        PreferenceManager.getDefaultSharedPreferences(application)
                    val isFirstRun = monetizrSdkPreference.getBoolean("MonetizrSdkFirstrun", true)
=======
                    val monetizrSdkPreference = PreferenceManager.getDefaultSharedPreferences(application)
                    val isFirstRun = monetizrSdkPreference.getBoolean(Parameters.IS_FIRST_RUN, true)
>>>>>>> Stashed changes

                    if (isFirstRun) {
                        val editor = monetizrSdkPreference.edit()
                        editor.putBoolean(Parameters.IS_FIRST_RUN, false)
                        editor.apply()

                        Telemetrics.sendFistRun()
                    }

                    // Check for application update, if application with monetiizr is being updated
<<<<<<< Updated upstream
                    val appVersion =
                        monetizrSdkPreference.getString("MonetizrSdkBundleVersion", "0")
                    val ctx = ActivityProvider.currentActivity
                    val pInfo =
                        application.packageManager.getPackageInfo((ctx as Activity).packageName, 0)
=======
                    val appVersion = monetizrSdkPreference.getString("MonetizrSdkBundleVersion", "0")
                    val pInfo = application.packageManager.getPackageInfo((application as Activity).packageName, 0)
>>>>>>> Stashed changes
                    val version = pInfo.versionName
                    if (appVersion != version) {
                        Telemetrics.update()
                        val edit = monetizrSdkPreference.edit()
                        edit.putString("MonetizrSdkBundleVersion", version)
                        edit.apply()
                    }
                } else {
                    logError("Did not have internet access")
                }
            } catch (e: Exception) {
                logError(e)
            }
        }

        @Suppress("DEPRECATION")
        private fun isNetworkAvailable(context: Context): Boolean {
<<<<<<< Updated upstream
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            if (Build.VERSION.SDK_INT < 23) {
                val ni = cm.activeNetworkInfo

                if (ni != null) {
                    return ni.isConnected && (ni.type == ConnectivityManager.TYPE_WIFI || ni.type == ConnectivityManager.TYPE_MOBILE)
                }
            } else {
                val n = cm.activeNetwork

                if (n != null) {
                    val nc = cm.getNetworkCapabilities(n)

                    return nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(
                        NetworkCapabilities.TRANSPORT_WIFI
                    )
                }
            }
            return false
        }

        /**
         * Make a request for product, get information from API about requested product.
         * Method does get information and makes invokes product activity
         *
         * @param   context     {Context}  Application context from which this method was invoked
         * @param   productTag  {String}   Product that is being requested
         * @param   apiKey      {String}   Provided API auth key
         */
        private fun requestProductInformation(
            context: Context,
            productTag: String,
            apiKey: String
        ) {
=======
            var result = false
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cm?.run {
                    cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                        result = when {
                            hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                            hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                            hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                            else -> false
                        }
                    }
                }
            } else {
                cm?.run {
                    cm.activeNetworkInfo?.run {
                        if (type == ConnectivityManager.TYPE_WIFI) {
                            result = true
                        } else if (type == ConnectivityManager.TYPE_MOBILE) {
                            result = true
                        }
                    }
                }
            }
            return result
        }

        private fun requestProductInformation(context: Context, productTag: String, apiKey: String) {
>>>>>>> Stashed changes
            val ctx = ActivityProvider.currentActivity
            val display = (ctx as Activity).windowManager.defaultDisplay

            val realMetrics = DisplayMetrics()
            display.getRealMetrics(realMetrics)
            val realWidth = realMetrics.widthPixels
            val language = Locale.getDefault().displayLanguage
            val url =
                apiAddress + "products/tag/" + productTag + "?size=" + realWidth + "&language=" + language

<<<<<<< Updated upstream
            // Make a json request to API
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
                val product = Product(productInfo)
                putExtra("product", productInfo.toString())
                putExtra("product_tag", productTag)
            }
=======
            WebApi.getInstance(context).makeRequest(url, Request.Method.GET, null, apiKey, { onProductSuccess(it, productTag) }, ::onProductFail)
        }

        private fun onProductSuccess(response: JSONObject, productTag: String) {
            hideProgressBar()
            Telemetrics.clickreward(productTag)
            showProductActivity(response, productTag)
        }
>>>>>>> Stashed changes

        private fun onProductFail(error: Throwable) {
            hideProgressBar()
            logError(error)
        }

        private fun showProductActivity(productInfo: JSONObject, productTag: String) {
            val currentActivity = ActivityProvider.currentActivity ?: return
            ProductActivity.start(currentActivity, productInfo.toString(), productTag)
        }

        private fun createProgressDialog() {
            val application = ActivityProvider.currentActivity as Context

            //RelativeLayout.LayoutParams.MATCH_PARENT
            val holderLayout = RelativeLayout(application)
            val params = RelativeLayout.LayoutParams(200, 200)
<<<<<<< Updated upstream
            params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
=======
            params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
>>>>>>> Stashed changes
            holderLayout.layoutParams = params


            val progressBar = ProgressBar(application)
            progressBar.isIndeterminate = true
            holderLayout.addView(progressBar, params)

            val alertBuilder = AlertDialog.Builder(application)
            alertBuilder.setCancelable(true)
            alertBuilder.setView(holderLayout)

            progressDialog = alertBuilder.create()
<<<<<<< Updated upstream
            progressDialog!!.let { dialog ->
                val window = progressDialog!!.window
                val dialogWindow = dialog.window

                if (window != null && dialogWindow != null) {
                    val layoutParams = WindowManager.LayoutParams()
                    layoutParams.copyFrom(dialogWindow.attributes)
                    layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT
                    layoutParams.height = RelativeLayout.LayoutParams.MATCH_PARENT
                    dialogWindow.attributes = layoutParams
                    dialogWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                }
                dialog.show()
            }
        }

        /**
         * Hide progress dialog
         */
        fun hideProgressBar() {
            progressDialog!!.dismiss()
=======
            progressDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        private fun logError(error: Throwable) {
            if (debuggable) {
                Log.i("MonetizrSDK", "has en error:  $error")
                error.printStackTrace()
            }
        }

        private fun logError(error: String) {
            if (debuggable) {
                Log.i("MonetizrSDK", "has en error: $error")
            }
        }

        private fun showProgressBar() {
            progressDialog?.show()
        }

        private fun hideProgressBar() {
            progressDialog?.dismiss()
            progressDialog = null
>>>>>>> Stashed changes
        }
    }
}