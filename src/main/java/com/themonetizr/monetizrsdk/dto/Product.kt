package com.themonetizr.monetizrsdk.dto

import org.json.JSONObject

class Product {
    val id: String
    val title: String
    val description: String
    val descriptionIos: String
    val descriptionHtml: String
    val maxOptionsLevel: Int

    val buttonTitle: String?
    val claimable: Boolean?
    val onlineStoreUrl: String?
    val availableForSale: Boolean
    val options: ArrayList<Option>
    val images: ArrayList<String>
    val variants: ArrayList<Variant>
    val variantHierarchy: HashSet<HierarchyVariant>


    constructor() {
        id = ""
        title = ""
        description = ""
        descriptionIos = ""
        descriptionHtml = ""
        maxOptionsLevel = 0

        buttonTitle = ""
        claimable =  false
        onlineStoreUrl = ""
        availableForSale = false
        options = ArrayList()
        images = ArrayList()
        variants = ArrayList()
        variantHierarchy = HashSet()
    }

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
            this.buttonTitle = json.getString("button_title")
        } else {
            this.buttonTitle = null
        }

        if (json.has("claimable")) {
            this.claimable = json.getBoolean("claimable")
        } else {
            this.claimable = false
        }

        if (json.has("onlineStoreUrl")) {
            this.onlineStoreUrl = json.getString("onlineStoreUrl")
        } else {
            this.onlineStoreUrl = null
        }

        this.options = ArrayList()
        this.images = ArrayList()
        this.variants = ArrayList()

        if (json.has("options")) {
            val optionArray = json.getJSONArray("options")
            for (i in 0 until optionArray.length()) {
                val optionObj = optionArray.getJSONObject(i)
                val option = Option(optionObj)
                this.options.add(option)
            }
        }

        if (json.has("images")) {
            val imagesArray = json.getJSONObject("images").getJSONArray("edges")
            for (i in 0 until imagesArray.length()) {
                val obj = imagesArray.getJSONObject(i)
                val src = obj.getJSONObject("node").getString("transformedSrc")
                this.images.add(src)
            }
        }

        if (json.has("variants")) {
            val variantsArray = json.getJSONObject("variants").getJSONArray("edges")
            for (i in 0 until variantsArray.length()) {
                val obj = variantsArray.getJSONObject(i).getJSONObject("node")
                val variant = Variant(obj)
                this.variants.add(variant)
            }
        }

        this.variantHierarchy = HierarchyVariant.buildStructure(variants)
        this.maxOptionsLevel = HierarchyVariant.maxOptionsLevel
    }

    fun getFirstVariant(): Variant? {
        return if (variants.size > 0) {
            variants[0]
        } else {
            null
        }
    }


}