package io.monetizr.monetizrsdk.dto

import org.json.JSONObject

data class ShippingAddressInto(
    val name: String,
    val surname: String,
    val address1: String,
    val address2: String,
    val city: String,
    val province: String,
    val country: String,
    val zip: String
) {

    fun getJsonObject(): JSONObject {
        val shippingAddress = JSONObject()
        shippingAddress.put("firstName", name)
        shippingAddress.put("lastName", surname)
        shippingAddress.put("address1", address1)
        shippingAddress.put("address2", address2)
        shippingAddress.put("city", city)
        shippingAddress.put("province", province)
        shippingAddress.put("country", country)
        shippingAddress.put("zip", zip)
        return shippingAddress
    }

}

