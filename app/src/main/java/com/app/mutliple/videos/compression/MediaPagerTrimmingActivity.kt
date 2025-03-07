package com.app.mutliple.videos.compression

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.abedelazizshe.lightcompressorlibrary.CompressionListener
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor
import com.abedelazizshe.lightcompressorlibrary.VideoQuality
import com.abedelazizshe.lightcompressorlibrary.config.Configuration
import com.app.mutliple.videos.compression.adapter.MediaThumbAdapter
import com.app.mutliple.videos.compression.model.AppMedia
import com.app.mutliple.videos.compression.trimmingvideo.SeekTrimmerView
import com.app.mutliple.videos.compression.trimmingvideo.VideoTrimmingListenerMultiple
import com.app.mutliple.videos.compression.utils.Constants
import com.app.mutliple.videos.compression.utils.Constants.VideoTrimmingLimit
import com.app.mutliple.videos.compression.utils.Constants.mediaLimits
import com.app.mutliple.videos.compression.utils.DeleteVoiceNoteInterface
import com.app.mutliple.videos.compression.utils.ImageUtility
import com.app.mutliple.videos.compression.utils.Utility
import com.app.mutliple.videos.compression.utils.Utility.getMimeType
import com.app.mutliple.videos.compression.utils.alertDialogMediaInterface
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.app.mutliple.videos.compression.adapter.MediaShowTrimPagerAdapter
import com.app.mutliple.videos.compression.trimmingvideo.RangeSeekBarViewMultiple
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import java.io.File
import java.lang.reflect.Type
import java.net.URLConnection

private const val TAG = "MediaPagerTrimmingActivity"
class MediaPagerTrimmingActivity : AppCompatActivity(),DeleteVoiceNoteInterface, MediaThumbAdapter.ItemMediaClick, Player.Listener, VideoTrimmingListenerMultiple {
    private var mMediaShowPagerAdapter: MediaShowTrimPagerAdapter? = null
    private var mMediaThumbAdapter: MediaThumbAdapter? = null
    var listMediaVideoImage: ArrayList<AppMedia> = ArrayList()
    private var linearLayoutManager: LinearLayoutManager? = null
    var modeSnappedPosition = -1
    var oldSnappedPosition = -1
    private var callFrom = "local"
    var isApiCall=false
    private var isSingle=false
    //var mediaImageVideoNote = ""
    private var pagerRegister: ViewPager2.OnPageChangeCallback? = null
    private var deleteVoiceNoteInterface: DeleteVoiceNoteInterface? = null
    private var currentVideoMedia: AppMedia? = null
    private var voiceNoteListCacheAdapter: File? = null


    companion object {
        private lateinit var outputDirectory: File
    }

    private var isFirstTime = true

    private var makeUri: Uri? = null
    lateinit var file: File
    private lateinit var am: AudioManager
    private var isVideoPlayedStatus = Constants.NONE
    private var videoPlayer: ExoPlayer? = null
    private var mediaList: ArrayList<AppMedia> = ArrayList()
    //private lateinit var imgAdd: ImageView
    private lateinit var listMedia: RecyclerView
    private lateinit var tvDoneAddMedia: TextView
    private lateinit var lytBottom: LinearLayout
    private lateinit var videoTrimmerView: SeekTrimmerView
    private lateinit var rangeSeekBarView: RangeSeekBarViewMultiple
    private lateinit var trimmingContainer: FrameLayout
    private lateinit var frameLayout2: FrameLayout
    private lateinit var ivProgressbarLogin: ProgressBar
    private lateinit var imgAttachedImage: ImageView
    private lateinit var viewPager2: ViewPager2
    private lateinit var videoView: PlayerView
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_multimedia_trim)
        voiceNoteListCacheAdapter = getDirectoryVoiceNoteListCache().absoluteFile

        am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        //imgAdd = findViewById(R.id.img_Add)
        listMedia = findViewById(R.id.list_media)
        tvDoneAddMedia = findViewById(R.id.tvDoneAddMedia)
        videoTrimmerView = findViewById(R.id.videoTrimmerView)
        rangeSeekBarView=videoTrimmerView.findViewById(R.id.rangeSeekBarViewMultiple)
        trimmingContainer=videoTrimmerView.findViewById(R.id.trimmingContainer)
        frameLayout2 = findViewById(R.id.frameLayout2)
        ivProgressbarLogin = findViewById(R.id.ivProgressbarLogin)
        imgAttachedImage = findViewById(R.id.imgAttachedImage)
        lytBottom = findViewById(R.id.lyt_bottom)
        viewPager2 = findViewById(R.id.pager_media)
        videoView = findViewById(R.id.videoView)
        outputDirectory = getOutputDirectory()
        videoTrimmerView.setOnK4LVideoListener(this)
