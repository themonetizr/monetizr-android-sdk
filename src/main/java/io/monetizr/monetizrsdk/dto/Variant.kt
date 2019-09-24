package io.monetizr.monetizrsdk.dto

import org.json.JSONObject

class Variant {
    private val id: String
    private val product: Product
    private val title: String
    private val selectedOptions: ArrayList<Option>
    private val priceV2: Price
    private val compareAtPriceV2: Price
    private val image: String

    constructor(json: JSONObject) {
        this.id = json.getString("id")
        this.product = Product(json.getJSONObject("product"))
        this.title = json.getString("title")
        this.priceV2 = Price(json.getJSONObject("priceV2"))
        this.compareAtPriceV2 = Price(json.getJSONObject("compareAtPriceV2"))

        val image = json.getJSONObject("image").getString("transformedSrc")
        this.image = image

        this.selectedOptions = ArrayList()

        if (json.has("selectedOptions")) {
            val optionArray = json.getJSONArray("selectedOptions")
            for (i in 0..optionArray.length()) {
                val optionObj = optionArray.getJSONObject(i)
                val option = Option(optionObj)
                this.selectedOptions.add(option)
            }
        }
    }

}
