package com.app.mutliple.videos.compression.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.app.mutliple.videos.compression.R
import com.app.mutliple.videos.compression.model.AppMedia
import com.app.mutliple.videos.compression.utils.ImageUtility
import com.app.mutliple.videos.compression.utils.Utility
import com.makeramen.roundedimageview.RoundedImageView
import java.io.File

class MediaThumbAdapter(
    private var mediaList: ArrayList<AppMedia>?,
    private var onClick: ItemMediaClick ?= null,
    private var pgMedia: ViewPager2? = null
) :
    RecyclerView.Adapter<MediaThumbAdapter.ViewHolder>() {
    private var mContext: Context? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var ivMediaImage: RoundedImageView = view.findViewById(R.id.ivMediaImage)
        var ivMediaImageBorder: ImageView = view.findViewById(R.id.ivMediaImageBorder)
        var layoutThumb: ConstraintLayout = view.findViewById(R.id.lyt_thumb)
        var txtDuration : TextView= view.findViewById(R.id.txtDuration)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        mContext = parent.context
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_media, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(pgMedia?.currentItem == position){
            holder.ivMediaImageBorder.visibility = View.VISIBLE
        } else {
            holder.ivMediaImageBorder.visibility = View.GONE
        }

        var profilePic : String? =null

        profilePic = mediaList?.get(position)?.absolute_path
        if(Utility.getMimeType(profilePic)?.isNotEmpty() == true
            && Utility.getMimeType(profilePic)?.contains("video") == true){
            holder.txtDuration.visibility=View.VISIBLE
            holder.txtDuration.text=Utility.stringForDuration( Utility.getDuration(File(profilePic), mContext!!))
        }else
            holder.txtDuration.visibility=View.GONE

        if(profilePic!=null)
            ImageUtility.loadMedianGlide(
                profilePic,
                imageView = holder.ivMediaImage
            )

        holder.layoutThumb.setOnClickListener {
            onClick?.let {  it.onItemClick(holder.absoluteAdapterPosition) }
        }
    }

    override fun getItemCount(): Int {
        return mediaList?.size!!
    }

    interface ItemMediaClick{
        fun onItemClick(position: Int)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}