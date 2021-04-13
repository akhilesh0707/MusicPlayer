package com.aqube.music.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.aqube.music.R
import com.aqube.music.data.entities.Song
import com.aqube.music.databinding.ActivityMainBinding
import com.aqube.music.exoplayer.isPlaying
import com.aqube.music.exoplayer.toSong
import com.aqube.music.others.Status
import com.aqube.music.ui.adapters.SwipeSongAdapter
import com.aqube.music.ui.viewmodels.MainViewModel
import com.bumptech.glide.RequestManager
import com.google.android.material.snackbar.Snackbar
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

    private var playBackState: PlaybackStateCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRecyclerView()
        subscribeToObservers()

        binding.imageViewPlayPause.setOnClickListener {
            currentPlayingSong?.let {
                mainViewModel.playOrToggleSong(it, true)
            }
        }

        binding.viewPagerSong.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (playBackState?.isPlaying == true) {
                    mainViewModel.playOrToggleSong(swipeSongAdapter.songs[position])
                } else {
                    currentPlayingSong = swipeSongAdapter.songs[position]
                }
            }
        })
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

        mainViewModel.playbackState.observe(this) {
            playBackState = it
            binding.imageViewPlayPause.setImageResource(
                if (playBackState?.isPlaying == true) R.drawable.exo_controls_pause else R.drawable.exo_controls_play
            )
        }

        mainViewModel.isConnected.observe(this) {
            it?.getContentIfNotHandled().let { result ->
                when (result?.status) {
                    Status.ERROR -> showSnackBar(result.message)
                    else -> Unit
                }
            }
        }

        mainViewModel.networkError.observe(this) {
            it?.getContentIfNotHandled().let { result ->
                when (result?.status) {
                    Status.ERROR -> showSnackBar(result.message)
                    else -> Unit
                }
            }
        }
    }

    private fun showSnackBar(message: String?) {
        Snackbar.make(binding.rootLayout, message ?: "Unknown error occurred", Snackbar.LENGTH_LONG)
            .show()
    }
}

