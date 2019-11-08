package com.themonetizr.monetizrsdk.dto

import org.json.JSONObject
import java.io.Serializable
import java.math.BigDecimal

class Price : Serializable{
    val currencyCode: String
    val amount: Float
    val currency: String

    constructor(currencyCode: String, amount: Float, currency: String) {
        this.currencyCode = currencyCode
        this.amount = amount
        this.currency = currency
    }

    constructor(json: JSONObject) {
        if (json.has("currencyCode")) {
            this.currencyCode = json.getString("currencyCode")
        } else {
            this.currencyCode = ""
        }

        if (json.has("amount")) {
            this.amount =  BigDecimal.valueOf(json.getDouble("amount")).toFloat()
        } else {
            this.amount = 0.00.toFloat()
        }

        if (json.has("currency")) {
            this.currency = json.getString("currency")
        } else {
            this.currency = ""
        }
    }

    public fun formatString(): String {
        // TODO currency formatter
        return "$currencyCode $amount"
    }
}