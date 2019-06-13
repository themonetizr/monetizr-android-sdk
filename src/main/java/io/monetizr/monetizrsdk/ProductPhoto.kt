package io.monetizr.monetizrsdk

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONArray

/**
 * Additional class to store product photos and provide them for image adapter inside recycler view
 */
@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
data class ProductPhoto(val url: String) : Parcelable {

    constructor(parcel: Parcel) : this(parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(url)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ProductPhoto> {
        override fun createFromParcel(parcel: Parcel): ProductPhoto {
            return ProductPhoto(parcel)
        }

        override fun newArray(size: Int): Array<ProductPhoto?> {
            return arrayOfNulls(size)
        }

        fun getProductPhotos(images: JSONArray): ArrayList<ProductPhoto> {

            val imageUrls = ArrayList<ProductPhoto>()

            for (i in 0 until images.length()) {
                val image = images.getJSONObject(i).getJSONObject("node")
                imageUrls.add(ProductPhoto(image.getString("transformedSrc")))
            }

            return imageUrls

        }
    }
}