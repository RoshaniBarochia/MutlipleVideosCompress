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
import com.app.mutliple.videos.compression.utils.Constants.VideoTrimLimit
import com.app.mutliple.videos.compression.utils.Constants.media_selection_limit
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
class MediaPagerTrimmingActivity : AppCompatActivity(),DeleteVoiceNoteInterface, MediaThumbAdapter.thumdClick, Player.Listener, VideoTrimmingListenerMultiple {
    private var mMediaShowPagerAdapter: MediaShowTrimPagerAdapter? = null
    private var mMediaThumbAdapter: MediaThumbAdapter? = null
    var mArrayUri: ArrayList<AppMedia> = ArrayList()
    var linearLayoutManager: LinearLayoutManager? = null
    var modeSnappedPosition = -1
    var oldSnappedPosition = -1
    var callFrom = "local"
    var isApiCall=false
    var isSingle=false
    var voice_note_media = ""
    var pagerRegister: ViewPager2.OnPageChangeCallback? = null
    var deleteVoiceNoteInterface: DeleteVoiceNoteInterface? = null
    //    lateinit var voiceNoteListCacheAdapter: File
    var currentVideoMedia: AppMedia? = null
    var voiceNoteListCacheAdapter: File? = null


    companion object {
        private lateinit var outputDirectory: File
        var MIME_TYPES_IMAGE_VIDEO = arrayOf(
            "video/*",
            "image/*"
        )
    }

    var isFirstTime = true

    var makeUri: Uri? = null
    lateinit var file: File
    private lateinit var am: AudioManager
    var isVideoComplete = false
    var isVideoPlayedStatus = Constants.NONE
    var videoPlayer: ExoPlayer? = null
    var isPlaying = false
    var mediaList: ArrayList<AppMedia> = ArrayList()
    private lateinit var img_Add: ImageView
    private lateinit var list_media: RecyclerView
    private lateinit var tvDoneAddMedia: TextView
    private lateinit var lyt_bottom: LinearLayout
    private lateinit var videoTrimmerView: SeekTrimmerView
    private lateinit var rangeSeekBarView: RangeSeekBarViewMultiple
    private lateinit var trimmingContainer: FrameLayout
    private lateinit var frameLayout2: FrameLayout
    private lateinit var ivProgressbarLogin: ProgressBar
    private lateinit var imgAttachedImage: ImageView
    private lateinit var pager_media: ViewPager2
    private lateinit var videoView: PlayerView
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_multimedia_trim)
        voiceNoteListCacheAdapter = getDirectoryVoiceNoteListCache().absoluteFile

        am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        img_Add = findViewById(R.id.img_Add)
        list_media = findViewById(R.id.list_media)
        tvDoneAddMedia = findViewById(R.id.tvDoneAddMedia)
        videoTrimmerView = findViewById(R.id.videoTrimmerView)
        rangeSeekBarView=videoTrimmerView.findViewById(R.id.rangeSeekBarViewMultiple)
        trimmingContainer=videoTrimmerView.findViewById(R.id.trimmingContainer)
        frameLayout2 = findViewById(R.id.frameLayout2)
        ivProgressbarLogin = findViewById(R.id.ivProgressbarLogin)
        imgAttachedImage = findViewById(R.id.imgAttachedImage)
        lyt_bottom = findViewById(R.id.lyt_bottom)
        pager_media = findViewById(R.id.pager_media)
        videoView = findViewById(R.id.videoView)
        outputDirectory = getOutputDirectory()
        videoTrimmerView.setOnK4LVideoListener(this)
