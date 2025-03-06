package com.app.mutliple.videos.compression.trimmingvideo

import android.content.Context
import android.text.format.Formatter
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.app.mutliple.videos.compression.R
import com.google.android.exoplayer2.ui.PlayerView

class VideoTrimmerView @JvmOverloads constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int = 0) : BaseVideoTrimmerView(context, attrs, defStyleAttr) {

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
        LayoutInflater.from(context).inflate(R.layout.video_trimmer, this, true)
    }

    override fun getTimeLineView(): TimeLineView = findViewById(R.id.timeLineView)

    override fun getProgressBarView(): ProgressBarView = findViewById(R.id.timeVideoView)

    override fun getTimeInfoContainer(): View = findViewById(R.id.timeTextContainer)

    override fun getPlayView(): View = findViewById(R.id.playIndicatorView)
    /*override fun getSeekbarView(): SeekBar {
        TODO("Not yet implemented")
    }*/

    override fun getVideoView(): PlayerView = findViewById(R.id.player_view_lib)


    override fun getVideoViewContainer(): View = findViewById(R.id.videoViewContainer)

    override fun getRangeSeekBarView(): RangeSeekBarView = findViewById(R.id.rangeSeekBarView)

    override fun onRangeUpdated(startTimeInMs: Int,  endTimeInMs: Int) {
        val seconds = "sec"
        Log.d(TAG, "onRangeUpdated() called with: startTimeInMs = $startTimeInMs, endTimeInMs = $endTimeInMs")
        findViewById<TextView>(R.id.trimTimeRangeTextView).text = "${stringForTime(startTimeInMs)} $seconds - ${stringForTime(endTimeInMs)} $seconds"
    }

    override fun onVideoPlaybackReachingTime(timeInMs: Int) {
        val seconds = "sec"
        findViewById<TextView>(R.id.playbackTimeTextView).text = "${stringForTime(timeInMs)} $seconds"
    }

    override fun onGotVideoFileSize(videoFileSize: Long) {
        Log.d("File size video ::",videoFileSize.toString());
        findViewById<TextView>(R.id.videoFileSizeTextView).text = Formatter.formatShortFileSize(context, videoFileSize)
    }
    fun callTrimmingView(){
        initiateTrimming()
    }

}
