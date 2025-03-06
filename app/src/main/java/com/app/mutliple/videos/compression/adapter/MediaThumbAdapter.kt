package com.app.mutliple.videos.compression.adapter

import android.content.Context
import android.util.Log
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
    var followingList: ArrayList<AppMedia>?,
    var callFrom : String,
    var OnthumdClick: thumdClick,
    var voiceNoteListCacheAdapters :File,
    var pager_Media: ViewPager2? = null
) :
    RecyclerView.Adapter<MediaThumbAdapter.ViewHolder>() {



    var mContext: Context? = null
    var onFollowSuggestionItemCLick: ((pos: Int, action: String) -> Unit)? = null
    var selectedPOS = 0

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var ivMediaImage = view.findViewById<RoundedImageView>(R.id.ivMediaImage)
        var ivMediaImageBorder = view.findViewById<ImageView>(R.id.ivMediaImageBorder)
        var lyt_thumb = view.findViewById<ConstraintLayout>(R.id.lyt_thumb)
        var txtDuration : TextView= view.findViewById(R.id.txtDuration)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        mContext = parent.context
        var view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_media, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(pager_Media?.currentItem == position){
            holder.ivMediaImageBorder.visibility = View.VISIBLE
        } else {
            holder.ivMediaImageBorder.visibility = View.GONE
        }

        var profilePic : String? =null

        profilePic = followingList?.get(position)?.absolute_path
        if(Utility.getMimeType(profilePic)?.isNotEmpty() == true
            && Utility.getMimeType(profilePic)?.contains("video") == true){
            holder.txtDuration.visibility=View.VISIBLE
            holder.txtDuration.text=Utility.stringForDuration( Utility.getDuration(File(profilePic), mContext!!))
        }else
            holder.txtDuration.visibility=View.GONE
        Log.d(
            "loadImagesinGlide",
            "MediaShowActivity -loadMediainGlide - $profilePic"
        )
        if(profilePic!=null)
            ImageUtility.loadMedianGlide(
                profilePic,
                imageView = holder.ivMediaImage
            )
        Log.d(
            "loadImagesinGlide",
            "MediaShowActivity -loadMediainGlide out - ${followingList?.get(position)?.time}"
        )
        holder.lyt_thumb.setOnClickListener {
            OnthumdClick.onItemClick(holder.absoluteAdapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return followingList?.size!!
    }

    interface thumdClick{
        fun onItemClick(position: Int)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}