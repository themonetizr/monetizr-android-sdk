package io.monetizr.monetizrsdk.dto

import org.json.JSONObject

class ShippingRate {
    var title: String
    var handle: String
    var price: ArrayList<Price>

    constructor(json: JSONObject) {
        title = json.getString("title")
        handle = json.getString("handle")
        price = ArrayList()

        if (json.has("price")) {
            val array = json.getJSONArray("price")
            for (i in 0..array.length()) {
                val obj = array.getJSONObject(i)
                val price = Price(obj)
                this.price.add(price)
            }
        }
    }

    public fun firstPrice(): Price? {
        return if (price.isEmpty() == false) {
            price[0]
        } else {
            null
        }
    }
}