package com.app.mutliple.videos.compression.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.app.mutliple.videos.compression.R
import com.app.mutliple.videos.compression.model.MultiMedia
import com.app.mutliple.videos.compression.utils.Constants
import com.app.mutliple.videos.compression.utils.ImageUtility
import com.app.mutliple.videos.compression.utils.Utility
import com.makeramen.roundedimageview.RoundedImageView
import kotlinx.coroutines.*
import java.io.File

class StringMediaThumbAdapter(
    var followingList: ArrayList<MultiMedia>?,
    var callFrom : String,
    private var onThumbClick: thumdClick,
    var voiceNoteListCacheAdapters :File,
    private var pager_Media: ViewPager2? = null,
    var mContext: AppCompatActivity? = null
) :
    RecyclerView.Adapter<StringMediaThumbAdapter.ViewHolder>() {


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var ivMediaImage = view.findViewById<RoundedImageView>(R.id.ivMediaImage)!!
        var ivMediaImageBorder = view.findViewById<ImageView>(R.id.ivMediaImageBorder)!!
        var lytThumb = view.findViewById<ConstraintLayout>(R.id.lyt_thumb)!!
        var txtDuration : TextView= view.findViewById(R.id.txtDuration)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_media, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(pager_Media?.currentItem == position){
            holder.ivMediaImageBorder.visibility = View.VISIBLE
        } else {
            holder.ivMediaImageBorder.visibility = View.GONE
        }

        var profilePic : String?
        if(callFrom.equals("server",ignoreCase = true)) {
            if(!Utility.isEmpty(followingList?.get(position)?.media_thumb)) {

                val thumb=if(followingList?.get(position)?.media_thumb?.contains(Constants.VIDEO_FORMAT) == true){
                    val name = followingList?.get(position)?.media_thumb?.substring(0, followingList?.get(position)?.media_thumb?.indexOf(".")!!)
                    "$name"+ Constants.IMAGE_FORMAT
                }else{
                    followingList?.get(position)?.media_thumb
                }
                Log.d("TAG", "onBindViewHolder: media thumb available $thumb")
                profilePic =
                    "${voiceNoteListCacheAdapters.absolutePath}/${thumb}"
                val file =
                    File(voiceNoteListCacheAdapters, thumb!!)
                ImageUtility.loadMedianGlide(
                    profilePic,
                    imageView = holder.ivMediaImage
                )

                if(Utility.getMimeType(followingList?.get(position)?.media_file_name)?.isNotEmpty() == true
                    && Utility.getMimeType(followingList?.get(position)?.media_file_name)?.contains("video") == true){
                    holder.txtDuration.visibility=View.VISIBLE
                    Log.d(
                        "loadImages in Glide",
                        "MediaShowActivity -duration - ${followingList?.get(position)?.duration}"
                    )
                    holder.txtDuration.text= followingList?.get(position)?.duration.toString()
                }else
                    holder.txtDuration.visibility=View.GONE
            }else{
                Log.d("TAG", "onBindViewHolder: media thumb not available")
            }

        }


        holder.lytThumb.setOnClickListener {
            onThumbClick.onItemClick(holder.absoluteAdapterPosition)
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