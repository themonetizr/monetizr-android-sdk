package io.monetizr.monetizrsdk.dto

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

            if (withPayment != null && withPayment.shippingAddress != null) {
                val it = withPayment.shippingAddress!!
                val info = ShippingAddressInto(it.name, it.name, it.address1, it.address2, it.locality, it.administrativeArea, it.countryCode, it.postalCode)
                jsonBody.put("shippingAddress", info.getJsonObject())
            }

            return jsonBody
        }
    }
}