//        voiceNoteListCacheAdapter = getDirectoryVoiceNoteListCache()
        if (intent.hasExtra("media_list")) {
            val type: Type = object : TypeToken<List<AppMedia>>() {}.type
            mArrayUri=(Gson().fromJson(intent.getStringExtra("media_list"), type))
        }
        if (intent.hasExtra("extra_list")) {
            val type: Type = object : TypeToken<List<AppMedia>>() {}.type
            mArrayUri.addAll(Gson().fromJson(intent.getStringExtra("extra_list"), type))
        }

        if (mArrayUri.isNotEmpty()) {
            setMediaList()
        }

        callFrom = if (intent.hasExtra("call_from")) {
            intent.getStringExtra("call_from").toString()
        } else {
            "local"
        }
        isSingle=if (intent.hasExtra("isSingle")) {
             intent.getBooleanExtra("isSingle",false)
        }else{
            false
        }
        media_selection_limit = if(isSingle){
            1
        }else
            Constants.multipleMediaLimit

        if (callFrom.equals("server", ignoreCase = true)) {
            img_Add.visibility = View.GONE
            tvDoneAddMedia.visibility = View.GONE
        } else {
            img_Add.visibility = View.VISIBLE
            tvDoneAddMedia.visibility = View.VISIBLE
        }

        setMediaPager()
        pager_media.isUserInputEnabled = false
        mMediaThumbAdapter = MediaThumbAdapter(
            mArrayUri,
            callFrom,
            this@MediaPagerTrimmingActivity,
            voiceNoteListCacheAdapter!!,
            pager_media
        )
        linearLayoutManager = LinearLayoutManager(
            this@MediaPagerTrimmingActivity,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        list_media.layoutManager = linearLayoutManager
        list_media.adapter = mMediaThumbAdapter
        list_media.setHasFixedSize(true)
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
                    list_media.smoothScrollToPosition(position)
                    Handler(Looper.getMainLooper()).postDelayed({
                        setMediaSelected()
                        setPagerData()
                    }, 100)
                }
            }
        }
        pager_media.registerOnPageChangeCallback(pagerRegister!!)
        pager_media.currentItem = when {
            mArrayUri.size > -1 && intent.hasExtra("position") -> {
                intent.getIntExtra("position", -1)
            }
            mArrayUri.size > 0 -> {
                0
            }
            else -> {
                -1
            }
        }
        pager_media.getChildAt(0).setOnTouchListener(object : View.OnTouchListener {
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
            mArrayUri.size > 0 -> {
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
            img_Add.isEnabled = false

            lyt_bottom.isEnabled = false
            list_media.isEnabled = false
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
        img_Add.setOnClickListener {
            if (mArrayUri.size < Constants.media_selection_limit) {
                Utility.showDialogWithMedia(this@MediaPagerTrimmingActivity, object :
                    alertDialogMediaInterface {
                    override fun onCapturePhotoClick() {

                        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

                        val file = File(
                            outputDirectory,
                            System.currentTimeMillis().toString() + ".jpg"
                        )
                        voice_note_media = file.absolutePath

                        val fileUri = FileProvider.getUriForFile(
                            applicationContext,
                            "com.app.mutliple.videos.compression.fileprovider",
                            file
                        )
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
                        startResultForCamera.launch(cameraIntent)
                    }

                    override fun onCaptureVideoClick() {
                        val cameraIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)

                        val file = File(
                            outputDirectory,
                            System.currentTimeMillis().toString() + Constants.VIDEO_FORMAT
                        )
                        voice_note_media = file.absolutePath

                        val fileUri = FileProvider.getUriForFile(
                            applicationContext,
                             "com.app.mutliple.videos.compression.fileprovider",
                            file
                        )

                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
                        startResultForCamera.launch(cameraIntent)
                    }

                    override fun onGalleryClick() {
                        val pickIntent = Intent(Intent.ACTION_GET_CONTENT)
                        pickIntent.addCategory(Intent.CATEGORY_OPENABLE)
                        pickIntent.type = "*/*"
                        pickIntent.putExtra(
                            Intent.EXTRA_MIME_TYPES,
                            Constants.MIME_TYPES_IMAGE_VIDEO
                        )
                        if(!isSingle){
                            pickIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                        }
                        startResultForGallery.launch(pickIntent)
                    }

                }, isCancelable = true, isHideBottomBar = true)
            } else {
                Utility.msgDialog(this@MediaPagerTrimmingActivity, "Can't share more than ${Constants.media_selection_limit}  media items")
            }
            //hideSystemUI(isHide = true)
        }
    }
    
   

    private val startResultForCamera =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK && voice_note_media.isNotEmpty()) {
                // image and video from camera
                if (mArrayUri.size < Constants.media_selection_limit) {
                    val temp = mArrayUri.size
                    if (isImageFile(voice_note_media)) {
                        val imagePath = voice_note_media
                        val compressImageFile =
                            ImageUtility.compressImage(
                                this@MediaPagerTrimmingActivity, outputDirectory, imagePath
                            )
                        voice_note_media = compressImageFile.absolutePath
                        mArrayUri.add(
                            AppMedia(
                                -1,
                                voice_note_media,
                                voice_note_media,
                                0L,
                                 0L,
                                0L
                            )
                        )
                    } else {
                        val duration = Utility.getDuration(
                            File(voice_note_media),
                            this@MediaPagerTrimmingActivity
                        )
                        mArrayUri.add(
                            AppMedia(
                                -1,
                                voice_note_media,
                                voice_note_media,
                                0L,
                                duration,
                                duration,
                                isVideo = true
                            )
                        )
                    }
                    setMediaPager()
                    pager_media.registerOnPageChangeCallback(pagerRegister!!)
                    if (temp != 0) {
                        mMediaThumbAdapter?.notifyItemRangeChanged(temp - 1, mArrayUri.size)
                    } else {
                        mMediaThumbAdapter?.notifyDataSetChanged()
                    }
                    if (mArrayUri.size > 0) {
                        if (temp == 0) {
                            modeSnappedPosition = 0
                        }
                        pager_media.currentItem = modeSnappedPosition
                        setMediaSelected()
                        setMediaList()
                        setPagerData()
                    } else {

                    }
                } else {
                    Utility.msgDialog(this@MediaPagerTrimmingActivity, "Can't share more than ${Constants.media_selection_limit}  media items")
                }
            } else {
                voice_note_media = ""
            }
        }


    private val startResultForGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK &&
                result.data != null
            ) {
                // image and video from gallery
                if (mArrayUri.size < Constants.media_selection_limit) {
                    val temp = mArrayUri.size
                    val data = result.data
                    if (data?.clipData != null) {
                        val coutData: ClipData? = data.clipData
                        for (i in 0 until coutData?.itemCount!!) {
                            if (mArrayUri.size >= Constants.media_selection_limit) {
                                Utility.msgDialog(this@MediaPagerTrimmingActivity, "Can't share more than ${Constants.media_selection_limit}  media items")
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
                                mArrayUri.add(
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
                                mArrayUri.add(
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
                            mArrayUri.add(
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
                            mArrayUri.add(
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
                    pager_media.registerOnPageChangeCallback(pagerRegister!!)
                    if (temp != 0) {
                        mMediaThumbAdapter?.notifyItemRangeChanged(temp - 1, mArrayUri.size)
                    } else {
                        mMediaThumbAdapter?.notifyDataSetChanged()
                    }
                    if (mArrayUri.size > 0) {
                        if (temp == 0) {
                            modeSnappedPosition = 0
                        }
                        pager_media.currentItem = modeSnappedPosition
                        setMediaSelected()
                        setMediaList()
                        setPagerData()
                    } else {

                    }
                } else {
                    Utility.msgDialog(this@MediaPagerTrimmingActivity, "Can't share more than ${Constants.media_selection_limit}  media items")
                }
            } else {
                voice_note_media = ""
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
    }


    @SuppressLint("NewApi")
    override fun onDestroy() {
        ImageUtility.clearGlideCache(this@MediaPagerTrimmingActivity)
        super.onDestroy()
        //hideSystemUI(isHide = false)
    }

    fun setPagerData() {
        if (getMimeType(mArrayUri[pager_media.currentItem].absolute_path)?.isNotEmpty() == true && getMimeType(
                mArrayUri[pager_media.currentItem].absolute_path
            )?.contains("video") == true
        ) {
            imgAttachedImage.visibility = View.GONE
            for (i in mediaList) {
                if (i.absolute_path == mArrayUri[pager_media.currentItem].absolute_path) {
                    currentVideoMedia = i
                    setVideoTrimmerView(i)
                }
            }

        } else
            setCurrentPageDataForImage(mArrayUri[pager_media.currentItem].absolute_path)
    }

    override fun onDeleteClick() {
        if (mArrayUri.size > 0) {
            val temp = pager_media.currentItem
            if (mArrayUri.size > 1 && pager_media.currentItem == (mArrayUri.size - 1)) {
                modeSnappedPosition -= 1
            }
            mArrayUri.removeAt(temp)
            mediaList.clear()
            setMediaPager()
            pager_media.registerOnPageChangeCallback(pagerRegister!!)
            mMediaThumbAdapter?.notifyItemRemoved(temp)
            if (mArrayUri.size > 0) {
                pager_media.currentItem = modeSnappedPosition
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
        if (mArrayUri.isNotEmpty()) {
            for (i in 0 until mArrayUri.size) {
                if (getMimeType(mArrayUri[i].absolute_path)?.isNotEmpty() == true && getMimeType(
                        mArrayUri[i].absolute_path
                    )?.contains("video") == true
                ) {
                    mediaList.add(
                        AppMedia(
                            i,
                            mArrayUri[i].absolute_path,
                            mArrayUri[i].absolute_path,
                            mArrayUri[i].start,
                            mArrayUri[i].end,
                            mArrayUri[i].duration,
                            isEditVoice = mArrayUri[i].isEditVoice,
                            pathUrl = mArrayUri[i].pathUrl,
                        )
                    )
                }
            }
        }
    }

    private fun setMediaPager() {
        mMediaShowPagerAdapter =
            MediaShowTrimPagerAdapter(
                mArrayUri,
                this@MediaPagerTrimmingActivity,
                callFrom,
                voiceNoteListCacheAdapter!!
            )
        pager_media.adapter = mMediaShowPagerAdapter
    }

    private fun setMediaSelected() {

        stopExoPlayer()
        if (mArrayUri.size > 0 && mMediaThumbAdapter != null) {

            if (oldSnappedPosition != -1) {
                val updateOldView: View? =
                    if (list_media.findViewHolderForAdapterPosition(oldSnappedPosition) != null
                        && list_media.findViewHolderForAdapterPosition(oldSnappedPosition)?.itemView != null
                    ) {
                        list_media.findViewHolderForAdapterPosition(oldSnappedPosition)!!.itemView
                    } else null

                if (updateOldView != null) {
                    val imgBorder = updateOldView.findViewById<ImageView>(R.id.ivMediaImageBorder)
                    imgBorder.visibility = View.GONE
                }
            }
            if (modeSnappedPosition != -1) {

                val updateNewView: View? =
                    if (list_media.findViewHolderForAdapterPosition(modeSnappedPosition) != null
                        && list_media.findViewHolderForAdapterPosition(modeSnappedPosition)?.itemView != null
                    ) {
                        list_media.findViewHolderForAdapterPosition(modeSnappedPosition)!!.itemView
                    } else null
                if (updateNewView != null) {

                    val imgBorderNew =
                        updateNewView.findViewById<ImageView>(R.id.ivMediaImageBorder)
                    imgBorderNew.visibility = View.VISIBLE
                }
            }
        } else {

        }
    }

    private fun setBackData() {
        stopPlayer()

        val intent = Intent()
        intent.putExtra("isDelete", true)
        intent.putExtra("media_list", Gson().toJson(mArrayUri))
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun stopPlayer() {
        pager_media.unregisterOnPageChangeCallback(pagerRegister!!)
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

            pager_media.currentItem = position
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
           trimmingContainer?.visibility=View.VISIBLE
        }else{
            videoTrimmerView.visibility = View.VISIBLE
            frameLayout2.visibility = View.VISIBLE
           trimmingContainer?.visibility=View.INVISIBLE
        }

        videoTrimmerView.setMaxDurationInMs(VideoTrimLimit)  // 15 sec set

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

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_ENDED -> {
                        // playView.setVisibility(VISIBLE)
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
            if(media.duration >= VideoTrimLimit  && media.start == 0L) {
                media.start=0L
                media.end= VideoTrimLimit.toLong()
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
            for (i in 0 until mArrayUri.size) {
                if (uri?.path == mArrayUri[i].absolute_path) {
                    val durationLast = Utility.getDuration(
                        File(mArrayUri[i].absolute_path),
                        this@MediaPagerTrimmingActivity
                    )

                    mArrayUri[i].absolute_path = dest
                    mArrayUri[i].start = 0L
                    mArrayUri[i].end = durationLast

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
        for (i in 0 until mArrayUri.size) {
            // Get length of file in bytes
            val length =File(mArrayUri[i].absolute_path).length()
            // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
            val fileSizeInKB = length / 1024
            //  Convert the KB to MegaBytes (1 MB = 1024 KBytes)
            val fileSizeInMB = fileSizeInKB / 1024;

            if ( fileSizeInMB > 3 && getMimeType(mArrayUri[i].absolute_path)?.isNotEmpty() == true && getMimeType(
                    mArrayUri[i].absolute_path
                )?.contains("video") == true && !mArrayUri[i].isComPressed
            ) {
                uris.add(mArrayUri[i].absolute_path)
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
                                for (i in 0 until mArrayUri.size) {
                                    if (uris[index] == mArrayUri[i].absolute_path) {
                                        mArrayUri[i].absolute_path = path.toString()
                                        mArrayUri[i].isComPressed = true
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
                            for (i in 0 until mArrayUri.size) {
                                if (uris[index] == mArrayUri[i].absolute_path) {
                                    //mArrayUri[i].isComPressed = true
                                    Toast.makeText(
                                        this@MediaPagerTrimmingActivity,
                                        "${mArrayUri[i].original_path} video has some problem, Please Try again.",
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
        img_Add.isEnabled = true
        lyt_bottom.isEnabled = true
        list_media.isEnabled = true
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
        mArrayUri[pager_media.currentItem].start = start
        mArrayUri[pager_media.currentItem].end = end

    }

    private fun getLastItemTrimmingApply() {
        val i = mediaList[mediaList.size - 1]
        val fileDest = File(
            outputDirectory,
            "remove_video_trimming${mediaList.size - 1}_${System.currentTimeMillis()}${Constants.VIDEO_FORMAT}"
        )
        if (i.start == 0L && i.end == i.duration) {
            if (i.duration >= VideoTrimLimit) {
                videoTrimmerView.callTrimmingView(
                    File(i.absolute_path),
                    fileDest,
                    i.start.toInt(),
                    VideoTrimLimit,
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
            File(it, Constants.app_hided_folder).absoluteFile.apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) {
            mediaDir.absoluteFile
        } else filesDir.absoluteFile
    }
    fun getDirectoryVoiceNoteListCache(): File {

        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            val file = File(Constants.voiceNoteListCache).absoluteFile
            file.apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) {
            mediaDir.absoluteFile
        } else {
            filesDir.absoluteFile
        }
    }
}
