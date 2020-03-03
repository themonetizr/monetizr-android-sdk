package com.themonetizr.monetizrsdk.dto

import org.json.JSONObject
import java.io.Serializable

class ShippingAddress: Serializable {
    val firstName: String
    val lastName: String
    val address1: String
    val address2: String
    val city: String
    val country: String
    val zip: String
    val province: String
    val email: String

    constructor(json: JSONObject) {
        this.firstName = json.getString("firstName")
        this.lastName = json.getString("lastName")
        this.address1 = json.getString("address1")
        this.address2 = json.getString("address2")
        this.city = json.getString("city")
        this.country = json.getString("country")
        this.zip = json.getString("zip")
        this.province = json.getString("province")
        this.email = json.getString("email")
    }

    fun getJsonObject(): JSONObject {
        val jsonBody = JSONObject()

        jsonBody.put("firstName", firstName)
        jsonBody.put("lastName", lastName)
        jsonBody.put("address1", address1)
        jsonBody.put("address2", address2)
        jsonBody.put("city", city)
        jsonBody.put("country", country)
        jsonBody.put("zip", zip)
        jsonBody.put("province", province)
        return jsonBody
    }
}