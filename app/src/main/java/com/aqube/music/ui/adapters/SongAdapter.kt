package com.aqube.music.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.aqube.music.data.entities.Song
import com.aqube.music.databinding.ListItemBinding
import com.bumptech.glide.RequestManager
import javax.inject.Inject

class SongAdapter @Inject constructor(
    private val glide: RequestManager
) : BaseSongAdapter<Song>() {

    override val differ = AsyncListDiffer(this, diffCallback)

    override fun getViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    inner class SongViewHolder(private val binding: ListItemBinding) :
        RecyclerView.ViewHolder(binding.root), Binder<Song> {
        override fun bind(song: Song) {
            binding.apply {
                textViewTitle.text = song.title
                textViewSubTitle.text = song.subTitle
                glide.load(song.imageUrl).into(imageViewAlbum)
                root.setOnClickListener {
                    onItemClickListener?.let { itemClick ->
                        itemClick(song)
                    }
                }
            }
        }
    }
}