package com.app.mutliple.videos.compression.adapter


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.app.mutliple.videos.compression.R
import com.app.mutliple.videos.compression.model.MultiMedia
import com.app.mutliple.videos.compression.utils.Constants
import com.app.mutliple.videos.compression.utils.ImageUtility
import com.app.mutliple.videos.compression.utils.OnCommonClick
import com.app.mutliple.videos.compression.utils.Utility
import com.app.mutliple.videos.compression.zoom_image.ZoomageView
import com.google.android.exoplayer2.ui.PlayerView
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File

private const val TAG = "MediaShowPagerAdapter"
class MediaShowRecyclerViewAdapter(
    private var list: ArrayList<MultiMedia>?,
    private var mContext: AppCompatActivity,
    private var callFrom: String,
    private var voiceNoteListCacheAdapters: File,
    commonClicks: OnCommonClick
) : RecyclerView.Adapter<MediaShowRecyclerViewAdapter.ViewHolder>() {
    var commonClick: OnCommonClick = commonClicks



    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val rootView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_media_recycler, parent, false)
        return ViewHolder(rootView)
    }

    class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
        val playIndicatorView : ImageView =rootView.findViewById(R.id.playIndicatorView)
        val imgAttachedImage : ZoomageView =rootView.findViewById(R.id.imgAttachedImage)
        val ivProgressbarLogin : ProgressBar =rootView.findViewById(R.id.ivProgressbarLogin)
        val videoViewAttached : PlayerView =rootView.findViewById(R.id.videoViewAttached)
        fun onPrepare() {
        }

        fun onDetach() {
        }
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list?.get(position)?.media_file_name
        if(Utility.getMimeType(model)?.isNotEmpty() == true && Utility.getMimeType(model)?.contains("video") == true && callFrom.equals("server",ignoreCase = true)) {
            holder.playIndicatorView.setOnClickListener {
                commonClick.onViewClick(position, Constants.PLAY, holder.playIndicatorView, "")
            }
            holder.itemView.setOnClickListener {
                commonClick.onViewClick(position, Constants.PLAY, holder.playIndicatorView, "")
            }
        }
        else{
            holder.playIndicatorView.visibility=View.GONE
            holder.imgAttachedImage.visibility = View.VISIBLE
            holder.videoViewAttached.visibility=View.GONE

            var profilePic: String
            if (callFrom.equals("server", ignoreCase = true)) {
                Log.d(TAG, "onBindViewHolder: call from :: $callFrom :: model :: $model")
                val mediaPic =
                    "${voiceNoteListCacheAdapters.absolutePath}/${
                        model
                    }"
                val file = model?.let {
                    File(
                        voiceNoteListCacheAdapters,
                        it
                    )
                }

                if (file?.let { ImageUtility.checkIfImageExists(it) } == true) {
                    profilePic =mediaPic
                    ImageUtility.loadMedianGlide(
                        profilePic,
                        imageView =  holder.imgAttachedImage,
                        progressBar =  holder.ivProgressbarLogin
                    )
                }
                else {

                }
            } else {
                profilePic = model!!
                ImageUtility.loadMedianGlide(
                    profilePic,
                    imageView =  holder.imgAttachedImage,
                    progressBar =  holder.ivProgressbarLogin
                )
                Log.d(
                    "loadImages inGlide",
                    "MediaShowActivity -loadMedia inGlide - $profilePic"
                )
            }

        }
    }

    override fun getItemCount(): Int {
        return if (list != null)
            list?.size!!
        else
            0
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)
        Log.i("GoogleIO", "ViewAttached")
       holder.onPrepare()

    }

    override fun onViewDetachedFromWindow( holder: ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.onDetach()
    }



}
