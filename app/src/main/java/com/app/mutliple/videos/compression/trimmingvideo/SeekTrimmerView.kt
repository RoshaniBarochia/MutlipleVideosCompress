package com.app.mutliple.videos.compression.trimmingvideo

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.app.mutliple.videos.compression.R
import java.io.File


class SeekTrimmerView @JvmOverloads constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int = 0) : BaseSeekTrimmerView(context, attrs, defStyleAttr) {

    private  val TAG = "VideoTrimmerView"
    private fun stringForTime(timeMs: Int): String {
        val totalSeconds = timeMs / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        val timeFormatter = java.util.Formatter()
        return if (hours > 0)
            timeFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        else
            timeFormatter.format("%02d:%02d", minutes, seconds).toString()
    }
    override fun initRootView() {
        LayoutInflater.from(context).inflate(R.layout.seek_trimmer, this, true)
    }

    override fun getTimeLineView(): TimeLineView = findViewById(R.id.timeLineView)

    override fun getProgressBarView(): ProgressBarViewMultiple = findViewById(R.id.timeVideoView)

    override fun getTimeInfoContainer(): View = findViewById(R.id.timeTextContainer)

    override fun getPlayView(): View = findViewById(R.id.playIndicatorView)
    fun getBackView(): View = findViewById(R.id.backgroundView)

    override fun getRangeSeekBarView(): RangeSeekBarViewMultiple = findViewById(R.id.rangeSeekBarView)

    override fun onRangeUpdated(startTimeInMs: Int,  endTimeInMs: Int) {
        val seconds = "sec"
        Log.d(TAG, "onRangeUpdated() called with: startTimeInMs = $startTimeInMs, endTimeInMs = $endTimeInMs")
       // trimTimeRangeTextView.text = "${stringForTime(startTimeInMs)} $seconds - ${stringForTime(endTimeInMs)} $seconds"
    }

    override fun onVideoPlaybackReachingTime(timeInMs: Int) {
        val seconds = "sec"
        //playbackTimeTextView.text = "${stringForTime(timeInMs)} $seconds"
    }

    override fun onGotVideoFileSize(videoFileSize: Long) {
        Log.d("File size video ::",videoFileSize.toString());
        //videoFileSizeTextView.text = Formatter.formatShortFileSize(context, videoFileSize)
    }
    fun callTrimmingView(srcFile : File ,dstFile : File,startPosition: Int,endPosition: Int,duration : Long){
        initiateTrimming(srcFile, dstFile, startPosition, endPosition,duration )
    }

}
