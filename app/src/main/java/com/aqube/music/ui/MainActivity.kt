package com.aqube.music.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.aqube.music.R
import com.aqube.music.data.entities.Song
import com.aqube.music.databinding.ActivityMainBinding
import com.aqube.music.exoplayer.toSong
import com.aqube.music.others.Status
import com.aqube.music.ui.adapters.SwipeSongAdapter
import com.aqube.music.ui.viewmodels.MainViewModel
import com.bumptech.glide.RequestManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var swipeSongAdapter: SwipeSongAdapter

    @Inject
    lateinit var glide: RequestManager

    private var currentPlayingSong: Song? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRecyclerView()
        subscribeToObservers()
    }

    private fun setupRecyclerView() {
        binding.viewPagerSong.apply {
            adapter = swipeSongAdapter
        }
    }

    private fun switchViewPagerToCurrentSong(song: Song) {
        val newSongIndex = swipeSongAdapter.songs.indexOf(song)
        if (newSongIndex != -1) {
            binding.viewPagerSong.currentItem = newSongIndex
            currentPlayingSong = song
        }
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(this) {
            it?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { songs ->
                            swipeSongAdapter.songs = songs
                            if (songs.isNotEmpty()) {
                                glide.load((currentPlayingSong ?: songs[0]).imageUrl)
                                    .into(binding.imageViewSongImage)
                            }
                            switchViewPagerToCurrentSong(currentPlayingSong ?: return@observe)
                        }
                    }
                    Status.LOADING -> Unit
                    Status.ERROR -> Unit
                }
            }
        }

        mainViewModel.curPlaySong.observe(this) {
            if (it == null) return@observe

            currentPlayingSong = it.toSong()
            glide.load(currentPlayingSong?.imageUrl).into(binding.imageViewSongImage)
            switchViewPagerToCurrentSong(currentPlayingSong ?: return@observe)
        }


    }
}

