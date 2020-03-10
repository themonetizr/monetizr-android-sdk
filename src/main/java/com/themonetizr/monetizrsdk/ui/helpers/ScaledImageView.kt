package com.themonetizr.monetizrsdk.ui.helpers

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView


class ScaledImageView(context: Context, attrs: AttributeSet) : AppCompatImageView(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val d = this.getDrawable()

        if (d != null) {
            // ceil not round - avoid thin vertical gaps along the left/right edges
            val width = View.MeasureSpec.getSize(widthMeasureSpec)
            val height =
                Math.ceil((width * d.intrinsicHeight.toFloat() / d!!.intrinsicWidth).toDouble())
                    .toInt()
            this.setMeasuredDimension(width, height)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }
}