package com.mdrafi.spotifyclone.adapters

import com.bumptech.glide.RequestManager
import com.mdrafi.spotifyclone.R
import com.mdrafi.spotifyclone.data.models.Song
import com.mdrafi.spotifyclone.databinding.ListItemBinding
import javax.inject.Inject

class SongAdapter @Inject constructor(
    private val glide: RequestManager,
) : BaseAdapter<Song, ListItemBinding>(
    itemSame = { oldItem, newItem ->
        oldItem.mediaId == newItem.mediaId
    },
    contentSame = { oldItem, newItem ->
        oldItem.hashCode() == newItem.hashCode()
    }
) {

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemBinding>, position: Int) {
        val song = songs[position]
        holder.binding.apply {
            tvPrimary.text = song.title
            tvSecondary.text = song.subtitle
            glide.load(song.imageUrl).into(ivItemImage)
        }
        holder.itemView.setOnClickListener {
            onItemClickListener?.let { click ->
                click(song)
            }
        }
    }

    override fun getLayoutIdForPosition(position: Int) = R.layout.list_item
}