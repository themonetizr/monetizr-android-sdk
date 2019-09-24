package io.monetizr.monetizrsdk.dto

import org.json.JSONObject

class Price {
    private val currencyCode: String
    private val amount: Int
    private val currency: String

    constructor(json: JSONObject) {
        this.currencyCode = json.getString("currencyCode")
        this.amount = json.getInt("amount")
        this.currency = json.getString("currency")
    }

    public fun formatString(): String {
        // TODO currency formatter
        return "$currency $amount"
    }
}