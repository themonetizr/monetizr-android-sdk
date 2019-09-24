package io.monetizr.monetizrsdk.dto

import org.json.JSONObject

class Product {
    private val id: String
    private val title: String
    private val description: String
    private val descriptionIos: String
    private val descriptionHtml: String

    private val button_title: String?
    private val onlineStoreUrl: String?
    private val availableForSale: Boolean
    private val options: ArrayList<Option>
    private val images: ArrayList<String>
    private val variants: ArrayList<Variant>

    constructor(json: JSONObject) {
        this.id = json.getString("id")
        this.title = json.getString("title")
        this.description = json.getString("description")
        this.descriptionIos = json.getString("description_ios")
        this.descriptionHtml = json.getString("descriptionHtml")

        if (json.has("availableForSale")) {
            this.availableForSale = json.getBoolean("availableForSale")
        } else {
            this.availableForSale = false
        }

        if (json.has("button_title")) {
            this.button_title = json.getString("button_title")
        } else {
            this.button_title = null
        }

        if (json.has("availableForSale")) {
            this.onlineStoreUrl = json.getString("availableForSale")
        } else {
            this.onlineStoreUrl = null
        }

        this.options = ArrayList()
        this.images = ArrayList()
        this.variants = ArrayList()

        if (json.has("options")) {
            val optionArray = json.getJSONArray("options")
            for (i in 0..optionArray.length()) {
                val optionObj = optionArray.getJSONObject(i)
                val option = Option(optionObj)
                this.options.add(option)
            }
        }

        if (json.has("images")) {
            val imagesArray = json.getJSONObject("images").getJSONArray("edges")
            for (i in 0..imagesArray.length()) {
                val obj = imagesArray.getJSONObject(i)
                val src = obj.getJSONObject("node").getString("transformedSrc")
                this.images.add(src)
            }
        }


        if (json.has("variants")) {
            val variantsArray = json.getJSONObject("variants").getJSONArray("edges")
            for (i in 0..variantsArray.length()) {
                val obj = variantsArray.getJSONObject(i).getJSONObject("node")
                val variant = Variant(obj)
                this.variants.add(variant)
            }
        }
    }
}