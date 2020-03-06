package com.themonetizr.monetizrsdk.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;

import com.themonetizr.monetizrsdk.R;

public class SlideUpDownPanel extends LinearLayout {
    private ViewDragHelper dragHelper;
    private boolean needAnimate = false;
    private double topPercent = 0.6f;

    public SlideUpDownPanel(Context context) {
        this(context, null);
    }

    public SlideUpDownPanel(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideUpDownPanel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.dragHelper = ViewDragHelper.create(this, 1.0f, dragCallback);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (dragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (dragHelper.shouldInterceptTouchEvent(event)) {
            return true;
        }
        return super.onInterceptTouchEvent(event);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        dragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int height = getMeasuredHeight();
        int width = getMeasuredWidth();

        // top layout
        final ViewGroup topView = (ViewGroup) getChildAt(0);
        int headerHeight = (int) (height * topPercent);
        topView.layout(0, 0, width, headerHeight);
        topView.getChildAt(0).layout(0, 0, width, headerHeight);

        // bottom layout
        final ViewGroup textView = (ViewGroup) getChildAt(1);
        textView.layout(0, headerHeight, width, headerHeight + textView.getMeasuredHeight());
    }

    @SuppressWarnings("FieldCanBeLocal")
    private ViewDragHelper.Callback dragCallback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
            return child.getId() == R.id.slider_root;
        }

        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
            final int maxDragDistance = getListHeight();
            final int allHeight = SlideUpDownPanel.this.getMeasuredHeight();
            int dif = Math.abs(allHeight - maxDragDistance);
            int headerHeight = (int) (allHeight * topPercent);

            if (top > headerHeight) {
                needAnimate = true;
            } else {
                needAnimate = false;
            }

            if (maxDragDistance < allHeight) {
                if (top < dif) {
                    return dif;
                } else {
                    return top;
                }
            } else {
                if (top > -dif) {
                    return top;
                } else {
                    return -dif;
                }
            }
        }

        @Override
        public int getViewVerticalDragRange(@NonNull View child) {
            return child.getMeasuredHeight();
        }

        @Override
        public void onViewReleased(@NonNull View releasedChild, float xV, float yV) {
            super.onViewReleased(releasedChild, xV, yV);
            if (yV > 800 || needAnimate) {
                int headerHeight = (int) (getMeasuredHeight() * topPercent);
                if (dragHelper.smoothSlideViewTo(releasedChild, 0, headerHeight)) {
                    ViewCompat.postInvalidateOnAnimation(SlideUpDownPanel.this);
                }
            }
        }
    };

    private int getListHeight() {
        int result = 0;
        ViewGroup listView = (ViewGroup) ((ViewGroup) getChildAt(1)).getChildAt(0);
        for (int i = 0; i < listView.getChildCount(); i++) {
            result = result + listView.getChildAt(i).getMeasuredHeight();
        }
        return result;
    }
}

