package com.themonetizr.monetizrsdk.dto

import org.json.JSONObject

class Checkout {
    val subtotalPrice: Price
    val totalPrice: Price
    val totalTax: Price
    val shippingRates: ArrayList<ShippingRate>

    constructor(checkOut: JSONObject) {
        totalPrice = Price(checkOut.getJSONObject("totalPriceV2"))
        subtotalPrice = Price(checkOut.getJSONObject("subtotalPriceV2"))
        totalTax = Price(checkOut.getJSONObject("totalTaxV2"))
        shippingRates = ArrayList()

        if (checkOut.has("availableShippingRates") && checkOut.isNull("availableShippingRates") == false &&
            checkOut.getJSONObject("availableShippingRates").has("shippingRates")
        ) {
            val array = checkOut.getJSONObject("availableShippingRates").getJSONArray("shippingRates")

            for (i in 0..array.length()) {
                val obj = array.getJSONObject(i)
                val rate = ShippingRate(obj)
                this.shippingRates.add(rate)
            }
        }
    }

    public fun getPriceNoShipping(): Price {
        val sum = subtotalPrice.amount + totalTax.amount
        return Price(totalTax.currencyCode, sum, totalTax.currency)
    }
}