//        voiceNoteListCacheAdapter = getDirectoryVoiceNoteListCache()
        if (intent.hasExtra("media_list")) {
            val type: Type = object : TypeToken<List<AppMedia>>() {}.type
            listMediaVideoImage=(Gson().fromJson(intent.getStringExtra("media_list"), type))
        }
        if (intent.hasExtra("extra_list")) {
            val type: Type = object : TypeToken<List<AppMedia>>() {}.type
            listMediaVideoImage.addAll(Gson().fromJson(intent.getStringExtra("extra_list"), type))
        }

        if (listMediaVideoImage.isNotEmpty()) {
            setMediaList()
        }


        isSingle=if (intent.hasExtra("isSingle")) {
             intent.getBooleanExtra("isSingle",false)
        }else{
            false
        }
        mediaLimits = if(isSingle){
            1
        }else
            Constants.MULTIPLE_MEDIA_LIMIT

            //imgAdd.visibility = View.VISIBLE
            tvDoneAddMedia.visibility = View.VISIBLE


        setMediaPager()
        viewPager2.isUserInputEnabled = false
        mMediaThumbAdapter = MediaThumbAdapter(
            listMediaVideoImage,
            this@MediaPagerTrimmingActivity,
            viewPager2
        )
        linearLayoutManager = LinearLayoutManager(
            this@MediaPagerTrimmingActivity,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        listMedia.layoutManager = linearLayoutManager
        listMedia.adapter = mMediaThumbAdapter
        listMedia.setHasFixedSize(true)
        pagerRegister = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                if (modeSnappedPosition != position) {
                    //videoTrimmerView.pauseVideo()
                    oldSnappedPosition = modeSnappedPosition
                    modeSnappedPosition = position
                    listMedia.smoothScrollToPosition(position)
                    Handler(Looper.getMainLooper()).postDelayed({
                        setMediaSelected()
                        setPagerData()
                    }, 100)
                }
            }
        }
        viewPager2.registerOnPageChangeCallback(pagerRegister!!)
        viewPager2.currentItem = when {
            listMediaVideoImage.size > -1 && intent.hasExtra("position") -> {
                intent.getIntExtra("position", -1)
            }
            listMediaVideoImage.size > 0 -> {
                0
            }
            else -> {
                -1
            }
        }
        viewPager2.getChildAt(0).setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (videoTrimmerView != null && !isApiCall) {
                            videoTrimmerView.onClickVideoPlayPause()
                        }
                    }
                }

                return v?.onTouchEvent(event) ?: true
            }
        })
        modeSnappedPosition = when {
            intent.hasExtra("position") -> {
                intent.getIntExtra("position", -1)
            }
            listMediaVideoImage.size > 0 -> {
                0
            }
            else -> {
                -1
            }
        }
        Handler(Looper.getMainLooper()).postDelayed({
            setMediaSelected()
            setPagerData()
        }, 100)
        fun setDeleteClick(deleteVoiceNoteInterface: DeleteVoiceNoteInterface) {
            this.deleteVoiceNoteInterface = deleteVoiceNoteInterface
        }
        tvDoneAddMedia.setOnClickListener {
            isApiCall=true
            stopExoPlayer()
            tvDoneAddMedia.isEnabled = false
           // imgAdd.isEnabled = false

            lytBottom.isEnabled = false
            listMedia.isEnabled = false
            rangeSeekBarView.isEnabled=false
            rangeSeekBarView.isClickable=false
            ivProgressbarLogin.visibility = View.VISIBLE
            tvDoneAddMedia.background = ContextCompat.getDrawable(
                this@MediaPagerTrimmingActivity,
                R.drawable.btn_round_disable
            )
            for(i in mediaList.indices){
                if(mediaList[i].isEditVoice){
                    mediaList.removeAt(i)
                }
            }

            if (mediaList.size > 0)
                getLastItemTrimmingApply()
            else
                setBackData()
        }
