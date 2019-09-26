package io.monetizr.monetizrsdk.dto

import org.json.JSONObject

class ProductShort {
    val title: String
    val description: String
    val descriptionHtml: String

    constructor(json: JSONObject) {
        this.title = json.getString("title")
        this.description = json.getString("description")
        this.descriptionHtml = json.getString("descriptionHtml")
    }

}