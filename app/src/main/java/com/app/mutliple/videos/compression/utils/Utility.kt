package com.app.mutliple.videos.compression.utils

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.app.mutliple.videos.compression.R
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.Formatter
import java.util.Locale

object Utility {
    fun getMimeType(url: String?): String? {
        var type: String? = ""
        if (!url.isNullOrEmpty()) {
            val extension = MimeTypeMap.getFileExtensionFromUrl(url)
            if (extension != null) {
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            }
        }
        if (type == null) {
            if (url?.contains(Constants.VIDEO_FORMAT) == true) {
                return "video"
            }
        }
        return type
    }

    fun stringForDuration(timeUs: Long): String {
        val mFormatBuilder = StringBuilder()
        val mFormatter = Formatter(mFormatBuilder, Locale.getDefault())
        val totalSeconds = timeUs / 1000
        if (totalSeconds <= 0) {
            return "00:00"
        }
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        mFormatBuilder.setLength(0)

        //  seconds += 3

        val formattedValues = if (hours > 0) {
            mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        } else {
            mFormatter.format("%02d:%02d", minutes, seconds).toString()
        }

        /*return if (hours > 0) {
            mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        } else {
            mFormatter.format("%02d:%02d", minutes, seconds).toString()
        }*/
//        Log.d("TAG", "stringForDuration formatedValues: $formatedValues")
        return formattedValues
    }

