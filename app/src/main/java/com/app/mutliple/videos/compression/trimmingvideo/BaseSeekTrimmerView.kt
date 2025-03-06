package com.app.mutliple.videos.compression.trimmingvideo

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.OpenableColumns
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.annotation.UiThread
import com.google.android.exoplayer2.*
import com.speakapp.app.trimmingvideo.UiThreadExecutor
import java.io.File
import java.lang.ref.WeakReference


abstract class BaseSeekTrimmerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    // private var mHolderTopView: SeekBar? = null
    lateinit var rangeSeekBarView: RangeSeekBarViewMultiple
    lateinit var playView: View

    // private val videoViewContainer: View
    lateinit var timeInfoContainer: View

    //private val videoView: VideoView
    lateinit var timeLineView: TimeLineView
    lateinit var mVideoProgressIndicator: ProgressBarViewMultiple
    private var src: Uri? = null
    private var dstFile: File? = null
    private var maxDurationInMs: Int = 0
    private var listeners = ArrayList<OnProgressVideoListener>()
    private var videoTrimmingListener: VideoTrimmingListenerMultiple? = null
    private var duration = 0
    private var timeVideo = 0
    private var startPosition = 0
    private var endPosition = 0
    private var originSizeFile: Long = 0
    private var resetSeekBar = true
    private var isFirstTime: Boolean = false
    private val messageHandler = MessageHandler(this@BaseSeekTrimmerView)
   /* private var start : Long =0L
    private var end : Long =0L*/

    init {
        setUp()
    }
    private fun setUp(){
        initRootView()
        rangeSeekBarView = obtainRangeSeekBarView()

        playView = obtainPlayView()
        timeInfoContainer = obtainTimeInfoContainer()
        timeLineView = obtainTimeLineView()
        mVideoProgressIndicator = obtainProgressBarView()
        setUpMargins()
        setUpListeners()
    }

    abstract fun initRootView()

    abstract fun obtainTimeLineView(): TimeLineView

    abstract fun obtainTimeInfoContainer(): View

    abstract fun obtainPlayView(): View

    abstract fun obtainRangeSeekBarView(): RangeSeekBarViewMultiple

    abstract fun onRangeUpdated(startTimeInMs: Int, endTimeInMs: Int)

    /**occurs during playback, to tell that you've reached a specific time in the video*/
    abstract fun onVideoPlaybackReachingTime(timeInMs: Int)

    abstract fun onGotVideoFileSize(videoFileSize: Long)

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpListeners() {
        listeners.add(mVideoProgressIndicator)
        listeners.add(object : OnProgressVideoListener {
            override fun updateProgress(time: Int, max: Int, scale: Float) {
                updateVideoProgress(time)
                //Log.d(TAG, "setProgressBarPosition progress: $time")
            }
        })

      /*  val gestureDetector = GestureDetector(context,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    onClickVideoPlayPause()
                    return true
                }
            }
        )*/



        rangeSeekBarView.addOnRangeSeekBarListener(object : OnRangeSeekBarMultipleListener {
            override fun onCreate(rangeSeekBarView: RangeSeekBarViewMultiple, index: Int, value: Float) {

            }

            override fun onSeek(rangeSeekBarView: RangeSeekBarViewMultiple, index: Int, value: Float) {
                onSeekThumbs(index, value)
            }

            override fun onSeekStart(rangeSeekBarView: RangeSeekBarViewMultiple, index: Int, value: Float) {
                // Do nothing

            }

            override fun onSeekStop(rangeSeekBarView: RangeSeekBarViewMultiple, index: Int, value: Float) {
                onStopSeekThumbs()
            }
        })
        rangeSeekBarView.addOnRangeSeekBarListener(mVideoProgressIndicator)
        Log.d(TAG, "setProgressBarPosition last: $duration")
        /*  mHolderTopView!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
              override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                  onPlayerIndicatorSeekChanged(progress, fromUser)
              }

              override fun onStartTrackingTouch(seekBar: SeekBar) {
                  onPlayerIndicatorSeekStart()
              }

              override fun onStopTrackingTouch(seekBar: SeekBar) {
                  onPlayerIndicatorSeekStop(seekBar)
              }
          })*/
       // initPlayer()
    }

   // var gpuPlayerView: GPUPlayerView? = null
    private var videoPlayer: ExoPlayer? = null



    private fun setUpMargins() {
        val marge = rangeSeekBarView.thumbWidth
        var lp: MarginLayoutParams = timeLineView.layoutParams as MarginLayoutParams
        lp.setMargins(marge, lp.topMargin, marge, lp.bottomMargin)
        timeLineView.layoutParams = lp
        lp = mVideoProgressIndicator.layoutParams as MarginLayoutParams
        lp.setMargins(marge, 0, marge, 0)
        mVideoProgressIndicator.layoutParams = lp
        mVideoProgressIndicator.requestLayout()


    }

    @Suppress("unused")
    @UiThread
    fun initiateTrimming(srcFile : File ,dstFile : File,startPosition1: Int,endPosition1: Int,duration : Long) {
        
        src= Uri.fromFile(srcFile)
        Log.d(TAG, "initiateTrimming src ::: $src")
        pauseVideo(true)
        val timeVideo =endPosition-startPosition
        var endPosition=endPosition1

        //if(src!!.path.equals(null) || src!!.path.equals(""))
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(context, src)
        val metadataKeyDuration =
            java.lang.Long.parseLong(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toString())
        if (timeVideo < MIN_TIME_FRAME) {
            if (metadataKeyDuration - endPosition > MIN_TIME_FRAME - timeVideo) {
                endPosition += MIN_TIME_FRAME - timeVideo
            } else if (startPosition > MIN_TIME_FRAME - timeVideo) {
                startPosition -= MIN_TIME_FRAME - timeVideo
            }
        }
        Log.d(TAG, "initiateTrimming duration ::: $duration")
        //notify that video trimming started
        if (videoTrimmingListener != null)
            videoTrimmingListener?.onTrimStarted(startPosition1.toLong(), endPosition.toLong())
        BackgroundExecutor.execute(
            object : BackgroundExecutor.Task(null, 500L, null) {
                override fun execute() {
                    try {
                        if(videoTrimmingListener !=null) {
                            TrimVideoUtilsMultiple.startTrim(
                                context,
                                src!!,
                                dstFile,
                                startPosition1.toLong(),
                                endPosition.toLong(),
                                duration,
                                videoTrimmingListener!!
                            )
                        }
                    } catch (e: Throwable) {
                        Thread.getDefaultUncaughtExceptionHandler()?.uncaughtException(
                            Thread.currentThread(), e
                        )
                    }
                }
            }
        )
    }

    fun onClickVideoPlayPause() {
        if (videoPlayer?.isPlaying == true) {
            messageHandler.removeMessages(SHOW_PROGRESS)
            pauseVideo()
        } else {
            playView.visibility = View.GONE
            if (resetSeekBar) {
                resetSeekBar = false
                videoPlayer?.seekTo(startPosition.toLong())
            }
            messageHandler.sendEmptyMessage(SHOW_PROGRESS)
            videoPlayer?.playWhenReady = true
        }
    }

    @UiThread
    private fun onVideoPrepared(start: Long, end: Long,videoPlayer: ExoPlayer) {
        Log.d(TAG, "onVideoPrepared: $start === $end")
        this.videoPlayer=videoPlayer
        /*this.start=start
        this.end=end*/
        Handler(Looper.getMainLooper()).postDelayed({
            playView.visibility = View.VISIBLE
            if (videoPlayer != null) {
                videoPlayer.playWhenReady = false
                duration = videoPlayer.duration.toInt()
                Log.d(TAG, "onVideoPrepared:duration $duration")
                if (end == 0L) {
                    setSeekBarPosition(start, duration.toLong(),0L)
                    onRangeUpdated(start.toInt(), (duration))
                } else {
                    setSeekBarPosition(start, end)
                    onRangeUpdated(start.toInt(), (end).toInt())
                }

                if (endPosition < 1000) {
                    endPosition = 1000
                }
                Log.d(TAG, "onSeekThumbs 2: $startPosition = $endPosition")

                onVideoPlaybackReachingTime(start.toInt())
                //  mediaPlayer=mp
                if (videoTrimmingListener != null)
                    videoTrimmingListener?.onVideoPrepared()
            }
        }, 1000)

    }

    fun setSeekBarPosition(start: Long, end: Long, select: Long  = -1) {
        Log.d(TAG, "setSeekBarPosition: called $select")
        if (duration >= maxDurationInMs) {
           /* startPosition = duration / 2 - maxDurationInMs / 2
            endPosition = duration / 2 + maxDurationInMs / 2*/
            startPosition=start.toInt()
            endPosition=end.toInt()
            rangeSeekBarView.setThumbValue(0, startPosition * 100f / duration)
            rangeSeekBarView.setThumbValue(1, endPosition * 100f / duration)

        } else {
           /* startPosition = 0
            endPosition = duration
             rangeSeekBarView.setThumbValue(0, startPosition * 100f / duration)
             rangeSeekBarView.setThumbValue(1, endPosition * 100f / duration)*/
        }
       /* if (duration >= maxDurationInMs) {
            startPosition=start.toInt()
            endPosition=end.toInt()
            rangeSeekBarView.setThumbValueForTrim(0, start * 100f / duration)
            rangeSeekBarView.setThumbValueForTrim(1, end * 100f / duration)
        } else {
            *//*rangeSeekBarView.setThumbValueForTrim(0, start * 100f / duration)
            rangeSeekBarView.setThumbValueForTrim(1, end * 100f / duration)*//*
        }*/
        Log.d(TAG, "onVideoPrepared:duration SEEK :: $duration")
        videoPlayer?.seekTo(start)
        setProgressBarPosition(startPosition)
        timeVideo = duration
        rangeSeekBarView.initMaxWidth(isFirstTime)
        if (duration >= maxDurationInMs) {
            /*startPosition=start.toInt()
            endPosition=end.toInt()
            rangeSeekBarView.setThumbValue(0, start * 100f / duration)
            rangeSeekBarView.setThumbValue(1, end * 100f / duration)*/
        } else {
            rangeSeekBarView.setThumbValueForTrim(0, start * 100f / duration)
            rangeSeekBarView.setThumbValueForTrim(1, end * 100f / duration)
        }

    }

    private fun onSeekThumbs(index: Int, value: Float) {
        when (index) {
            RangeSeekBarView.ThumbType.LEFT.index -> {
                startPosition = (duration * value / 100L).toInt()
                videoPlayer?.seekTo(startPosition.toLong())
            }
            RangeSeekBarView.ThumbType.RIGHT.index -> {
                endPosition = (duration * value / 100L).toInt()
            }
        }

        Log.d(TAG, "onSeekThumbs: $startPosition = $endPosition")
        if (videoTrimmingListener != null)
            videoTrimmingListener?.changeThumbPosition(startPosition.toLong(),endPosition.toLong())
        if (!isFirstTime) {
            setProgressBarPosition(startPosition)
            onRangeUpdated(startPosition, endPosition)
        } else {
            isFirstTime = false
        }
        timeVideo = endPosition - startPosition
    }
   /* fun changeStartEndValue(): IntArray {
        val data = IntArray(2)
        data[0]=startPosition
        data[1]=endPosition
        return data
    }*/

    private fun onStopSeekThumbs() {
        messageHandler.removeMessages(SHOW_PROGRESS)
        pauseVideo()
    }

    fun onVideoCompleted() {
        isVideoEnded = true
        playView.visibility = View.VISIBLE
        videoPlayer?.seekTo(startPosition.toLong())
        videoPlayer?.playWhenReady = false
    }

    private fun notifyProgressUpdate(all: Boolean) {
        if (duration == 0) return
        if (videoPlayer == null) return
        val position = videoPlayer?.currentPosition
        if(position !=null) {
            if (all)
                for (item in listeners)
                    item.updateProgress(position.toInt(), duration, position * 100f / duration)
            else
                listeners[1].updateProgress(position.toInt(), duration, position * 100f / duration)
        }
    }

    private fun updateVideoProgress(time: Int) {
        if (time >= endPosition) {
            messageHandler.removeMessages(SHOW_PROGRESS)
            pauseVideo()
            resetSeekBar = true
            return
        }
        if (obtainPlayView() != null) {
            // use long to avoid overflow

            setProgressBarPosition(time)
        }
        onVideoPlaybackReachingTime(time)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun pauseVideo(isFromTrim : Boolean = false) {
        if(isFromTrim){
            playView.visibility = View.GONE
        }else
            playView.visibility = View.VISIBLE
        videoPlayer?.playWhenReady = false
    }

    private fun setProgressBarPosition(position: Int) {
       // Log.d(TAG, "setProgressBarPosition: $position")

        if (duration > 0) {
            val pos = 100f * position / duration
            // mHolderTopView!!.progress = pos.toInt()
            messageHandler.removeMessages(SHOW_PROGRESS)
            messageHandler.sendEmptyMessage(SHOW_PROGRESS)
            mVideoProgressIndicator.updateProgress(position, duration, pos)
            mVideoProgressIndicator.visibility = VISIBLE
           // Log.d(TAG, "setProgressBarPosition: $pos")
        }
    }

    /**
     * Set video information visibility.
     * For now this is for debugging
     *
     * @param visible whether or not the videoInformation will be visible
     */
    fun setVideoInformationVisibility(visible: Boolean) {
        timeInfoContainer.visibility = if (visible) View.VISIBLE else View.GONE
    }

    /**
     * Listener for some [VideoView] events
     *
     * @param onK4LVideoListener interface for events
     */
    fun setOnK4LVideoListener(onK4LVideoListener: VideoTrimmingListenerMultiple) {
        this.videoTrimmingListener = onK4LVideoListener
    }

    fun setDestinationFile(dst: File) {
        this.dstFile = dst
    }

   /* override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        invalidate()
    }*/

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        //Cancel all current operations
        BackgroundExecutor.cancelAll("", true)
        UiThreadExecutor.cancelAll("")
        if (videoPlayer != null) {
            videoPlayer?.playWhenReady = false
            videoPlayer?.release()
            videoPlayer = null
        }
    }

    /**
     * Set the maximum duration of the trimmed video.
     * The trimmer interface wont allow the user to set duration longer than maxDuration
     */
    fun setMaxDurationInMs(maxDurationInMs: Int) {
        this.maxDurationInMs = maxDurationInMs
    }

    fun setFirstRun(check: Boolean) {
        rangeSeekBarView.setFirstRun(check)
    }

    fun isFirstTime(isFirst: Boolean) {
        isFirstTime = isFirst
    }
