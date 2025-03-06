package com.app.mutliple.videos.compression.adapter


import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.app.mutliple.videos.compression.R
import com.app.mutliple.videos.compression.model.AppMedia
import com.app.mutliple.videos.compression.utils.Constants
import com.app.mutliple.videos.compression.utils.ImageUtility
import com.app.mutliple.videos.compression.utils.OnCommonClick
import com.app.mutliple.videos.compression.utils.Utility
import com.google.android.exoplayer2.ui.PlayerView
import com.makeramen.roundedimageview.RoundedImageView
import java.io.File

class MediaSetTimeLineAdapter : RecyclerView.Adapter<MediaSetTimeLineAdapter.ViewHolder> {
    private var list: ArrayList<AppMedia>?
    private var mContext: Context
    private var callFrom: String
    private var voiceNoteListCacheAdapters: File
    var commonClick: OnCommonClick
    constructor(
        list: ArrayList<AppMedia>?,
        mContext: Context,
        callFrom: String,
        voiceNoteListCacheAdapters: File,
    ) : super() {
        this.list = list
        this.mContext = mContext
        this.callFrom = callFrom
        this.voiceNoteListCacheAdapters = voiceNoteListCacheAdapters
        this.commonClick = mContext as OnCommonClick
    }

    private val TAG = "MediaShowPagerAdapter"

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val rootView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_media_set_timeline, parent, false)
        return ViewHolder(rootView)
    }

    class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
        val playIndicatorView : ImageView =rootView.findViewById(R.id.playIndicatorView)
        val imgAttachedImage : RoundedImageView =rootView.findViewById(R.id.imgAttachedImage)
        val ivProgressbarLogin : ProgressBar =rootView.findViewById(R.id.ivProgressbarLogin)
        val videoViewAttached : PlayerView =rootView.findViewById(R.id.videoViewAttached)
        fun onPrepare() {
        }

        fun onDetach() {
        }
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list?.get(position)?.absolute_path
        if(Utility.getMimeType(model)?.isNotEmpty() == true && Utility.getMimeType(model)?.contains("video") == true) {
            //holder.playIndicatorView.visibility=View.VISIBLE
            holder.playIndicatorView.setOnClickListener {
                commonClick.onViewClick(position, Constants.ACTION_CLICK, holder.playIndicatorView, "")
            }
            holder.itemView.setOnClickListener {
                commonClick.onViewClick(position, Constants.ACTION_CLICK, holder.playIndicatorView, "")
            }
        }
        else{
            holder.playIndicatorView.visibility=View.GONE
            holder.imgAttachedImage.visibility = View.VISIBLE
            holder.videoViewAttached.visibility=View.GONE


            ImageUtility.loadMedianGlide(
                model!!,
                imageView =  holder.imgAttachedImage,
                progressBar =  holder.ivProgressbarLogin
            )
        }
    }

    override fun getItemCount(): Int {
        return if (list != null)
            list?.size!!
        else
            0
    }

    override fun onViewAttachedToWindow(@NonNull holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)
        Log.i("GoogleIO", "ViewAttached")
       holder.onPrepare()

    }

    override fun onViewDetachedFromWindow(@NonNull holder: ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.onDetach()
    }



}
