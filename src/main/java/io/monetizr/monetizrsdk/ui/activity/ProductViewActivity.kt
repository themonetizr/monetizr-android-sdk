package io.monetizr.monetizrsdk.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import io.monetizr.monetizrsdk.R
import io.monetizr.monetizrsdk.misc.Parameters
import io.monetizr.monetizrsdk.ui.adapter.ImageGalleryAdapter
import io.monetizr.monetizrsdk.ui.adapter.ItemIndicator
import io.monetizr.monetizrsdk.ui.adapter.ItemSnapHelper
import kotlinx.android.synthetic.main.activity_product.*

class ProductViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_view)
        val images = intent.getStringArrayListExtra(Parameters.PRODUCT_IMAGES)
        initImageAdapter(images)
        closeButtonView.setOnClickListener { finish() }
    }

    private fun initImageAdapter(photos: ArrayList<String>) {
        val imageGalleryAdapter = ImageGalleryAdapter(this, photos)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        productImagesView.setHasFixedSize(true)
        productImagesView.layoutManager = layoutManager
        ItemSnapHelper().attachToRecyclerView(productImagesView)
        productImagesView.addItemDecoration(ItemIndicator())
        productImagesView.adapter = imageGalleryAdapter
    }

    companion object {
        fun start(context: Context, images: ArrayList<String>) {
            val starter = Intent(context, ProductActivity::class.java)
            starter.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            starter.putExtra(Parameters.PRODUCT_IMAGES, images)
            context.startActivity(starter)
        }
    }

}
