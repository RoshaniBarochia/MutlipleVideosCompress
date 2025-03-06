package com.app.mutliple.videos.compression.adapter

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.app.mutliple.videos.compression.R
import com.app.mutliple.videos.compression.model.AppMedia
import java.io.File

class MediaShowTrimPagerAdapter//his.commonClick = mContext as OnCommonClick
    (
    private var list: ArrayList<AppMedia>?,
    private var mContext: Context,
    private var callFrom: String,
    private var voiceNoteListCacheAdapter: File
) : RecyclerView.Adapter<MediaShowTrimPagerAdapter.ViewHolder>()
{
    private var isFirstTime=true
    private val TAG = "MediaShowPagerAdapter"
    private var recyclerView: RecyclerView? = null


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val rootView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_media_pager_trim, parent, false)
        return ViewHolder(rootView)
    }

    class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {

        fun onPrepare() {

        }


        fun onDetach() {

        }

    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list?.get(position)

    }
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = null
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


    private fun myUri(originalUri: Uri,file : File): Uri? {
        var returnedUri: Uri? = null
        returnedUri = if (originalUri.scheme == null) {
            Uri.fromFile(file)
        } else {
            originalUri
        }
        return returnedUri
    }




}
