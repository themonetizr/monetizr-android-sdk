package com.themonetizr.monetizrsdk.dto

import org.json.JSONObject
import java.io.Serializable

class PurchaseCallbackData: Serializable {
    var orderNumber: String = ""
    var offerTag: String = ""
    var createdAt: String = ""
    var totalPrice: String = ""

    constructor(json: JSONObject) {
        if (json.has("orderNumber")) {
            this.orderNumber = json.getString("orderNumber")
        }

        if (json.has("offerTag")) {
            this.offerTag = json.getString("offerTag")
        }

        if (json.has("createdAt")) {
            this.createdAt = json.getString("createdAt")
        }

        if (json.has("totalPrice")) {
            this.totalPrice = json.getString("totalPrice")
        }
    }

}