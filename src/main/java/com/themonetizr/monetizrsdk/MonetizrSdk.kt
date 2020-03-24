package com.themonetizr.monetizrsdk

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import androidx.preference.PreferenceManager
import com.android.volley.Request
import com.android.volley.VolleyError
import com.themonetizr.monetizrsdk.api.Telemetrics
import com.themonetizr.monetizrsdk.api.WebApi
import com.themonetizr.monetizrsdk.misc.ConfigHelper
import com.themonetizr.monetizrsdk.misc.Parameters
import com.themonetizr.monetizrsdk.provider.ActivityProvider
import com.themonetizr.monetizrsdk.provider.ApplicationProvider
import com.themonetizr.monetizrsdk.ui.activity.ProductActivity
import com.themonetizr.monetizrsdk.ui.dialog.MessageDialog
import com.themonetizr.monetizrsdk.ui.helpers.ErrorMessageBuilder
import com.themonetizr.monetizrsdk.ui.helpers.ProgressDialogBuilder
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.util.*



class MonetizrSdk {

    companion object {
        var debuggable: Boolean = false
        var dynamicApiKey: String? = null
        private var playerId: String? = null
        private var lockedProduct: Boolean = false
        private var initialLaunch: Boolean = true
        private var progressDialog: AlertDialog? = null

        /**
         * Show product with specified tag for specific user
         *
         * @param  String  productTag                Product tag that is provided to customer
         * @param  Boolean locked_product            Determines if this product has to be displayed as locked
         * @param  String  player_id                 Player ID (optional parameter in case this is a pre-paid product)
         */
        fun showProductForTag(productTag: String, locked_product: Boolean = false, player_id: String? = null) {
            try {
                val context = ApplicationProvider.application as Context
                val activity = ActivityProvider.currentActivity

                if (activity == null) {
                    logError("Activity context is null")
                    return
                }

                if (isNetworkAvailable(context) == false) {
                    logError("Did not have internet access")
                    ErrorMessageBuilder.makeDialog(activity, context.getString(R.string.no_network))?.show()
                    return
                }

                var apiKey = ConfigHelper.getConfigValue(context, Parameters.RAW_API_KEY)

                // Use specified api key if it is passed to method
                if (dynamicApiKey?.equals(null) == false) {
                    apiKey = dynamicApiKey.toString()
                }

                lockedProduct = locked_product

                if (player_id?.equals(null) == false) {
                    playerId = player_id.toString()
                }


                val endpoint = ConfigHelper.getConfigValue(context, Parameters.RAW_API_ENDPOINT)

                progressDialog = ProgressDialogBuilder.makeProgressDialog(activity)

                showProgressBar()
                requestProductInformation(activity, productTag, endpoint, apiKey)
                sendTelemetricsInfo(activity)

            } catch (e: Exception) {
                logError(e)
            }
        }

        private fun sendTelemetricsInfo(context: Context) {
            Telemetrics.sendDeviceInfo()

            if (initialLaunch) {
                Telemetrics.session(ApplicationProvider.sessionStart)
                Telemetrics.sndFirstImpression()
                initialLaunch = false
            }

            val monetizrSdkPreference = PreferenceManager.getDefaultSharedPreferences(context)
            val isFirstRun = monetizrSdkPreference.getBoolean(Parameters.IS_FIRST_RUN, true)

            if (isFirstRun) {
                val editor = monetizrSdkPreference.edit()
                editor.putBoolean(Parameters.IS_FIRST_RUN, false)
                editor.apply()

                Telemetrics.sendFistRun()
            }

            val lastUpdateVersion = monetizrSdkPreference.getInt(Parameters.LAST_UPDATE_VERSION, BuildConfig.VERSION_CODE)
            val currentVersion = BuildConfig.VERSION_CODE

            if (lastUpdateVersion == currentVersion) {
                val edit = monetizrSdkPreference.edit()
                edit.putInt(Parameters.LAST_UPDATE_VERSION, lastUpdateVersion)
                edit.apply()
            } else if (lastUpdateVersion < currentVersion) {
                Telemetrics.update()
            }
        }

        @Suppress("DEPRECATION")
        private fun isNetworkAvailable(context: Context): Boolean {
            var result = false
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                logError("Build version above M")
                cm?.run {
                    cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                        result = when {
                            hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                            hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                            hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                            hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> true
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

        private fun requestProductInformation(activity: Activity, productTag: String, endpoint: String, apiKey: String) {
            val display = activity.windowManager.defaultDisplay

            val realMetrics = DisplayMetrics()
            display.getRealMetrics(realMetrics)
            val realWidth = realMetrics.widthPixels
            val language = Locale.getDefault().displayLanguage
            val url = endpoint + "products/" + productTag + "?size=" + realWidth + "&locale=" + language

            WebApi.getInstance(activity).makeRequest(url, Request.Method.GET, null, apiKey, { onProductSuccess(it, productTag) }, ::onProductFail)
        }

        private fun onProductSuccess(response: JSONObject, productTag: String) {
            hideProgressBar()
            Telemetrics.clickreward(productTag)
            showProductActivity(response, productTag)
        }

        private fun onProductFail(it: VolleyError) {
            hideProgressBar()
            logError(it)

            // Show error message if request was invalid
            parseVolleyError(it)
        }

        private fun showProductActivity(productInfo: JSONObject, productTag: String) {
            val currentActivity = ActivityProvider.currentActivity ?: return
            val data = productInfo.getJSONObject("data")

            val activity = ActivityProvider.currentActivity as Context

            if (data.has("productByHandle") && !data.isNull("productByHandle")) {
                val product = data?.getJSONObject("productByHandle")
                ProductActivity.playerId = playerId
                ProductActivity.lockedProduct = lockedProduct
                ProductActivity.start(currentActivity, product.toString(), productTag)
            } else {
                logError("Product not found")
                ErrorMessageBuilder.makeDialog(activity, "Product was not found")?.show()
            }
        }


        fun logError(error: Throwable) {
            if (debuggable) {
                Log.i("MonetizrSDK", "has an error:  $error")
                error.printStackTrace()
            }
        }

        fun logError(error: String) {
            if (debuggable) {
                Log.i("MonetizrSDK", "has an error:  $error")
            }
        }

        fun logError(error: String, fragmentManager: FragmentManager) {
            if (debuggable) {
                Log.i("MonetizrSDK", "has an error:  $error")
                val message = MessageDialog.newInstance("has an error:  $error", "success")
                message.show(fragmentManager, MessageDialog.TAG)
            }
        }

        fun logError(error: VolleyError, fragmentManager: FragmentManager) {
            if (debuggable) {
                try {
                    val responseBody = String(error.networkResponse.data)
                    val data = JSONObject(responseBody)
                    var message = ""
                    if (data.has("message")) {
                        message = data.getString("message")
                    } else if (data.has("detail")) {
                        message = data.getString("detail")
                    } else {
                        // Assumption that required fields were not provided
                        message = "Required fields were not provided for API request"
                    }
                    logError(data.toString())
                    val error = MessageDialog.newInstance(message, "error")
                    error.show(fragmentManager, MessageDialog.TAG)
                } catch (e: JSONException) {
                    logError(e)
                } catch (e: UnsupportedEncodingException) {
                    logError(e)
                }catch (e: Exception){
                    logError(e)
                }
            }
        }

        private fun parseVolleyError(error: VolleyError) {

            val activity = ActivityProvider.currentActivity as Context

            try {
                val responseBody = String(error.networkResponse.data)
                val data = JSONObject(responseBody)

                var message = ""
                if (data.has("message")) {
                    message = data.getString("message")
                } else if (data.has("detail")) {
                    message = data.getString("detail")
                }
                ErrorMessageBuilder.makeDialog(activity, message)?.show()
            } catch (e: JSONException) {
                logError(e)
                ErrorMessageBuilder.makeDialog(activity, "Unexpected error has occured, please check your settings and try again")?.show()
            } catch (e: UnsupportedEncodingException) {
                logError(e)
            }catch (e: Exception){
                logError(e)
                ErrorMessageBuilder.makeDialog(activity, "Unexpected error has occured, please check your settings and try again")?.show()
            }
        }

        private fun showProgressBar() {
            progressDialog?.show()
        }

        private fun hideProgressBar() {
            progressDialog?.dismiss()
            progressDialog = null
        }
    }

}