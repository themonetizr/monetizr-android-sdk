package com.themonetizr.monetizrsdk.dto

import com.google.android.gms.wallet.PaymentData
import org.json.JSONObject
import java.util.*

class CheckoutBody {

    companion object {
        public fun createBody(variant: JSONObject, productTag: String, withPayment: PaymentData?): JSONObject {
            val language = Locale.getDefault().displayLanguage
            val jsonBody = JSONObject()

            jsonBody.put("product_handle", productTag)
            jsonBody.put("variantId", variant.getString("id"))
            jsonBody.put("language", language)
            jsonBody.put("quantity", 1)


            if (withPayment != null) {
                val paymentJson = JSONObject(withPayment?.toJson())
                val it = paymentJson.getJSONObject("shippingAddress")
                val info = ShippingAddressInto(it.getString("name"), it.getString("name"), it.getString("address1"), it.getString("address2"), it.getString("locality"), it.getString("administrativeArea"), it.getString("countryCode"), it.getString("postalCode"))
                jsonBody.put("shippingAddress", info.getJsonObject())
            }

            return jsonBody
        }
    }
}