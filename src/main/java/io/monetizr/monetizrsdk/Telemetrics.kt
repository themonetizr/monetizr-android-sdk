package io.monetizr.monetizrsdk

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import io.monetizr.monetizrsdk.ApplicationProvider.sessionStart
import org.json.JSONObject
import java.util.*


/**
 * Telemetrics class. Singleton patter to allow easier initialization and calls outside monetizr sdk in host application
 */
@Suppress("SpellCheckingInspection")
class Telemetrics {

    companion object {

        /**
         * Make POST request, add it to volley request queue to finish them
         *
         * @param  jsonBody  {JSONObject}  Request body parameters
         * @param  endpoint  {String}      API endpoint to send request to
         */
        private fun sendPostRequest(jsonBody: JSONObject, endpoint: String) {
            val url = MonetizrSdk.apiAddress + endpoint

            val jsonObjectRequest = object : JsonObjectRequest(
                Method.POST, url, jsonBody,
                Response.Listener {
                    // Successful response, Do nothing
                },
                Response.ErrorListener { error ->
                    if (MonetizrSdk.debuggable) {
                        // Die silently, so it does not provide any bad experience
                        Log.i("MonetizrSDK", "Received API error $error")
                        error.printStackTrace()
                    }
                }) {

                // Override headers to pass authorization
                override fun getHeaders(): MutableMap<String, String> {

                    val header = mutableMapOf<String, String>()
                    header["Authorization"] = "Bearer " + MonetizrSdk.apikey
                    return header
                }
            }

            // Access the RequestQueue through singleton class.
            val ctx = ApplicationProvider.application as Context
            SingletonRequest.getInstance(ctx).addToRequestQueue(jsonObjectRequest)
        }

        /**
         * Create a GET requests
         *
         * @param  endpoint  {String}      API endpoint to send request to
         */
        private fun sendGetRequest(endpoint: String) {

            val url = MonetizrSdk.apiAddress + endpoint

            val jsonObjectRequest = object : JsonObjectRequest(
                Method.GET, url, null,
                Response.Listener {
                    // Successful response, Do Nothing
                },
                Response.ErrorListener { error ->
                    if (MonetizrSdk.debuggable) {
                        // Die silently, so it does not provide any bad experience
                        Log.i("MonetizrSDK", "Received API error $error")
                        error.printStackTrace()
                    }
                }) {

                // Override headers to pass authorization
                override fun getHeaders(): MutableMap<String, String> {

                    val header = mutableMapOf<String, String>()
                    header["Authorization"] = "Bearer " + MonetizrSdk.apikey
                    return header
                }
            }

            // Access the RequestQueue through singleton class.
            val ctx = ApplicationProvider.application as Context
            SingletonRequest.getInstance(ctx).addToRequestQueue(jsonObjectRequest)
        }

        /**
         * Store some device information that is not relevant for specific user
         */
        @SuppressLint("HardwareIds")
        fun deviceData() {
            val jsonBody = JSONObject()
            val application = ApplicationProvider.application as Context
            var region: String

            if (Build.VERSION.SDK_INT >= 24) {
                region = application.resources.configuration.locales[0].country
            } else {
                region = application.resources.configuration.locale.country
            }

            val androidId = Settings.Secure.getString(
                application.contentResolver,
                Settings.Secure.ANDROID_ID
            )
            val language = Locale.getDefault().displayLanguage
            jsonBody.put("device_name", Build.MANUFACTURER + " " + Build.MODEL)
            jsonBody.put("os_version", "Android: " + Build.VERSION.RELEASE + " SDK: " + Build.VERSION.SDK_INT)
            jsonBody.put("region", region)
            jsonBody.put("device_identifier", androidId)
            jsonBody.put("language", language)

            this.sendPostRequest(jsonBody, "telemetric/devicedata")
        }

        /**
         * Monetizr product has been opened and shown to user
         *
         * @param  rewardTag  {String}  Product or reward tag, that invoked window
         */
        fun clickreward(rewardTag: String) {
            val jsonBody = JSONObject()
            jsonBody.put("trigger_tag", rewardTag)

            this.sendPostRequest(jsonBody, "telemetric/clickreward")
        }

        /**
         * Store information about triggers in game. For now manual task
         *
         * @param numberOfTriggers  {Int}    Integer, number of triggers in a game
         * @param funnelTriggerList {String} Text, list of triggers in a game, separated with comma
         */
        fun designInfo(numberOfTriggers: Int, funnelTriggerList: String) {
            val jsonBody = JSONObject()

            jsonBody.put("number_of_triggers", numberOfTriggers)
            jsonBody.put("funnel_trigger_list", funnelTriggerList)

            this.sendPostRequest(jsonBody, "telemetric/design")
        }

        /**
         * Storing information of when product dailog has been closed without any intervention
         *
         * @param  rewardTag  {String}  Product or reward tag, that was dismissed
         */
        fun dismiss(rewardTag: String) {
            val jsonBody = JSONObject()

            jsonBody.put("trigger_tag", rewardTag)

            this.sendPostRequest(jsonBody, "telemetric/dismiss")
        }

        /**
         * Store information about encounter that was used to invoke interaction.
         * As this is not automatically possible, providing manual input method
         *
         * @param  triggerType           {String}  Trigger that invoked interaction. For example this was a click
         * @param  completionStatus      {Int}     At what stage this encounter was invoked in the game. Estimated value
         * @param  triggerTag            {String}  Trigger that caused encounter (solved puzzle, reached new level, crossword solved, etc)
         * @param  levelName             {String}  Game level with enabled/specified trigger.
         * @param  difficultyLevelName  {String}  Level name, for example, how difficult this level was: "hard, medium, easy, etc"
         * @param  difficultyEstimation  {Int}     Overall difficulty estimation at the moment of product being shown
         */
        fun encounter(
            triggerType: String,
            completionStatus: Int,
            triggerTag: String,
            levelName: String,
            difficultyLevelName: String,
            difficultyEstimation: Int
        ) {
            val jsonBody = JSONObject()

            jsonBody.put("trigger_type", triggerType)
            jsonBody.put("completion_status", completionStatus)
            jsonBody.put("trigger_tag", triggerTag)
            jsonBody.put("level_name", levelName)
            jsonBody.put("difficulty_level_name", difficultyLevelName)
            jsonBody.put("difficulty_estimation", difficultyEstimation)

            this.sendPostRequest(jsonBody, "telemetric/encounter")
        }

        /**
         * Milliseconds that have passed from time when application has started until the time first impression was shown to user
         */
        fun firstimpression() {
            val jsonBody = JSONObject()

            val launchTime = System.currentTimeMillis() - ApplicationProvider.appStart
            jsonBody.put("first_impression_shown", launchTime)

            this.sendPostRequest(jsonBody, "telemetric/firstimpression")
        }

        /**
         * Milliseconds that have passed from time when application has started until the time first checkout has been clicked
         */
        fun firstimpressioncheckout() {
            val jsonBody = JSONObject()

            val launchTime = System.currentTimeMillis() - ApplicationProvider.appStart
            jsonBody.put("first_impression_checkout", launchTime)

            this.sendPostRequest(jsonBody, "telemetric/firstimpressioncheckout")
        }

        /**
         * Milliseconds that have passed from time when application has started until the time first variant selection has been clicked
         */
        fun firstimpressionclick() {
            val jsonBody = JSONObject()

            val launchTime = System.currentTimeMillis() - ApplicationProvider.appStart
            jsonBody.put("first_impression_click", launchTime)

            this.sendPostRequest(jsonBody, "telemetric/firstimpressionclick")
        }

        /**
         * Milliseconds that have passed from time when application has started until the time first purchase has been made
         *
         * @param  milliseconds  {long}  Miliseconds that have passed from application launch until first purchase
         */
        fun firstimpressionpurchase(milliseconds: Long) {
            val jsonBody = JSONObject()

            jsonBody.put("first_impression_purchase", milliseconds)

            this.sendPostRequest(jsonBody, "telemetric/firstimpressionpurchase")
        }

        /**
         * Milliseconds that have passed from time when product has been visible until time it has been dismissed
         *
         * @param  millisecondsProductVisible  {Long}    Milliseconds that have passed from application launch until product dismiss
         * @param  rewardTag                   {String}  Product or reward tag, that invoked window
         */
        fun impressionvisible(millisecondsProductVisible: Long, rewardTag: String) {
            val jsonBody = JSONObject()

            jsonBody.put("trigger_tag", rewardTag)
            jsonBody.put("time_until_dismiss", millisecondsProductVisible)

            this.sendPostRequest(jsonBody, "telemetric/impressionvisible")
        }

        /**
         * Application, with integrated monetizr sdk has been installed
         */
        @SuppressLint("HardwareIds")
        fun install() {
            val jsonBody = JSONObject()
            val application = ApplicationProvider.application as Context

            val androidId = Settings.Secure.getString(
                application.contentResolver,
                Settings.Secure.ANDROID_ID
            )
            jsonBody.put("device_identifier", androidId)
            jsonBody.put("installed", true)

            this.sendPostRequest(jsonBody, "telemetric/install")
        }

        /**
         * Player behaviour event, opt-in endpoint as it does not have automatically available data
         *
         * @param  gameProgress  {Int}  Integer, percentage, estimated level of game progress at the time of calling product show, for example 30% completed
         * @param  sessionTime   {Int}  Integer, seconds since application has been started
         */
        fun playerbehaviour(gameProgress: Int, sessionTime: Int) {
            val jsonBody = JSONObject()

            val application = ApplicationProvider.application as Context
            val androidId = Settings.Secure.getString(
                application.contentResolver,
                Settings.Secure.ANDROID_ID
            )

            jsonBody.put("device_identifier", androidId)
            jsonBody.put("game_progress", gameProgress)
            jsonBody.put("session_time", sessionTime)

            this.sendPostRequest(jsonBody, "telemetric/playerbehaviour")
        }

        /**
         * Milliseconds that have passed from time when application has started until the time first purchase has been made
         *
         * @param  rewardTag     {String}  Product or reward tag, that invoked window
         * @param  productPrice  {Double}  Product price
         * @param  currency      {String}  Product currency
         * @param  city          {String}  City
         */
        fun purchase(rewardTag: String, productPrice: Double, currency: String, city: String) {
            val jsonBody = JSONObject()

            val application = ApplicationProvider.application as Context
            val androidId = Settings.Secure.getString(
                application.contentResolver,
                Settings.Secure.ANDROID_ID
            )

            var region: String

            if (Build.VERSION.SDK_INT >= 24) {
                region = application.resources.configuration.locales[0].country
            } else {
                region = application.resources.configuration.locale.country
            }

            jsonBody.put("device_identifier", androidId)
            jsonBody.put("trigger_tag", rewardTag)
            jsonBody.put("product_price", productPrice)
            jsonBody.put("currency", currency)
            jsonBody.put("country", region)
            jsonBody.put("city", city)

            this.sendPostRequest(jsonBody, "telemetric/purchase")
        }

        /**
         * New game session has been created
         *
         * @param  sessionStart   {String}  Datetime when session started, format "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
         */
        fun session(sessionStart: String) {
            val jsonBody = JSONObject()

            val application = ApplicationProvider.application as Context
            val androidId = Settings.Secure.getString(
                application.contentResolver,
                Settings.Secure.ANDROID_ID
            )

            jsonBody.put("device_identifier", androidId)
            jsonBody.put("session_start", sessionStart)

            this.sendPostRequest(jsonBody, "telemetric/session")
        }

        /**
         * Game session ended
         */
        fun sessionEnd() {
            val jsonBody = JSONObject()

            val application = ApplicationProvider.application as Context
            val androidId = Settings.Secure.getString(
                application.contentResolver,
                Settings.Secure.ANDROID_ID
            )

            val c = Calendar.getInstance()
            val date = c.time
            val sessionEnd = MonetizrSdk.getStringTimeStampWithDate(date)

            jsonBody.put("device_identifier", androidId)
            jsonBody.put("session_start", sessionStart)
            jsonBody.put("session_end", sessionEnd)

            this.sendPostRequest(jsonBody, "telemetric/session/session_end")
        }

        /**
         * Application with monetizr sdk has been updated
         */
        fun update() {
            val jsonBody = JSONObject()

            val application = ApplicationProvider.application as Context
            val androidId = Settings.Secure.getString(
                application.contentResolver,
                Settings.Secure.ANDROID_ID
            )

            var version = "undefined"
            try {
                val ctx = ActivityProvider.currentActivity
                val pInfo = application.packageManager.getPackageInfo((ctx as Activity).packageName, 0)
                version = pInfo.versionName
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }

            jsonBody.put("device_identifier", androidId)
            jsonBody.put("bundle_version", version)

            this.sendPostRequest(jsonBody, "telemetric/update")
        }
    }
}