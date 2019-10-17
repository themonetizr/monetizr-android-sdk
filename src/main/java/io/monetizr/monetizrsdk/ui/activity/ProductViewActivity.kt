package io.monetizr.monetizrsdk.ui.activity

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
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
        val pos = intent.getIntExtra(Parameters.PRODUCT_IMAGE_POS, 0);
        initImageAdapter(images,pos)
        closeButtonView.setOnClickListener { finish() }

        hideStatusBar()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        hideStatusBar()
    }

    override fun onResume() {
        super.onResume()
        hideStatusBar()
    }

    private fun initImageAdapter(photos: ArrayList<String>, pos: Int) {
        val imageGalleryAdapter = ImageGalleryAdapter(this, photos)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        productImagesView.setHasFixedSize(true)
        productImagesView.layoutManager = layoutManager
        ItemSnapHelper().attachToRecyclerView(productImagesView)
        productImagesView.addItemDecoration(ItemIndicator())
        productImagesView.adapter = imageGalleryAdapter
        layoutManager.scrollToPosition(pos)
    }

    private fun hideStatusBar() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

    companion object {
        fun start(context: Context, images: ArrayList<String>, position: Int) {
            val starter = Intent(context, ProductViewActivity::class.java)
            starter.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            starter.putExtra(Parameters.PRODUCT_IMAGES, images)
            starter.putExtra(Parameters.PRODUCT_IMAGE_POS, position)
            context.startActivity(starter)
        }
    }

}
