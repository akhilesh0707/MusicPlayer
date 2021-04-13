package com.aqube.music.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.aqube.music.data.entities.Song
import com.aqube.music.databinding.SwipeItemBinding
import javax.inject.Inject

class SwipeSongAdapter @Inject constructor() : BaseSongAdapter<Song>() {

    override val differ = AsyncListDiffer(this, diffCallback)

    override fun getViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = SwipeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    inner class SongViewHolder(private val binding: SwipeItemBinding) :
        RecyclerView.ViewHolder(binding.root), Binder<Song> {
        override fun bind(song: Song) {
            binding.apply {
                val titleText = "${song.title} - ${song.subTitle}"
                textViewTitle.text = titleText
                root.setOnClickListener {
                    onItemClickListener?.let { itemClick ->
                        itemClick(song)
                    }
                }
            }
        }
    }
}