/*
    fun hideView() {
        timeLineView.visibility = GONE
        rangeSeekBarView.visibility = GONE
        timeInfoContainer.visibility = GONE
        mVideoProgressIndicator.visibility = GONE
    }

    fun showView() {
        timeLineView.visibility = VISIBLE
        rangeSeekBarView.visibility = VISIBLE
        timeInfoContainer.visibility = VISIBLE
        mVideoProgressIndicator.visibility = VISIBLE
    }*/

    fun setVideoURI(videoURI: Uri, start: Long, end: Long,videoPlayer: ExoPlayer) {
        src = videoURI
        if (videoURI.scheme == "file") {
            val file = File(videoURI.path.toString())
            originSizeFile = file.length()
            onGotVideoFileSize(originSizeFile)
        } else if (videoURI.scheme == "content") {
            if (originSizeFile == 0L) {
                val cursor = context.contentResolver.query(videoURI, null, null, null, null)
                if (cursor != null) {
                    Log.d("File size cursor:: ", cursor.columnCount.toString())
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    cursor.moveToFirst()
                    originSizeFile = cursor.getLong(sizeIndex)
                    cursor.close()
                    onGotVideoFileSize(originSizeFile)
                }
            }
        }
        isVideoEnded = false
        onVideoPrepared(start, end,videoPlayer)
        //buildMediaSource(src!!, start, end)

        Log.d(TAG, "setVideoURI: timelineview" + src)

        timeLineView.setVideo(src!!)


    }

    var isVideoEnded: Boolean = false
   /* open fun buildMediaSource(mUri: Uri, start: Long, end: Long) {
        try {
            val dataSourceFactory = DefaultDataSourceFactory(
                context,
                Util.getUserAgent(context, "${context.resources.getString(R.string.app_name)}")
            )
            val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(buildMediaItemMP4(mUri))

            videoPlayer = SimpleExoPlayer.Builder(context).build().apply {
                setMediaSource(videoSource)
            }
            videoPlayer?.prepare()
            videoPlayer?.setThrowsWhenUsingWrongThread(false)
            videoView.layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            videoView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
            videoView.player=videoPlayer
            videoPlayer?.playWhenReady = false
            videoPlayer?.addListener(object : Player.EventListener {
                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_ENDED -> {
                            Log.v("onPlayerStateChanged: ", "Video ended.")
                            // playView.setVisibility(VISIBLE)

                            isVideoEnded = true
                            onVideoCompleted()
                        }
                        Player.STATE_READY -> {
                            isVideoEnded = false
                            // startProgress()
                            // playView.setVisibility(if (videoPlayer!!.playWhenReady) GONE else VISIBLE)
                            Log.v("onPlayerStateChanged: ", " Ready to play.")
                        }
                        else -> {
                            Log.v("onPlayerStateChanged: ", " else part.")
                        }
                    }

                }
            })
            val metaRetriever = MediaMetadataRetriever()
            metaRetriever.setDataSource(context, mUri)
            val rotation =
                metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)

            *//* if (rotation != null && rotation == "0") {
                 gpuPlayerView!!.setPlayerScaleType(PlayerScaleType.RESIZE_FIT_WIDTH)
             } else gpuPlayerView!!.setPlayerScaleType(PlayerScaleType.RESIZE_NONE)*//*



        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun buildMediaItemMP4(source: Uri): MediaItem {
        return MediaItem.Builder()
            .setUri(source)
            .setMimeType(MimeTypes.AUDIO_AAC)
            .build()
    }*/

    private val TAG = "BaseVideoTrimmerView"

    private class MessageHandler constructor(view: BaseSeekTrimmerView) : Handler(Looper.getMainLooper()) {
        private val mView: WeakReference<BaseSeekTrimmerView> = WeakReference(view)

        override fun handleMessage(msg: Message) {
            val view = mView.get()
            if (view?.videoPlayer == null)
                return
            view.notifyProgressUpdate(true)
            if (view.videoPlayer?.isPlaying == true) {
                sendEmptyMessageDelayed(0, 10)
            }
        }
    }

    companion object {
        private const val MIN_TIME_FRAME = 1000
        private const val SHOW_PROGRESS = 2
    }
/*

    private fun onPlayerIndicatorSeekChanged(progress: Int, fromUser: Boolean) {
        var duration = (duration * progress / 1000L).toInt()
        if (fromUser) {
            if (duration < startPosition) {
                setProgressBarPosition(startPosition)
                duration = startPosition
            } else if (duration > endPosition) {
                setProgressBarPosition(endPosition)
                duration = endPosition
            }
            onVideoPlaybackReachingTime(duration)
        }
    }

    private fun onPlayerIndicatorSeekStart() {
        playView.visibility = VISIBLE
        notifyProgressUpdate(true)
    }
*/

    /*private  fun onPlayerIndicatorSeekStop(seekBar: SeekBar) {
        mMessageHandler.removeMessages(SHOW_PROGRESS)
        //videoView.pause()
        playView.setVisibility(VISIBLE)
        val duration = (duration * seekBar.progress / 1000L).toInt()
        videoView.seekTo(duration)
        onVideoPlaybackReachingTime(duration)
        notifyProgressUpdate(true)
    }*/

    abstract fun obtainProgressBarView(): ProgressBarViewMultiple

}
