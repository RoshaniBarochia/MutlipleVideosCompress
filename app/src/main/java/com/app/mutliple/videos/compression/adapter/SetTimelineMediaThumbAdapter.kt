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
import com.makeramen.roundedimageview.RoundedImageView
import com.app.mutliple.videos.compression.model.AppMedia
import com.app.mutliple.videos.compression.utils.ImageUtility
import com.app.mutliple.videos.compression.utils.Utility
import java.io.File

class SetTimelineMediaThumbAdapter(
    var followingList: ArrayList<AppMedia>?,
    var callFrom : String,
    var OnthumdClick: thumdClick,
    var voiceNoteListCacheAdapters :File,
    var pager_Media: ViewPager2? = null
) :
    RecyclerView.Adapter<SetTimelineMediaThumbAdapter.ViewHolder>() {

    var mContext: Context? = null
    var onFollowSuggestionItemCLick: ((pos: Int, action: String) -> Unit)? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var ivMediaImage: RoundedImageView = view.findViewById(R.id.ivMediaImage)
        var ivMediaImageBorder: ImageView = view.findViewById(R.id.ivMediaImageBorder)
        var lyt_thumb: ConstraintLayout = view.findViewById(R.id.lyt_thumb)
        var txtDuration : TextView= view.findViewById(R.id.txtDuration)
        var txtTime : TextView= view.findViewById(R.id.txtTime)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        mContext = parent.context
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_media_time_line_thumb, parent, false)
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
//            holder.itemView.visibility = View.GONE
        }else
            holder.txtDuration.visibility=View.GONE
        Log.d(
            "loadImagesinGlide",
            "MediaShowActivity -loadMediainGlide - $profilePic &&& ID=${followingList?.get(position)?.id}"
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
        if(callFrom.equals("server",ignoreCase = true) && (followingList?.get(position)?.time!! > -1)) {
            Log.d(
                "loadImagesinGlide",
                "MediaShowActivity -loadMediainGlide if - ${followingList?.get(position)?.time}"
            )
            holder.txtTime.visibility=View.VISIBLE
            holder.txtTime.text=Utility.stringForDuration(followingList?.get(position)?.time?.toLong()!!)
        }else {
            holder.txtTime.visibility=View.INVISIBLE
        }
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