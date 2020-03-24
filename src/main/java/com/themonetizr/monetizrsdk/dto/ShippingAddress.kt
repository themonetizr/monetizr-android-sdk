package com.themonetizr.monetizrsdk.dto

import org.json.JSONObject
import java.io.Serializable

class ShippingAddress: Serializable {
    var firstName: String = ""
    var lastName: String = ""
    var address1: String = ""
    var address2: String = ""
    var city: String = ""
    var country: String = ""
    var zip: String = ""
    var province: String = ""
    var email: String = ""

    constructor(json: JSONObject) {
        if (json.has("firstName")) {
            this.firstName = json.getString("firstName")
        }

        if(json.has("lastName")) {
            this.lastName = json.getString("lastName")
        }

        if (json.has("address1")) {
            this.address1 = json.getString("address1")
        }

        if (json.has("address2")) {
            this.address2 = json.getString("address2")
        }

        if (json.has("city")) {
            this.city = json.getString("city")
        }

        if (json.has("country")) {
            this.country = json.getString("country")
        }

        if (json.has("zip")) {
            this.zip = json.getString("zip")
        }

        if (json.has("province")) {
            this.province = json.getString("province")
        }

        if (json.has("email")) {
            this.email = json.getString("email")
        }
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