package com.themonetizr.monetizrsdk.dto

import org.json.JSONObject

class ShippingRate {
    var title: String
    var handle: String
    var price: Price

    constructor(json: JSONObject) {
        title = json.getString("title")
        handle = json.getString("handle")
        price = Price(json.getJSONObject("priceV2"))
    }
}