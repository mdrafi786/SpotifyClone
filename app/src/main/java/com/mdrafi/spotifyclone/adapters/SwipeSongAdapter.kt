package com.mdrafi.spotifyclone.adapters

import com.mdrafi.spotifyclone.R
import com.mdrafi.spotifyclone.data.models.Song
import com.mdrafi.spotifyclone.databinding.SwipeItemBinding
import javax.inject.Inject

class SwipeSongAdapter @Inject constructor() : BaseAdapter<Song, SwipeItemBinding>(
    itemSame = { oldItem, newItem ->
        oldItem.mediaId == newItem.mediaId
    },
    contentSame = { oldItem, newItem ->
        oldItem.hashCode() == newItem.hashCode()
    }
) {
    override fun onBindViewHolder(
        holder: BaseViewHolder<SwipeItemBinding>,
        position: Int,
    ) {
        val song = songs[position]
        holder.binding.apply {
            val text = "${song.title} - ${song.subtitle}"
            tvPrimary.text = text
        }
        holder.itemView.setOnClickListener {
            onItemClickListener?.let { click ->
                click(song)
            }
        }
    }

    override fun getLayoutIdForPosition(position: Int) = R.layout.swipe_item
}