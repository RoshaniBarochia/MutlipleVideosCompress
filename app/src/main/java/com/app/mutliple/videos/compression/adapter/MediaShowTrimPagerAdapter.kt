package com.app.mutliple.videos.compression.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.mutliple.videos.compression.R
import com.app.mutliple.videos.compression.model.AppMedia

class MediaShowTrimPagerAdapter(
    private var list: ArrayList<AppMedia>?
) : RecyclerView.Adapter<MediaShowTrimPagerAdapter.ViewHolder>() {
    private var recyclerView: RecyclerView? = null


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val rootView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_media_pager_trim, parent, false)
        return ViewHolder(rootView)
    }

    class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {}


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
}
