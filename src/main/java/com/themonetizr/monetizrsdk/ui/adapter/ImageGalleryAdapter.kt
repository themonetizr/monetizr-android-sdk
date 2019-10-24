package com.themonetizr.monetizrsdk.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.themonetizr.monetizrsdk.R
import com.themonetizr.monetizrsdk.ui.activity.ProductViewActivity

class ImageGalleryAdapter(private val context: Context, private val productPhotos: ArrayList<String>) : RecyclerView.Adapter<ImageGalleryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val photoView = inflater.inflate(R.layout.item_image, parent, false)
        return ViewHolder(photoView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val productPhoto = productPhotos[position]
        holder.itemView.setOnClickListener(holder)
        val imageView = holder.photoImageView

        val factory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()

        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()

        Glide.with(context)
            .load(productPhoto)
            .transition(DrawableTransitionOptions.withCrossFade(factory))
            .placeholder(circularProgressDrawable)
            .error(R.drawable.ic_error)
            .fitCenter()
            .into(imageView)
    }

    override fun getItemCount(): Int {
        return productPhotos.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        override fun onClick(p0: View?) {
            ProductViewActivity.start(context, productPhotos, adapterPosition)
        }

        var photoImageView: ImageView = itemView.findViewById(R.id.iv_photo)
    }
}
