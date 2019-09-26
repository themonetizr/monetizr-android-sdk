package io.monetizr.monetizrsdk.dto

import org.json.JSONObject

class Variant {
    private val id: String
    private val product: ProductShort
    private val title: String
    private val selectedOptions: ArrayList<Option>
      val priceV2: Price
    private val compareAtPriceV2: Price?
    private val image: String

    constructor(json: JSONObject) {
        this.id = json.getString("id")
        this.product = ProductShort(json.getJSONObject("product"))
        this.title = json.getString("title")
        this.priceV2 = Price(json.getJSONObject("priceV2"))
        if (json.has("compareAtPriceV2") && json.isNull("compareAtPriceV2") == false) {
            this.compareAtPriceV2 = Price(json.getJSONObject("compareAtPriceV2"))
        } else {
            this.compareAtPriceV2 = null
        }

        val image = json.getJSONObject("image").getString("transformedSrc")
        this.image = image

        this.selectedOptions = ArrayList()

        if (json.has("selectedOptions")) {
            val optionArray = json.getJSONArray("selectedOptions")
            for (i in 0 until optionArray.length()) {
                val optionObj = optionArray.getJSONObject(i)
                val option = Option(optionObj)
                this.selectedOptions.add(option)
            }
        }
    }

}
