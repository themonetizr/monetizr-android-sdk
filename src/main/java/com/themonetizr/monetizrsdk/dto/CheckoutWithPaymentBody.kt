package com.themonetizr.monetizrsdk.dto

import org.json.JSONObject

class CheckoutWithPaymentBody {

    companion object {
        fun createBody(paymentInformation: String, checkoutInfo: JSONObject, productTag: String, shippingAddress: ShippingAddress?, chosenShippingRate: ShippingRate): JSONObject {
            val paymentMethodData = JSONObject(paymentInformation).getJSONObject("paymentMethodData")
            val billingAddressData = paymentMethodData.getJSONObject("info").getJSONObject("billingAddress")
            val email = JSONObject(paymentInformation).getString("email")

            val billingAddress = JSONObject()

            billingAddress.put("firstName", billingAddressData.getString("name"))
            billingAddress.put("lastName", billingAddressData.getString("name"))
            billingAddress.put("address1", billingAddressData.getString("address1"))
            billingAddress.put("address2", billingAddressData.getString("address2"))
            billingAddress.put("city", billingAddressData.getString("locality"))
            billingAddress.put("province", billingAddressData.getString("administrativeArea"))
            billingAddress.put("country", billingAddressData.getString("countryCode"))
            billingAddress.put("zip", billingAddressData.getString("postalCode"))

            val jsonBody = JSONObject()
            val checkoutId = checkoutInfo.getString("id")

            jsonBody.put("checkoutId", checkoutId)
            jsonBody.put("product_handle", productTag)
            jsonBody.put("billingAddress", billingAddress)
            jsonBody.put("shippingAddress", shippingAddress?.getJsonObject())
            jsonBody.put("shippingRateHandle", chosenShippingRate.handle)
            jsonBody.put("email", email)

            return jsonBody
        }

        fun paymentBody(paymentInformation: String?, checkoutInfo: JSONObject, productTag: String): JSONObject {
            val paymentMethodData = JSONObject(paymentInformation).getJSONObject("paymentMethodData")

            val jsonBody = JSONObject()
            val checkoutId = checkoutInfo.getString("id")
            val paymentTokenString = paymentMethodData.getJSONObject("tokenizationData").getString("token")
            val paymentToken = JSONObject(paymentTokenString).getString("id")

            jsonBody.put("checkoutId", checkoutId)
            jsonBody.put("product_handle", productTag)
            jsonBody.put("payment_token", paymentToken)
            jsonBody.put("type", "google_pay")
            jsonBody.put("test", true)

            return jsonBody
        }
    }
}