//        imgAdd.setOnClickListener {
//            if (listMediaVideoImage.size < Constants.mediaLimits) {
//                Utility.showDialogWithMedia(this@MediaPagerTrimmingActivity, object :
//                    alertDialogMediaInterface {
//                    override fun onCapturePhotoClick() {
//
//                        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//
//                        val file = File(
//                            outputDirectory,
//                            System.currentTimeMillis().toString() + ".jpg"
//                        )
//                        mediaImageVideoNote = file.absolutePath
//
//                        val fileUri = FileProvider.getUriForFile(
//                            applicationContext,
//                            BuildConfig.APPLICATION_ID+"fileprovider",
//                            file
//                        )
//                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
//                        startResultForCamera.launch(cameraIntent)
//                    }
//
//                    override fun onCaptureVideoClick() {
//                        val cameraIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
//
//                        val file = File(
//                            outputDirectory,
//                            System.currentTimeMillis().toString() + Constants.VIDEO_FORMAT
//                        )
//                        mediaImageVideoNote = file.absolutePath
//
//                        val fileUri = FileProvider.getUriForFile(
//                            applicationContext,
//                            BuildConfig.APPLICATION_ID+".fileprovider",
//                            file
//                        )
//
//                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
//                        startResultForCamera.launch(cameraIntent)
//                    }
//
//                    override fun onGalleryClick() {
//                        val pickIntent = Intent(Intent.ACTION_GET_CONTENT)
//                        pickIntent.addCategory(Intent.CATEGORY_OPENABLE)
//                        pickIntent.type = "*/*"
//                        pickIntent.putExtra(
//                            Intent.EXTRA_MIME_TYPES,
//                            Constants.MIME_TYPES_IMAGE_VIDEO
//                        )
//                        if(!isSingle){
//                            pickIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
//                        }
//                        startResultForGallery.launch(pickIntent)
//                    }
//
//                }, isCancelable = true)
//            } else {
//                Utility.msgDialog(this@MediaPagerTrimmingActivity, "Can't share more than ${mediaLimits}  media items")
//            }
//        }
    }
    
   

   /* private val startResultForCamera =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK && mediaImageVideoNote.isNotEmpty()) {
                // image and video from camera
                if (listMediaVideoImage.size < Constants.mediaLimits) {
                    val temp = listMediaVideoImage.size
                    if (isImageFile(mediaImageVideoNote)) {
                        val imagePath = mediaImageVideoNote
                        val compressImageFile =
                            ImageUtility.compressImage(
                                this@MediaPagerTrimmingActivity, outputDirectory, imagePath
                            )
                        mediaImageVideoNote = compressImageFile.absolutePath
                        listMediaVideoImage.add(
                            AppMedia(
                                -1,
                                mediaImageVideoNote,
                                mediaImageVideoNote,
                                0L,
                                 0L,
                                0L
                            )
                        )
                    } else {
                        val duration = Utility.getDuration(
                            File(mediaImageVideoNote),
                            this@MediaPagerTrimmingActivity
                        )
                        listMediaVideoImage.add(
                            AppMedia(
                                -1,
                                mediaImageVideoNote,
                                mediaImageVideoNote,
                                0L,
                                duration,
                                duration,
                                isVideo = true
                            )
                        )
                    }
                    setMediaPager()
                    viewPager2.registerOnPageChangeCallback(pagerRegister!!)
                    if (temp != 0) {
                        mMediaThumbAdapter?.notifyItemRangeChanged(temp - 1, listMediaVideoImage.size)
                    } else {
                        mMediaThumbAdapter?.notifyDataSetChanged()
                    }
                    if (listMediaVideoImage.size > 0) {
                        if (temp == 0) {
                            modeSnappedPosition = 0
                        }
                        viewPager2.currentItem = modeSnappedPosition
                        setMediaSelected()
                        setMediaList()
                        setPagerData()
                    } else {

                    }
                } else {
                    Utility.msgDialog(this@MediaPagerTrimmingActivity, "Can't share more than ${Constants.mediaLimits}  media items")
                }
            } else {
                mediaImageVideoNote = ""
            }
        }


    private val startResultForGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK &&
                result.data != null
            ) {
                // image and video from gallery
                if (listMediaVideoImage.size < Constants.mediaLimits) {
                    val temp = listMediaVideoImage.size
                    val data = result.data
                    if (data?.clipData != null) {
                        val coutData: ClipData? = data.clipData
                        for (i in 0 until coutData?.itemCount!!) {
                            if (listMediaVideoImage.size >= Constants.mediaLimits) {
                                Utility.msgDialog(this@MediaPagerTrimmingActivity, "Can't share more than ${Constants.mediaLimits}  media items")
                                break
                            }
                            val imageUrl: Uri = data.clipData!!.getItemAt(i).uri
                            val path = com.app.mutliple.videos.compression.utils.FileUtils.getRealPath(this, imageUrl)
                            if (isImageFile(path)) {
                                val compressImageFile =
                                    ImageUtility.compressImage(
                                        this@MediaPagerTrimmingActivity,
                                        outputDirectory,
                                        path
                                    )
                                listMediaVideoImage.add(
                                    AppMedia(
                                        -1,
                                        compressImageFile.absolutePath,
                                        compressImageFile.absolutePath,
                                        0L,
                                        0L,
                                        0L
                                    )
                                )
                            } else {
                                val duration =
                                    Utility.getDuration(File(path), this@MediaPagerTrimmingActivity)
                                listMediaVideoImage.add(
                                    AppMedia(
                                        -1,
                                        path,
                                        path,
                                        0L,
                                        duration,
                                        duration,
                                        isVideo = true
                                    )
                                )
                            }
                        }
                    } else {
                        if (isImageFile(
                                com.app.mutliple.videos.compression.utils.FileUtils.getRealPath(
                                    this,
                                    Uri.parse(data?.data.toString())
                                )
                            )
                        ) {
                            val imagePath =
                                com.app.mutliple.videos.compression.utils.FileUtils.getRealPath(this, Uri.parse(data?.data?.toString()))
                            val compressImageFile =
                                ImageUtility.compressImage(
                                    this@MediaPagerTrimmingActivity,
                                    outputDirectory,
                                    imagePath
                                )
                            listMediaVideoImage.add(
                                AppMedia(
                                    -1,
                                    compressImageFile.absolutePath,
                                    compressImageFile.absolutePath,
                                    0L,
                                    0L,
                                    0L
                                )
                            )
                        } else {
                            val path =
                                com.app.mutliple.videos.compression.utils.FileUtils.getRealPath(this, Uri.parse(data?.data?.toString()))
                            val duration =
                                Utility.getDuration(File(path), this@MediaPagerTrimmingActivity)
                            listMediaVideoImage.add(
                                AppMedia(
                                    -1,
                                    path,
                                    path,
                                    0L,
                                    duration,
                                    duration,
                                    isVideo = true
                                )
                            )
                        }
                    }

                    setMediaPager()
                    viewPager2.registerOnPageChangeCallback(pagerRegister!!)
                    if (temp != 0) {
                        mMediaThumbAdapter?.notifyItemRangeChanged(temp - 1, listMediaVideoImage.size)
                    } else {
                        mMediaThumbAdapter?.notifyDataSetChanged()
                    }
                    if (listMediaVideoImage.size > 0) {
                        if (temp == 0) {
                            modeSnappedPosition = 0
                        }
                        viewPager2.currentItem = modeSnappedPosition
                        setMediaSelected()
                        setMediaList()
                        setPagerData()
                    } else {

                    }
                } else {
                    Utility.msgDialog(this@MediaPagerTrimmingActivity, "Can't share more than ${Constants.mediaLimits}  media items")
                }
            } else {
                mediaImageVideoNote = ""
            }
        }

    var isVideoRecorded = false
    var isImageCapture = false
    private fun isImageFile(path: String?): Boolean {

        isImageCapture = true
        isVideoRecorded = false
        return if (!Utility.isEmpty(path)) {
            val mimeType: String = URLConnection.guessContentTypeFromName(path)
            mimeType != null && mimeType.startsWith("image")
        } else
            false
    }*/


    @SuppressLint("NewApi")
    override fun onDestroy() {
        ImageUtility.clearGlideCache(this@MediaPagerTrimmingActivity)
        super.onDestroy()
        //hideSystemUI(isHide = false)
    }

    fun setPagerData() {
        if (getMimeType(listMediaVideoImage[viewPager2.currentItem].absolute_path)?.isNotEmpty() == true && getMimeType(
                listMediaVideoImage[viewPager2.currentItem].absolute_path
            )?.contains("video") == true
        ) {
            imgAttachedImage.visibility = View.GONE
            for (i in mediaList) {
                if (i.absolute_path == listMediaVideoImage[viewPager2.currentItem].absolute_path) {
                    currentVideoMedia = i
                    setVideoTrimmerView(i)
                }
            }

        } else
            setCurrentPageDataForImage(listMediaVideoImage[viewPager2.currentItem].absolute_path)
    }

    override fun onDeleteClick() {
        if (listMediaVideoImage.size > 0) {
            val temp = viewPager2.currentItem
            if (listMediaVideoImage.size > 1 && viewPager2.currentItem == (listMediaVideoImage.size - 1)) {
                modeSnappedPosition -= 1
            }
            listMediaVideoImage.removeAt(temp)
            mediaList.clear()
            setMediaPager()
            viewPager2.registerOnPageChangeCallback(pagerRegister!!)
            mMediaThumbAdapter?.notifyItemRemoved(temp)
            if (listMediaVideoImage.size > 0) {
                viewPager2.currentItem = modeSnappedPosition
                setMediaSelected()
                setMediaList()
                setPagerData()
            } else {
                stopExoPlayer()
                videoView.visibility = View.GONE
                imgAttachedImage.visibility = View.GONE
                videoTrimmerView.visibility = View.GONE
            }
        }
    }

    fun setMediaList() {
        mediaList.clear()
        if (listMediaVideoImage.isNotEmpty()) {
            for (i in 0 until listMediaVideoImage.size) {
                if (getMimeType(listMediaVideoImage[i].absolute_path)?.isNotEmpty() == true && getMimeType(
                        listMediaVideoImage[i].absolute_path
                    )?.contains("video") == true
                ) {
                    mediaList.add(
                        AppMedia(
                            i,
                            listMediaVideoImage[i].absolute_path,
                            listMediaVideoImage[i].absolute_path,
                            listMediaVideoImage[i].start,
                            listMediaVideoImage[i].end,
                            listMediaVideoImage[i].duration,
                            isEditVoice = listMediaVideoImage[i].isEditVoice,
                            pathUrl = listMediaVideoImage[i].pathUrl,
                        )
                    )
                }
            }
        }
    }

    private fun setMediaPager() {
        mMediaShowPagerAdapter =
            MediaShowTrimPagerAdapter(
                listMediaVideoImage
            )
        viewPager2.adapter = mMediaShowPagerAdapter
    }

    private fun setMediaSelected() {

        stopExoPlayer()
        if (listMediaVideoImage.size > 0 && mMediaThumbAdapter != null) {

            if (oldSnappedPosition != -1) {
                val updateOldView: View? =
                    if (listMedia.findViewHolderForAdapterPosition(oldSnappedPosition) != null
                        && listMedia.findViewHolderForAdapterPosition(oldSnappedPosition)?.itemView != null
                    ) {
                        listMedia.findViewHolderForAdapterPosition(oldSnappedPosition)!!.itemView
                    } else null

                if (updateOldView != null) {
                    val imgBorder = updateOldView.findViewById<ImageView>(R.id.ivMediaImageBorder)
                    imgBorder.visibility = View.GONE
                }
            }
            if (modeSnappedPosition != -1) {

                val updateNewView: View? =
                    if (listMedia.findViewHolderForAdapterPosition(modeSnappedPosition) != null
                        && listMedia.findViewHolderForAdapterPosition(modeSnappedPosition)?.itemView != null
                    ) {
                        listMedia.findViewHolderForAdapterPosition(modeSnappedPosition)!!.itemView
                    } else null
                if (updateNewView != null) {

                    val imgBorderNew =
                        updateNewView.findViewById<ImageView>(R.id.ivMediaImageBorder)
                    imgBorderNew.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setBackData() {
        stopPlayer()

        val intent = Intent()
        intent.putExtra("isDelete", true)
        intent.putExtra("media_list", Gson().toJson(listMediaVideoImage))
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun stopPlayer() {
        viewPager2.unregisterOnPageChangeCallback(pagerRegister!!)
        videoTrimmerView.pauseVideo()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopPlayer()
        stopExoPlayer()
    }


    override fun onItemClick(position: Int) {
        if(!isApiCall) {

            viewPager2.currentItem = position
        }
    }

//    /* get output file path*/
//    private fun getOutputDirectory(): File {
//        val mediaDir = externalMediaDirs.firstOrNull()?.let {
//            File(it, Constants.app_hided_folder).absoluteFile.apply { mkdirs() }
//        }
//        return if (mediaDir != null && mediaDir.exists()) {
//            mediaDir.absoluteFile
//        } else filesDir.absoluteFile
//    }

    private fun setCurrentPageDataForImage(model: String) {
        imgAttachedImage.visibility = View.GONE
        val mContext = this@MediaPagerTrimmingActivity

        imgAttachedImage.visibility = View.VISIBLE
        videoTrimmerView.visibility = View.GONE
        videoView.visibility = View.GONE

        var profilePic: String
        
            profilePic = model
            ImageUtility.loadMedianGlide(
                profilePic,
                imageView = imgAttachedImage,
                progressBar = ivProgressbarLogin
            )

        

    }

    private val focusChangeListener by lazy {
        AudioManager.OnAudioFocusChangeListener { focusChange ->
            am
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS -> {
                    if (videoPlayer != null && videoPlayer!!.isPlaying) {
                        videoPlayer!!.playWhenReady = false
                    }

                }
                AudioManager.AUDIOFOCUS_GAIN -> {
                }
                else -> {
                }
            }
        }
    }

    private fun buildMediaItemMP4(source: Uri): MediaItem {
        return MediaItem.Builder()
            .setUri(source)
            .setMimeType(MimeTypes.VIDEO_MP4)
            .build()
    }

    fun stopExoPlayer() {
        if (videoPlayer != null) {
            
            isVideoPlayedStatus = Constants.NONE
            videoPlayer?.playWhenReady = false
            videoPlayer?.removeListener(this)
            videoPlayer?.release()
            videoPlayer = null
        } 

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setVideoTrimmerView(media: AppMedia) {
        file = File(media.original_path)

        
        ivProgressbarLogin.visibility = View.VISIBLE
        videoView.visibility = View.VISIBLE
        if(!media.isEditVoice) {
            videoTrimmerView.visibility = View.VISIBLE
            frameLayout2.visibility = View.VISIBLE
           trimmingContainer.visibility=View.VISIBLE
        }else{
            videoTrimmerView.visibility = View.VISIBLE
            frameLayout2.visibility = View.VISIBLE
           trimmingContainer.visibility=View.INVISIBLE
        }

        videoTrimmerView.setMaxDurationInMs(VideoTrimmingLimit)  // 15 sec set

        val trimmedVideoFile = File(outputDirectory, "remove_video_trimming" + System.currentTimeMillis() + Constants.VIDEO_FORMAT)

        videoTrimmerView.setDestinationFile(trimmedVideoFile)
        makeUri = myUri(Uri.fromFile(file))
        videoTrimmerView.isFirstTime(isFirstTime)
        val dataSourceFactory = DefaultDataSourceFactory(
            this@MediaPagerTrimmingActivity,
            Util.getUserAgent(
                this@MediaPagerTrimmingActivity,
                resources.getString(R.string.app_name)
            )
        )
        val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(buildMediaItemMP4(makeUri!!))

        videoPlayer = ExoPlayer.Builder(this@MediaPagerTrimmingActivity).build().apply {
            setMediaSource(videoSource)
        }
        videoPlayer?.prepare()
        videoView.player = videoPlayer

        videoPlayer?.playWhenReady = false
        videoPlayer?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    val result = Utility.setAudioManager(
                        focusChangeListener,
                        this@MediaPagerTrimmingActivity
                    )
                    if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        // other app had stopped playing song now , so you can start recording.
                    }
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                when (playbackState) {
                    Player.STATE_ENDED -> {
                        videoTrimmerView.onVideoCompleted()
                    }
                    Player.STATE_READY -> {
                        // startProgress()
                        // playView.setVisibility(if (videoPlayer!!.playWhenReady) GONE else VISIBLE)
                    }
                    else -> {
                    }
                }

            }

        })

        videoPlayer?.let {
            if(media.duration >= VideoTrimmingLimit  && media.start == 0L) {
                media.start=0L
                media.end= VideoTrimmingLimit.toLong()
            }
            videoTrimmerView.setVideoURI(
                makeUri!!, media.start, media.end, it
            )
        }
        videoTrimmerView.setVideoInformationVisibility(false)

    }

    private fun myUri(originalUri: Uri): Uri {
        val returnedUri: Uri = if (originalUri.scheme == null) {
            Uri.fromFile(file)
        } else {
            originalUri
        }
        return returnedUri
    }

    override fun onPause() {
        super.onPause()
        videoTrimmerView.pauseVideo()
        stopPlayer()
    }


    override fun onVideoPrepared() {
        try {
            Handler(Looper.getMainLooper()).postDelayed({
                if (ivProgressbarLogin != null)
                    ivProgressbarLogin.visibility = View.GONE
            }, 500)
        } catch (e: Exception) {
        }
    }


    override fun onTrimStarted(start: Long, end: Long) {
        ivProgressbarLogin.visibility = View.VISIBLE
    }

    override fun onFinishedTrimming(
        uri: Uri?,
        dest: String?,
        start: Long,
        end: Long,
        duration: Long
    ) {
        

        if (dest == null) {
            ivProgressbarLogin.visibility = View.GONE
            Handler(Looper.getMainLooper()).postDelayed({
                Utility.msgDialog(this@MediaPagerTrimmingActivity, "Failed trimming...", dialogType = Constants.SUCCESS)
            }, 500)
        } else {

            // ivProgressbarLogin.visibility = View.GONE
            mediaList.removeAt(mediaList.size - 1)
            for (i in 0 until listMediaVideoImage.size) {
                if (uri?.path == listMediaVideoImage[i].absolute_path) {
                    val durationLast = Utility.getDuration(
                        File(listMediaVideoImage[i].absolute_path),
                        this@MediaPagerTrimmingActivity
                    )

                    listMediaVideoImage[i].absolute_path = dest
                    listMediaVideoImage[i].start = 0L
                    listMediaVideoImage[i].end = durationLast

                }
            }
            if (mediaList.size > 0) {
                getLastItemTrimmingApply()
            } else {
                processVideo()
            }

        }
    }

    @SuppressLint("SetTextI18n")
    private fun processVideo() {
        ivProgressbarLogin.visibility = View.VISIBLE
        val uris = mutableListOf<String>()
        var count = 0
        for (i in 0 until listMediaVideoImage.size) {
            // Get length of file in bytes
            val length =File(listMediaVideoImage[i].absolute_path).length()
            // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
            val fileSizeInKB = length / 1024
            //  Convert the KB to MegaBytes (1 MB = 1024 KBytes)
            val fileSizeInMB = fileSizeInKB / 1024;

            if ( fileSizeInMB > 3 && getMimeType(listMediaVideoImage[i].absolute_path)?.isNotEmpty() == true && getMimeType(
                    listMediaVideoImage[i].absolute_path
                )?.contains("video") == true && !listMediaVideoImage[i].isComPressed
            ) {
                uris.add(listMediaVideoImage[i].absolute_path)
            }
        }
        if (uris.size == 0) {
            setBackData()
        }else {
            GlobalScope.launch {
                VideoCompressor.start(
                    context = this@MediaPagerTrimmingActivity,
                    uris,
                    isStreamable = false,
                    saveAt = Environment.DIRECTORY_MOVIES,
                    listener = object : CompressionListener {
                        override fun onProgress(index: Int, percent: Float) {
                            //Update UI
                            if (percent <= 100 && percent.toInt() % 5 == 0)
                                runOnUiThread {
                                    Log.d( TAG, "onProgress: index:: $index percent :: $percent")
                                }
                        }

                        override fun onStart(index: Int) {
                            Log.d(  TAG, "onStart: index:: $index")
                        }

                        override fun onSuccess(index: Int, size: Long, path: String?) {

                            if (path != null) {
                                for (i in 0 until listMediaVideoImage.size) {
                                    if (uris[index] == listMediaVideoImage[i].absolute_path) {
                                        listMediaVideoImage[i].absolute_path = path.toString()
                                        listMediaVideoImage[i].isComPressed = true
                                        count++

                                        break
                                    }
                                }
                                if (uris.size == count) {
                                    ivProgressbarLogin.visibility = View.GONE
                                    setBackData()
                                }
                            }
                        }

                        override fun onFailure(index: Int, failureMessage: String) {
                            for (i in 0 until listMediaVideoImage.size) {
                                if (uris[index] == listMediaVideoImage[i].absolute_path) {
                                    //mArrayUri[i].isComPressed = true
                                    Toast.makeText(
                                        this@MediaPagerTrimmingActivity,
                                        "${listMediaVideoImage[i].original_path} video has some problem, Please Try again.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                            buttonDataEnable()

                            Log.d( "failureMessage", failureMessage)
                            ivProgressbarLogin.visibility = View.GONE
                        }

                        override fun onCancelled(index: Int) {
                            Log.d( "TAG", "compression has been cancelled")
                            // make UI changes, cleanup, etc
                        }
                    },
                    configureWith = Configuration(
                        quality = VideoQuality.MEDIUM,
                        isMinBitrateCheckEnabled = false
                    )
                )
            }
        }
    }
    fun buttonDataEnable(){
        isApiCall=false
        tvDoneAddMedia.isEnabled = true
       // imgAdd.isEnabled = true
        lytBottom.isEnabled = true
        listMedia.isEnabled = true
        rangeSeekBarView.isEnabled=true
        rangeSeekBarView.isClickable=true

        tvDoneAddMedia.background = ContextCompat.getDrawable(
            this@MediaPagerTrimmingActivity,
            R.drawable.btn_round_colorprimary
        )
    }

    override fun onErrorWhileViewingVideo(what: Int, extra: Int) {
        ivProgressbarLogin.visibility = View.GONE
        Log.d(  TAG, "what->" + what + "extra->" + extra)
    }

    override fun changeThumbPosition(start: Long, end: Long) {
        currentVideoMedia?.start = start
        currentVideoMedia?.end = end
        listMediaVideoImage[viewPager2.currentItem].start = start
        listMediaVideoImage[viewPager2.currentItem].end = end

    }

    private fun getLastItemTrimmingApply() {
        val i = mediaList[mediaList.size - 1]
        val fileDest = File(
            outputDirectory,
            "remove_video_trimming${mediaList.size - 1}_${System.currentTimeMillis()}${Constants.VIDEO_FORMAT}"
        )
        if (i.start == 0L && i.end == i.duration) {
            if (i.duration >= VideoTrimmingLimit) {
                videoTrimmerView.callTrimmingView(
                    File(i.absolute_path),
                    fileDest,
                    i.start.toInt(),
                    VideoTrimmingLimit,
                    i.duration
                )
            } else {
                mediaList.removeAt(mediaList.size - 1)
                if (mediaList.size > 0)
                    getLastItemTrimmingApply()
                else
                //setBackData()
                    processVideo()
            }
        } else
            videoTrimmerView.callTrimmingView(
                File(i.absolute_path),
                fileDest,
                i.start.toInt(),
                i.end.toInt(),
                i.duration
            )
    }
    /* get output file path*/
    fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, Constants.APP_HIDDEN_FOLDER).absoluteFile.apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) {
            mediaDir.absoluteFile
        } else filesDir.absoluteFile
    }
    fun getDirectoryVoiceNoteListCache(): File {

        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            val file = File(Constants.IMAGE_VIDEO_CACHE).absoluteFile
            file.apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) {
            mediaDir.absoluteFile
        } else {
            filesDir.absoluteFile
        }
    }
}