    fun refreshGallery(path: String, context: Context) {

        val file = File(path)
        try {
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val contentUri = Uri.fromFile(file)
            mediaScanIntent.data = contentUri
            context.sendBroadcast(mediaScanIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /////////////////////////////////////////////////////////////
    fun getDuration(newFile: File? = null, context: Context): Long {
        var dur = "0"
        try {

            if (newFile != null) {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, Uri.fromFile(newFile))
                dur = "" + retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                retriever.release()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return if (dur != null && dur != "null") dur.toLong() else 0L
    }

    fun getDuration(url: String): Long {
        var dur = 0L
        try {
            if (url != null) {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(url, HashMap<String, String>())
                dur = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLong()!!
                retriever.release()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return if (dur != null) dur.toLong() else 0L
    }
    fun setAudioManager(
        focusChangeListener: AudioManager.OnAudioFocusChangeListener,
        context: Context,
    ): Int {
        val am = context.getSystemService(AppCompatActivity.AUDIO_SERVICE) as AudioManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            am.requestAudioFocus(
                AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(focusChangeListener).build()
            )
        } else {
            am.requestAudioFocus(
                focusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }

    fun calculateDurationInMillis(duration: String): Int {
        var seekValueMS = 0
        if (duration.isNotEmpty() && duration.contains(":")) {
            duration.let {
                val second = it.split(":")
                val min = second[0]
                val sec = second[1]
                val minutes = min.toInt() * 60
                val seekValueSec = minutes + sec.toInt()
                seekValueMS = seekValueSec * 1000
            }
        }
        return seekValueMS
    }
    @SuppressLint("StaticFieldLeak")
    // var oldDialogMsg = MessageDialog.getInstance()
    fun msgDialog(
        context: AppCompatActivity,
        msg: String,
        msgTitle: String = "",
        dialogType: String? = Constants.ERROR,
        iconDialogStatus: Int = 0,
        backgroundColorDialog: Int = R.color.black,
        msgType: String = "",
        mShowCloseIcon: Boolean = false
    ): MessageDialog {
        var dialogMsg: MessageDialog? = MessageDialog(iconDialogStatus, backgroundColorDialog)


        if (dialogMsg != null) {
            try {

                /*if (oldDialogMsg != null) {
                oldDialogMsg.dismiss()
            }*/
                dialogMsg = MessageDialog.getInstance(iconDialogStatus, backgroundColorDialog)
                val bundle = Bundle()
                bundle.putString("okTxt", "OK")
                bundle.putString("tvMsgText", msg)
                bundle.putString("msgTitle", msgTitle)
                bundle.putString("dialogType", "" + dialogType)
                bundle.putString("msgType", "" + msgType)
                bundle.putBoolean("mShowCloseIcon", mShowCloseIcon)

                dialogMsg.arguments = bundle

                if (!dialogMsg.isAdded) {

                    dialogMsg.show(context.supportFragmentManager, "")
                    context.lifecycleScope.launch {
                        delay(3000)
                        try {
                            if (dialogMsg != null) {
                                if (dialogMsg.isVisible == true) {
                                    dialogMsg.dismiss()
                                }
                            }
                        } catch (e: NullPointerException) {
                            e.printStackTrace()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }
                    dialogMsg.setListener(MessageDialog.OnClick {
                        try {
                            if (dialogMsg != null) {
                                if (dialogMsg?.isVisible == true) {
                                    dialogMsg?.dismiss()
                                }
                            }
                        } catch (e: java.lang.Exception) {

                        }
                    })
                    //oldDialogMsg = dialogMsg
                }

            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

        return dialogMsg!!
    }

    @SuppressLint("SetTextI18n")
    fun showDialogWithMedia(
        context: AppCompatActivity,
        alertDialogMediaInterface: alertDialogMediaInterface,
        isCancelable: Boolean,
        isHideBottomBar: Boolean = false,
        isUploadAudioMenu: Boolean = false,
    ): Dialog {
        val alertwithMediaOption = Dialog(context, R.style.myDialogTheme)
        alertwithMediaOption.requestWindowFeature(Window.FEATURE_NO_TITLE)
        alertwithMediaOption.setContentView(R.layout.dialog_media)
        alertwithMediaOption.setCancelable(isCancelable)

        val window = alertwithMediaOption.window
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val tv_title = alertwithMediaOption.findViewById<TextView>(R.id.tv_title)
        val tv_capture_photo = alertwithMediaOption.findViewById<TextView>(R.id.tv_capture_photo)
        val tv_capture_video = alertwithMediaOption.findViewById<TextView>(R.id.tv_capture_video)
        val tv_gallery = alertwithMediaOption.findViewById<TextView>(R.id.tv_gallery)
        val tvCancel = alertwithMediaOption.findViewById<TextView>(R.id.tvCancel)

        val ViewCaptureVideo = alertwithMediaOption.findViewById<View>(R.id.ViewCaptureVideo)

        if (isUploadAudioMenu) {
            tv_title.text = "Upload audio"
            tv_capture_video.text = "Upload audio from device"
            tv_gallery.text = "Extract audio from video"

            tv_capture_photo.visibility = View.GONE
            ViewCaptureVideo.visibility = View.GONE
        }

        tv_capture_photo.setOnClickListener {
            alertDialogMediaInterface.onCapturePhotoClick()
            if (alertwithMediaOption != null && alertwithMediaOption.isShowing) {
                alertwithMediaOption.dismiss()
            }
        }
        tv_capture_video.setOnClickListener {
            alertDialogMediaInterface.onCaptureVideoClick()
            if (alertwithMediaOption != null && alertwithMediaOption.isShowing) {
                alertwithMediaOption.dismiss()
            }
        }
        tv_gallery.setOnClickListener {
            alertDialogMediaInterface.onGalleryClick()
            if (alertwithMediaOption != null && alertwithMediaOption.isShowing) {
                alertwithMediaOption.dismiss()
            }
        }
        tvCancel.setOnClickListener {
            if (alertwithMediaOption != null && alertwithMediaOption.isShowing) {
                alertwithMediaOption.dismiss()
            }
        }

        alertwithMediaOption.show()

        return alertwithMediaOption
    }

    fun getHeightWidthDuration(newFile: File? = null, context: Context): Array<String?> {
        val dur: Array<String?> = arrayOfNulls(3)
        try {

            if (newFile != null) {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, Uri.fromFile(newFile))
                dur[0] = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                dur[1] = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                dur[2] = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)

                retriever.release()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return dur
    }

    fun getImageSize(newFile: File? = null, context: Context): IntArray {
        try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            val input = context.contentResolver.openInputStream(Uri.fromFile(newFile));
            BitmapFactory.decodeStream(input, null, options); input?.close();
            return intArrayOf(options.outWidth, options.outHeight)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return intArrayOf(0, 0)
    }

    /////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////
    fun getText(eTxt: EditText?): String {
        return eTxt?.text?.toString()?.trim { it <= ' ' } ?: ""
    }


    fun isEmpty(s: String?): Boolean {
        return s == null || s.isEmpty() || s.equals("null", ignoreCase = true)
    }


    @SuppressLint("RestrictedApi")
    fun createPlayer(
        context: Context,
        audioUrl: String,
        volume: Float = 1f,
    ): ExoPlayer {
        val dataSourceFactory = DefaultHttpDataSource.Factory()
        val extractorsFactory = DefaultExtractorsFactory().setConstantBitrateSeekingEnabled(true)
        val buildMediaItemMP4 = MediaItem.Builder()
            .setUri(Uri.parse(audioUrl))
            // .setMimeType(MimeTypes.BASE_TYPE_AUDIO)
            .build()
        val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory, extractorsFactory)
            .createMediaSource(buildMediaItemMP4)

        val player = ExoPlayer.Builder(context).build().apply {
            setMediaSource(videoSource)
        }
        player.prepare()
        player.volume = volume
        return player
    }


}