package io.monetizr.monetizrsdk.dto

import org.json.JSONObject

class CheckoutWithPaymentBody {

    companion object {
        fun createBody(paymentInformation: String, checkoutInfo: JSONObject, productTag: String, variantForCheckout: JSONObject?, shippingAddress: JSONObject, chosenShippingRate: ShippingRate): JSONObject {
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
            val checkoutId = checkoutInfo.getJSONObject("data").getJSONObject("checkoutCreate").getJSONObject("checkout").getString("id")
            val paymentToken = paymentMethodData.getJSONObject("tokenizationData").getString("token")

            val totalPrice = if (variantForCheckout != null) {
                variantForCheckout.getJSONObject("priceV2").getString("amount")
            } else {
                "0"
            }

            val paymentAmount = JSONObject()

            if (chosenShippingRate.firstPrice() != null) {
                paymentAmount.put("amount", totalPrice.toDouble() + chosenShippingRate.firstPrice()!!.amount.toDouble())
                paymentAmount.put("currencyCode", chosenShippingRate.firstPrice()!!.currencyCode)
            }

            jsonBody.put("checkoutId", checkoutId)
            jsonBody.put("product_handle", productTag)
            jsonBody.put("billingAddress", billingAddress)
            jsonBody.put("idempotencyKey", paymentToken)
            jsonBody.put("paymentAmount", paymentAmount)
            jsonBody.put("paymentData", paymentToken)
            jsonBody.put("test", true)
            jsonBody.put("type", "google_pay")
            jsonBody.put("shippingAddress", shippingAddress)
            jsonBody.put("shippingRateHandle", chosenShippingRate.handle)
            jsonBody.put("email", email)

            return jsonBody
        }
    }
}