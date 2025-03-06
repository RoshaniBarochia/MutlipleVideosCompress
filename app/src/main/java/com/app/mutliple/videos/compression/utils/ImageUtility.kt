package com.app.mutliple.videos.compression.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import com.app.mutliple.videos.compression.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Runnable
import kotlin.coroutines.coroutineContext

object ImageUtility {
    fun loadImagineGlide(
        imageUrl: String?,
        placeHolder: Int? = null,
        imageView: ImageView? = null,
        progressBar: ProgressBar? = null,
        isGIF: Boolean? = false,
        height: Int = 512,
        width: Int = 512,
    ) {

        if (imageView == null) {
            return
        }
        if (imageView != null) {
            if (!isGIF!!) {
                Glide.with(imageView.context.applicationContext)
                    .setDefaultRequestOptions(
                        RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(placeHolder!!)
                            .error(placeHolder)
                            .centerCrop()
                    )
                    .load(imageUrl)
                    .also {
                        it.into(imageView)
                    }

            } else {
                Glide.with(imageView.context.applicationContext).asGif()
                    .load(placeHolder).into(imageView)

            }
        }
    }


    fun loadMedianGlide(
        imageUrl: String?,
        placeHolder: Int = R.color.transparent,
        imageView: ImageView? = null,
        progressBar: ProgressBar? = null,
        isSetImage: Boolean = false,
        pos:Int = 0
    ) {
        if (imageView == null) {
            return
        }
        val mContext = imageView.context

        if (progressBar != null) {
            progressBar.visibility = View.VISIBLE
        }

        val requestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(placeHolder)
            .error(placeHolder)

        Glide.with(mContext.applicationContext)
            .setDefaultRequestOptions(requestOptions)
            .load(imageUrl)
            .dontTransform()
            .listener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any,
                    target: Target<Drawable?>,
                    isFirstResource: Boolean,
                ): Boolean {
                    try {
                        if (progressBar != null) {
                            progressBar.visibility = View.GONE
                        }
                    } catch (e: Exception) {

                    }
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any,
                    target: Target<Drawable?>,
                    dataSource: DataSource,
                    isFirstResource: Boolean,
                ): Boolean {
                    imageView.clearAnimation()
                    try {
                        if (progressBar != null) {
                            progressBar.visibility = View.GONE
                        }
                    } catch (e: Exception) {

                    }
                    if(isSetImage){
                        if (resource?.intrinsicHeight!! >= resource.intrinsicWidth) {
                            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                            val viewWidth: Float = imageView.width.toFloat()
                            val viewHeight: Float = imageView.height.toFloat()
                            val drawableWidth = resource.intrinsicWidth
                            val drawableHeight = resource.intrinsicHeight

                            val widthScale = viewWidth / drawableWidth
                            val heightScale = viewHeight / drawableHeight
                            val scale = widthScale.coerceAtLeast(heightScale)

                            val baseMatrix: Matrix = Matrix()
                            baseMatrix.reset()
                            baseMatrix.postScale(scale, scale)
                            imageView.imageMatrix = baseMatrix
                            imageView.scaleType = ImageView.ScaleType.MATRIX
                            imageView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                            imageView.requestLayout()

                        }
                        else if(pxToDp(imageView.context, resource.intrinsicHeight.toFloat()) <= imageView.context.resources?.getDimension(
                                com.intuit.sdp.R.dimen._180sdp)!!){
                            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                            imageView.layoutParams.height = mContext!!.resources.getDimensionPixelOffset(
                                com.intuit.sdp.R.dimen._160sdp)
                            imageView.requestLayout()
                        }
                    }
                    return false
                }
            })
            .into(imageView)

    }

    fun pxToDp(context: Context, px: Float): Float {
        return px / context.resources.displayMetrics.density
    }

    fun loadMediaInCards(
        imageUrl: String?,
        placeHolder: Int = R.color.transparent,
        imageView: ImageView? = null,
        progressBar: ProgressBar? = null,
        isSetImage: Boolean = false
    ) {
        if (imageView == null) {
            return
        }

        val mContext = imageView.context

        if (progressBar != null) {
            progressBar.visibility = View.VISIBLE
        }

        val requestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(placeHolder)
            .error(placeHolder)
        Glide.with(mContext.applicationContext)
            .setDefaultRequestOptions(requestOptions)
            .load(imageUrl)
            .listener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any,
                    target: Target<Drawable?>,
                    isFirstResource: Boolean,
                ): Boolean {
                    try {
                        if (progressBar != null) {
                            progressBar.visibility = View.GONE
                        }
                    } catch (e: Exception) {
                    }
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any,
                    target: Target<Drawable?>,
                    dataSource: DataSource,
                    isFirstResource: Boolean,
                ): Boolean {
                    imageView.clearAnimation()
                    try {
                        if (progressBar != null) {
                            progressBar.visibility = View.GONE
                        }
                    } catch (e: Exception) {
                    }
                    return false
                }
            })
            .into(imageView)

    }


    fun clearGlideCache(mContext: Context) {
        Thread(Runnable {
            Glide.get(mContext.applicationContext).clearDiskCache() //1
        }).start()
        Glide.get(mContext).clearMemory() //2
        //1. You can only call clearDiskCache() in background. A simple solution is to call this method in a thread.
        //2. You can only call clearMemory() in the main thread.
    }

    var TAG = "Picasso"
    fun checkIfImageExists(file: File): Boolean {
        var b: Bitmap? = null
        val path = file.absolutePath
        if (path != null)
            b = BitmapFactory.decodeFile(path)
        return !(b == null || b.equals(""))
    }

    fun compressImage(mContext: Context, outputSaveDirectory: File, imagePath: String): File {
        val photoFile = File(
            outputSaveDirectory,
            System.currentTimeMillis().toString() + Constants.IMAGE_FORMAT
        )

        try {
            photoFile.createNewFile()
            val photo: Bitmap =
                ImageCompression.compressImage(mContext, imagePath)
            val file = File(imagePath)
            val fos = FileOutputStream(photoFile)
            when {

                file.length() <= 1000000 -> {
                    photo.compress(Bitmap.CompressFormat.JPEG, 70, fos)
                }
                file.length() in 1000001..4000000 -> {
                    photo.compress(Bitmap.CompressFormat.JPEG, 60, fos)
                }
                file.length() in 4000001..6000000 -> {
                    photo.compress(Bitmap.CompressFormat.JPEG, 50, fos)
                }
                else -> {
                    photo.compress(Bitmap.CompressFormat.JPEG, 40, fos)
                }
            }
            fos.close()
            Utility.refreshGallery(photoFile.absolutePath, mContext)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return photoFile
    }


    suspend fun retrieveVideoFrameFromVideo(videoPath: String?): Bitmap? {
        return withContext(coroutineContext) {
            var bitmap: Bitmap? = null
            var mediaMetadataRetriever: MediaMetadataRetriever? = null
            try {
                mediaMetadataRetriever = MediaMetadataRetriever()
                if (Build.VERSION.SDK_INT >= 14) mediaMetadataRetriever.setDataSource(
                    videoPath,
                    HashMap()
                ) else mediaMetadataRetriever.setDataSource(videoPath)
                bitmap =
                    mediaMetadataRetriever.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            } finally {
                mediaMetadataRetriever?.release()
            }
            bitmap
        }
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        var width = drawable.intrinsicWidth
        width = if (width > 0) width else 1
        var height = drawable.intrinsicHeight
        height = if (height > 0) height else 1
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}