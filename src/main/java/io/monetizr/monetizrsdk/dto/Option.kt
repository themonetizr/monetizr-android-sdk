package io.monetizr.monetizrsdk.dto

import org.json.JSONObject

class Option {
    private val name: String
    private val values: ArrayList<String>

    constructor(json: JSONObject) {
        this.name = json.getString("name")
        this.values = ArrayList()
        val array = json.getJSONArray("values")

        for (i in 0..array.length()) {
            this.values.add(array.getString(i))
        }
    }
}
