package com.themonetizr.monetizrsdk.dto

import org.json.JSONObject
import java.util.*

class CheckoutBody {

    companion object {
        fun createBody(variant: JSONObject, productTag: String, address: ShippingAddress?): JSONObject {
            val language = Locale.getDefault().displayLanguage
            val jsonBody = JSONObject()

            jsonBody.put("product_handle", productTag)
            jsonBody.put("variantId", variant.getString("id"))
            jsonBody.put("language", language)
            jsonBody.put("quantity", 1)

            if (address != null) {
                jsonBody.put("shippingAddress", address.getJsonObject())
            }

            return jsonBody
        }

        fun createUpdateBody(checkoutInfo: JSONObject, productTag: String, shippingAddress: ShippingAddress?, chosenShippingRate: ShippingRate): JSONObject {
            val jsonBody = JSONObject()

            val billingAddress = JSONObject()

            billingAddress.put("firstName", shippingAddress?.firstName)
            billingAddress.put("lastName", shippingAddress?.lastName)
            billingAddress.put("address1", shippingAddress?.address1)
            billingAddress.put("address2", shippingAddress?.address2)
            billingAddress.put("city", shippingAddress?.city)
            billingAddress.put("province", shippingAddress?.province)
            billingAddress.put("country", shippingAddress?.country)
            billingAddress.put("zip", shippingAddress?.zip)

            jsonBody.put("checkoutId", checkoutInfo.getString("id"))
            jsonBody.put("product_handle", productTag)
            jsonBody.put("billingAddress", billingAddress)
            jsonBody.put("shippingAddress", shippingAddress?.getJsonObject())
            jsonBody.put("shippingRateHandle", chosenShippingRate.handle)
            jsonBody.put("email", shippingAddress?.email)

            return jsonBody
        }

        fun createClaimBody(checkoutInfo: JSONObject, playerId: String?): JSONObject {
            val jsonBody = JSONObject()
            jsonBody.put("checkoutId", checkoutInfo.getString("id"))
            jsonBody.put("player_id", playerId)

            return jsonBody
        }
    }
}