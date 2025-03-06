package com.app.mutliple.videos.compression.trimmingvideo

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.app.mutliple.videos.compression.R


class ProgressBarViewMultiple @JvmOverloads constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr), OnRangeSeekBarMultipleListener, OnProgressVideoListener {

    private var mProgressHeight = 0
    private var mViewWidth = 0
    private val mBackgroundColor = Paint()
    private val mProgressColor = Paint()
    private var mBackgroundRect: Rect? = null
    private var mProgressRect: Rect? = null
    private fun init() {
        val lineProgress = ContextCompat.getColor(context, R.color.white)
        val lineBackground = ContextCompat.getColor(context, R.color.transparent)
        mProgressHeight =
            context.resources.getDimensionPixelOffset(R.dimen.progress_video_line_height)

        mBackgroundColor.isAntiAlias = true
        mBackgroundColor.color = lineBackground
        mProgressColor.isAntiAlias = true
        mProgressColor.color = lineProgress
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val minW = paddingLeft + paddingRight + suggestedMinimumWidth
        mViewWidth = resolveSizeAndState(minW, widthMeasureSpec, 1)
        val minH = paddingBottom + paddingTop + mProgressHeight
        val viewHeight = resolveSizeAndState(minH, heightMeasureSpec, 1)
        setMeasuredDimension(mViewWidth, viewHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawLineBackground(canvas)
        drawLineProgress(canvas)
    }

    private fun drawLineBackground(canvas: Canvas) {
        if (mBackgroundRect != null) {
            canvas.drawRect(mBackgroundRect!!, mBackgroundColor)
        }
    }

    private fun drawLineProgress(canvas: Canvas) {
        if (mProgressRect != null) {
            canvas.drawRect(mProgressRect!!, mProgressColor)
        }
    }

    private fun updateBackgroundRect(index: Int, value: Float) {
        if (mBackgroundRect == null) {
            mBackgroundRect = Rect(0, 0, mViewWidth, mProgressHeight)
        }
        val newValue = (mViewWidth * value / 100).toInt()
        mBackgroundRect = if (index == 0) {
            Rect(newValue, mBackgroundRect!!.top, mBackgroundRect!!.right, mBackgroundRect!!.bottom)
        } else {
            Rect(mBackgroundRect!!.left, mBackgroundRect!!.top, newValue, mBackgroundRect!!.bottom)
        }
        updateProgress(0, 0, 0.0f)
    }

    override fun updateProgress(time: Int, max: Int, scale: Float) {
        mProgressRect = if (scale == 0f) {
            Rect(0, mBackgroundRect!!.top, 0, mBackgroundRect!!.bottom)
        } else {
            val newValue = (mViewWidth * scale / 100).toInt()
            Rect(mBackgroundRect!!.left, mBackgroundRect!!.top, newValue, mBackgroundRect!!.bottom)
        }
        invalidate()
    }

    init {
        init()
    }


     override fun onCreate(rangeSeekBarView: RangeSeekBarViewMultiple, index: Int, value: Float) {
         updateBackgroundRect(index, value)
     }

     override fun onSeek(rangeSeekBarView: RangeSeekBarViewMultiple, index: Int, value: Float) {
         updateBackgroundRect(index, value)
     }

     override fun onSeekStart(rangeSeekBarView: RangeSeekBarViewMultiple, index: Int, value: Float) {
         updateBackgroundRect(index, value)
     }

     override fun onSeekStop(rangeSeekBarView: RangeSeekBarViewMultiple, index: Int, value: Float) {
         updateBackgroundRect(index, value)
     }

 }
