package com.themonetizr.monetizrsdk.api

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.android.volley.Request
import com.themonetizr.monetizrsdk.provider.ActivityProvider
import com.themonetizr.monetizrsdk.provider.ApplicationProvider
import com.themonetizr.monetizrsdk.provider.ApplicationProvider.sessionStart
import com.themonetizr.monetizrsdk.MonetizrSdk
import com.themonetizr.monetizrsdk.misc.ConfigHelper
import com.themonetizr.monetizrsdk.misc.Parameters
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

@Suppress("SpellCheckingInspection")
class Telemetrics {

    companion object {
        private fun sendPostRequest(jsonBody: JSONObject, endpoint: String) {
            val ctx = ApplicationProvider.application as Context
            val apiKey = ConfigHelper.getConfigValue(ctx, Parameters.RAW_API_KEY)
            val apiAddress = ConfigHelper.getConfigValue(ctx, Parameters.RAW_API_ENDPOINT)
            val url = apiAddress + endpoint
            WebApi.getInstance(ctx).makeRequest(url, Request.Method.POST, jsonBody, apiKey, {}, Companion::onFail)
        }

        private fun onFail(error: Throwable) {
            if (MonetizrSdk.debuggable) {
                Log.i("MonetizrSDK", "Received API error $error")
                error.printStackTrace()
            }
        }

        private fun sendGetRequest(endpoint: String) {
            val ctx = ApplicationProvider.application as Context
            val apiKey = ConfigHelper.getConfigValue(ctx, Parameters.RAW_API_KEY)
            val apiAddress = ConfigHelper.getConfigValue(ctx, Parameters.RAW_API_ENDPOINT)
            val url = apiAddress + endpoint
            WebApi.getInstance(ctx).makeRequest(url, Request.Method.GET, null, apiKey, {}, Companion::onFail)
        }

        /**
         * Store some device information that is not relevant for specific user
         */
        @SuppressLint("HardwareIds")
        fun sendDeviceInfo() {
            val jsonBody = JSONObject()
            val application = ApplicationProvider.application as Context
            val region: String

            if (Build.VERSION.SDK_INT >= 24) {
                region = application.resources.configuration.locales[0].country
            } else {
                region = application.resources.configuration.locale.country
            }

            val androidId = Settings.Secure.getString(application.contentResolver, Settings.Secure.ANDROID_ID)
            val language = Locale.getDefault().displayLanguage
            jsonBody.put("device_name", Build.MANUFACTURER + " " + Build.MODEL)
            jsonBody.put("os_version", "Android: " + Build.VERSION.RELEASE + " SDK: " + Build.VERSION.SDK_INT)
            jsonBody.put("region", region)
            jsonBody.put("device_identifier", androidId)
            jsonBody.put("language", language)

            sendPostRequest(jsonBody, "telemetric/devicedata")
        }

        /**
         * Monetizr product has been opened and shown to user
         *
         * @param  rewardTag  {String}  Product or reward tag, that invoked window
         */
        fun clickreward(rewardTag: String) {
            val jsonBody = JSONObject()
            jsonBody.put("trigger_tag", rewardTag)
            sendPostRequest(jsonBody, "telemetric/clickreward")
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

            sendPostRequest(jsonBody, "telemetric/design")
        }

        /**
         * Storing information of when product dailog has been closed without any intervention
         *
         * @param  rewardTag  {String}  Product or reward tag, that was dismissed
         */
        fun dismiss(rewardTag: String) {
            val jsonBody = JSONObject()

            jsonBody.put("trigger_tag", rewardTag)

            sendPostRequest(jsonBody, "telemetric/dismiss")
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

            sendPostRequest(jsonBody, "telemetric/encounter")
        }

        /**
         * Milliseconds that have passed from time when application has started until the time first impression was shown to user
         */
        fun sndFirstImpression() {
            val jsonBody = JSONObject()

            val launchTime = System.currentTimeMillis() - ApplicationProvider.appStart
            jsonBody.put("first_impression_shown", launchTime)

            sendPostRequest(jsonBody, "telemetric/firstimpression")
        }

        /**
         * Milliseconds that have passed from time when application has started until the time first checkout has been clicked
         */
        fun firstimpressioncheckout() {
            val jsonBody = JSONObject()

            val launchTime = System.currentTimeMillis() - ApplicationProvider.appStart
            jsonBody.put("first_impression_checkout", launchTime)

            sendPostRequest(jsonBody, "telemetric/firstimpressioncheckout")
        }

        /**
         * Milliseconds that have passed from time when application has started until the time first variant selection has been clicked
         */
        fun firstimpressionclick() {
            val jsonBody = JSONObject()

            val launchTime = System.currentTimeMillis() - ApplicationProvider.appStart
            jsonBody.put("first_impression_click", launchTime)

            sendPostRequest(jsonBody, "telemetric/firstimpressionclick")
        }

        /**
         * Milliseconds that have passed from time when application has started until the time first purchase has been made
         *
         * @param  milliseconds  {long}  Miliseconds that have passed from application launch until first purchase
         */
        fun firstimpressionpurchase(milliseconds: Long) {
            val jsonBody = JSONObject()

            jsonBody.put("first_impression_purchase", milliseconds)

            sendPostRequest(jsonBody, "telemetric/firstimpressionpurchase")
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

            sendPostRequest(jsonBody, "telemetric/impressionvisible")
        }

        /**
         * Application, with integrated monetizr sdk has been installed
         */
        @SuppressLint("HardwareIds")
        fun sendFistRun() {
            val jsonBody = JSONObject()
            val application = ApplicationProvider.application as Context

            val androidId = Settings.Secure.getString(
                application.contentResolver,
                Settings.Secure.ANDROID_ID
            )
            jsonBody.put("device_identifier", androidId)
            jsonBody.put("installed", true)

            sendPostRequest(jsonBody, "telemetric/install")
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

            sendPostRequest(jsonBody, "telemetric/playerbehaviour")
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
            val androidId = Settings.Secure.getString(application.contentResolver, Settings.Secure.ANDROID_ID)

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

            sendPostRequest(jsonBody, "telemetric/purchase")
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

            sendPostRequest(jsonBody, "telemetric/session")
        }

        /**
         * Game session ended
         */
        fun sessionEnd() {
            val jsonBody = JSONObject()

            val application = ApplicationProvider.application as Context
            val androidId = Settings.Secure.getString(application.contentResolver, Settings.Secure.ANDROID_ID)

            val c = Calendar.getInstance()
            val date = c.time
            val sessionEnd = getStringTimeStampWithDate(date)

            jsonBody.put("device_identifier", androidId)
            jsonBody.put("session_start", sessionStart)
            jsonBody.put("session_end", sessionEnd)

            sendPostRequest(jsonBody, "telemetric/session/session_end")
        }

        fun getStringTimeStampWithDate(date: Date): String {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getDefault()
            return dateFormat.format(date)
        }

        /**
         * Application with monetizr sdk has been updated
         */
        fun update() {
            val jsonBody = JSONObject()

            val application = ApplicationProvider.application as Context
            val androidId = Settings.Secure.getString(application.contentResolver, Settings.Secure.ANDROID_ID)

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

            sendPostRequest(jsonBody, "telemetric/update")
        }
    }
}