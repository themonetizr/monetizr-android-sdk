package com.themonetizr.monetizrsdk.dto

import org.json.JSONObject

class Price {
    val currencyCode: String
    val amount: Int
    val currency: String

    constructor(currencyCode: String, amount: Int, currency: String) {
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
            this.amount = json.getInt("amount")
        } else {
            this.amount = 0
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