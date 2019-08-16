package io.monetizr.monetizrsdk

import android.content.Context
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import android.util.DisplayMetrics
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Scroller


/**
 * Example used from https://github.com/jsaund/Playground
 *
 * Item snap allows to snap image in place when swiping left, it does not help with swiping right
 * What it does, centers recycler view image at the place
 */
class ItemSnapHelper : androidx.recyclerview.widget.LinearSnapHelper() {
    companion object {
        private const val MILLISECONDS_PER_INCH = 100f
        private const val MAX_SCROLL_ON_FLING_DURATION_MS = 1000
    }

    private var context: Context? = null
    private var helper: androidx.recyclerview.widget.OrientationHelper? = null
    private var scroller: Scroller? = null
    private var maxScrollDistance: Int = 0

    override fun attachToRecyclerView(recyclerView: androidx.recyclerview.widget.RecyclerView?) {
        if (recyclerView != null) {
            context = recyclerView.context
            scroller = Scroller(context, DecelerateInterpolator())
        } else {
            scroller = null
            context = null
        }
        super.attachToRecyclerView(recyclerView)
    }

    override fun findSnapView(layoutManager: androidx.recyclerview.widget.RecyclerView.LayoutManager?): View? =
        findFirstView(layoutManager, helper(layoutManager))

    override fun calculateDistanceToFinalSnap(layoutManager: androidx.recyclerview.widget.RecyclerView.LayoutManager, targetView: View): IntArray {
        val out = IntArray(2)
        out[0] = distanceToStart(targetView, helper(layoutManager))
        return out
    }

    override fun calculateScrollDistance(velocityX: Int, velocityY: Int): IntArray {
        val out = IntArray(2)
        val helper = helper ?: return out

        if (maxScrollDistance == 0) {
            maxScrollDistance = (helper.endAfterPadding - helper.startAfterPadding) / 2
        }

        scroller?.fling(0, 0, velocityX, velocityY, -maxScrollDistance, maxScrollDistance, 0, 0)
        out[0] = scroller?.finalX ?: 0
        out[1] = scroller?.finalY ?: 0
        return out
    }

    override fun createScroller(layoutManager: androidx.recyclerview.widget.RecyclerView.LayoutManager?): androidx.recyclerview.widget.RecyclerView.SmoothScroller? {
        if (layoutManager !is androidx.recyclerview.widget.RecyclerView.SmoothScroller.ScrollVectorProvider)
            return super.createScroller(layoutManager)
        val context = context ?: return null
        return object : androidx.recyclerview.widget.LinearSmoothScroller(context) {
            override fun onTargetFound(targetView: View, state: androidx.recyclerview.widget.RecyclerView.State, action: Action) {
                val snapDistance = calculateDistanceToFinalSnap(layoutManager, targetView)
                val dx = snapDistance[0]
                val dy = snapDistance[1]
                val dt = calculateTimeForDeceleration(Math.abs(dx))
                val time = Math.max(1, Math.min(MAX_SCROLL_ON_FLING_DURATION_MS, dt))
                action.update(dx, dy, time, mDecelerateInterpolator)
            }

            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float =
                MILLISECONDS_PER_INCH / displayMetrics.densityDpi
        }
    }

    private fun distanceToStart(targetView: View, helper: androidx.recyclerview.widget.OrientationHelper): Int {
        val childStart = helper.getDecoratedStart(targetView)
        val containerStart = helper.startAfterPadding
        return childStart - containerStart
    }

    private fun findFirstView(layoutManager: androidx.recyclerview.widget.RecyclerView.LayoutManager?, helper: androidx.recyclerview.widget.OrientationHelper): View? {
        if (layoutManager == null) return null

        val childCount = layoutManager.childCount
        if (childCount == 0) return null

        var absClosest = Integer.MAX_VALUE
        var closestView: View? = null
        val start = helper.startAfterPadding

        for (i in 0 until childCount) {
            val child = layoutManager.getChildAt(i)
            val childStart = helper.getDecoratedStart(child)
            val absDistanceToStart = Math.abs(childStart - start)
            if (absDistanceToStart < absClosest) {
                absClosest = absDistanceToStart
                closestView = child
            }
        }
        return closestView
    }

    private fun helper(layoutManager: androidx.recyclerview.widget.RecyclerView.LayoutManager?): androidx.recyclerview.widget.OrientationHelper {
        if (helper == null) {
            helper = androidx.recyclerview.widget.OrientationHelper.createHorizontalHelper(layoutManager)
        }
        return helper!!
    }
}