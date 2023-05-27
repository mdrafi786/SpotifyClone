package com.mdrafi.spotifyclone.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter<T : Any, V : ViewDataBinding>(
    itemSame: (T, T) -> Boolean,
    contentSame: (T, T) -> Boolean,
) : ListAdapter<T, BaseAdapter.BaseViewHolder<V>>(object : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return itemSame(oldItem, newItem)
    }

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return contentSame(oldItem, newItem)
    }
}) {

    class BaseViewHolder<V> constructor(val binding: V) :
        RecyclerView.ViewHolder((binding as ViewDataBinding).root)

    var songs: List<T>
        get() = currentList
        set(value) {
            submitList(value)
        }

    protected var onItemClickListener: ((T) -> Unit)? = null

    fun setItemClickListener(listener: (T) -> Unit) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<V> {
        return BaseViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                viewType,
                parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    override fun getItemViewType(position: Int): Int {
        return getLayoutIdForPosition(position)
    }

    protected abstract fun getLayoutIdForPosition(position: Int): Int
}