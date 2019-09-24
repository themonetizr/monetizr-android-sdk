package io.monetizr.monetizrsdk.ui.adapter

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.recyclerview.widget.RecyclerView
import android.view.animation.AccelerateDecelerateInterpolator

/**
 * Recycler view item indicator. Adds round indicators at the bottom of recycler view
 */
class ItemIndicator : RecyclerView.ItemDecoration() {

    /**
     * Active and inactive indicator colors
     */
    private val colorActive = "#FFFFFF"
    private val colorInactive = 0x33000000

    private val densityPixels = Resources.getSystem().displayMetrics.density

    /**
     * Height of the space the indicator takes up at the b
     * ottom of the view.
     */
    private val mIndicatorHeight = (densityPixels * 16).toInt()

    /**
     * Indicator stroke width.
     */
    private val mIndicatorStrokeWidth = densityPixels * 4

    /**
     * Indicator width.
     */
    private val mIndicatorItemLength = densityPixels * 4
    /**
     * Padding between indicators.
     */
    private val mIndicatorItemPadding = densityPixels * 8

    /**
     * Some more natural animation interpolation
     */
    private val mInterpolator = AccelerateDecelerateInterpolator()

    private val mPaint = Paint()

    init {

        mPaint.strokeWidth = mIndicatorStrokeWidth
        mPaint.style = Paint.Style.STROKE
        mPaint.isAntiAlias = true
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)

        val itemCount = parent.adapter!!.itemCount

        // center horizontally, calculate width and subtract half from center
        val totalLength = mIndicatorItemLength * itemCount
        val paddingBetweenItems = Math.max(0, itemCount - 1) * mIndicatorItemPadding
        val indicatorTotalWidth = totalLength + paddingBetweenItems
        val indicatorStartX = (parent.width - indicatorTotalWidth) / 2f

        // center vertically in the allotted space
        val indicatorPosY = parent.height - mIndicatorHeight / 2f

        drawInactiveIndicators(c, indicatorStartX, indicatorPosY, itemCount)

        // find active page (which should be highlighted)
        val layoutManager = parent.layoutManager as androidx.recyclerview.widget.LinearLayoutManager?
        val activePosition = layoutManager!!.findFirstVisibleItemPosition()
        if (activePosition == RecyclerView.NO_POSITION) {
            return
        }

        // find offset of active page (if the user is scrolling)
        val activeChild = layoutManager.findViewByPosition(activePosition)
        val left = activeChild!!.left
        val width = activeChild.width

        // on swipe the active item will be positioned from [-width, 0]
        // interpolate offset for smooth animation
        val progress = mInterpolator.getInterpolation(left * -1 / width.toFloat())

        drawHighlights(c, indicatorStartX, indicatorPosY, activePosition, progress)
    }

    private fun drawInactiveIndicators(c: Canvas, indicatorStartX: Float, indicatorPosY: Float, itemCount: Int) {
        mPaint.color = colorInactive

        // width of item indicator including padding
        val itemWidth = mIndicatorItemLength + mIndicatorItemPadding

        var start = indicatorStartX
        for (i in 0 until itemCount) {

            c.drawCircle(start, indicatorPosY, mIndicatorItemLength / 2f, mPaint)

            start += itemWidth
        }
    }

    private fun drawHighlights(
        c: Canvas, indicatorStartX: Float, indicatorPosY: Float,
        highlightPosition: Int, progress: Float
    ) {
        mPaint.color = Color.parseColor(colorActive)

        // width of item indicator including padding
        val itemWidth = mIndicatorItemLength + mIndicatorItemPadding

        if (progress == 0f) {
            // no swipe, draw a normal indicator
            val highlightStart = indicatorStartX + itemWidth * highlightPosition

            c.drawCircle(highlightStart, indicatorPosY, mIndicatorItemLength / 2f, mPaint)

        } else {
            val highlightStart = indicatorStartX + itemWidth * highlightPosition
            // calculate partial highlight
            val partialLength = mIndicatorItemLength * progress + mIndicatorItemPadding * progress

            c.drawCircle(highlightStart + partialLength, indicatorPosY, mIndicatorItemLength / 2f, mPaint)
        }
    }
}