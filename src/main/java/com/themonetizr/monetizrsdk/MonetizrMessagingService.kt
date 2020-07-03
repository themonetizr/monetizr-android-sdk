package com.themonetizr.monetizrsdk

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.themonetizr.monetizrsdk.dto.PurchaseCallbackData
import com.themonetizr.monetizrsdk.ui.activity.ProductActivity
import com.themonetizr.monetizrsdk.ui.dialog.MessageDialog
import org.json.JSONObject

class MonetizrMessagingService : FirebaseMessagingService() {


    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d("Monetizr", "From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("Monetizr", "Message data payload: ${remoteMessage.data}")
            handleNow(remoteMessage)
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d("Monetizr", "Message Notification Body: ${it.body}")
        }
    }
    // [END receive_message]

    // [START on_new_token]
    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Log.d("Monetizr", "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token)
    }
    // [END on_new_token]

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private fun handleNow(remoteMessage: RemoteMessage) {
        val jsonBody = JSONObject()
        jsonBody.put("orderNumber", remoteMessage.data["orderNumber"])
        jsonBody.put("offerTag", remoteMessage.data["offerTag"])
        jsonBody.put("createdAt", remoteMessage.data["createdAt"])
        jsonBody.put("totalPrice", remoteMessage.data["totalPrice"])
        val purchase = PurchaseCallbackData(jsonBody)
        MonetizrSdk.purchaseCallback.firePurchaseCallback(purchase)
        val manager = ProductActivity.getManager()
        if (manager != null) {
            val message = MessageDialog.newInstance(getText(R.string.purchase_successful).toString(), "success")
            message.show(manager!!, MessageDialog.TAG)
        }
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implement this method to send token to your app server.
        Log.d("Monetizr", "sendRegistrationTokenToServer($token)")
    }
}