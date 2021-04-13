package com.aqube.music.ui.fragments

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.aqube.music.R
import com.aqube.music.data.entities.Song
import com.aqube.music.databinding.FragmentSongBinding
import com.aqube.music.exoplayer.isPlaying
import com.aqube.music.exoplayer.toSong
import com.aqube.music.others.Status
import com.aqube.music.ui.viewmodels.MainViewModel
import com.aqube.music.ui.viewmodels.SongViewModel
import com.bumptech.glide.RequestManager
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SongFragment : Fragment(R.layout.fragment_song) {

    private var _binding: FragmentSongBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var glide: RequestManager

    private lateinit var mainViewModel: MainViewModel
    private val songViewModel: SongViewModel by viewModels()
    private var currentPlayingSong: Song? = null
    private var playBackState: PlaybackStateCompat? = null
    private var shouldUpdateSeekBar = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSongBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        subscribeToObservers()
        setClickListeners()
    }

    private fun updateTitleAndSongImage(song: Song) {
        val title = "${song.title} - ${song.subTitle}"
        binding.textViewSongName.text = title
        glide.load(song.imageUrl).into(binding.imageViewSongImage)
    }

    private fun setClickListeners() {
        binding.imageViewPlayPause.setOnClickListener {
            currentPlayingSong?.let { song ->
                mainViewModel.playOrToggleSong(song, true)
            }
        }

        binding.imageViewSkipNext.setOnClickListener {
            mainViewModel.skipToNextSong()
        }

        binding.imageViewSkipPrevious.setOnClickListener {
            mainViewModel.skipToPreviousSong()
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    setCurrentPlayerTimeToTextView(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                shouldUpdateSeekBar = false
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    mainViewModel.seekTo(it.progress.toLong())
                    shouldUpdateSeekBar = true
                }
            }

        })
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) {
            it?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { songs ->
                            if (currentPlayingSong == null && songs.isNotEmpty()) {
                                currentPlayingSong = songs[0]
                                updateTitleAndSongImage(songs[0])
                            }
                        }
                    }
                    else -> Unit
                }
            }
        }

        mainViewModel.curPlaySong.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            currentPlayingSong = it.toSong()
            updateTitleAndSongImage(currentPlayingSong!!)
        }

        mainViewModel.playbackState.observe(viewLifecycleOwner) {
            playBackState = it
            binding.imageViewPlayPause.setImageResource(
                if (playBackState?.isPlaying == true) R.drawable.exo_controls_pause else R.drawable.exo_controls_play
            )
            binding.seekBar.progress = it?.position?.toInt() ?: 0
        }

        songViewModel.curPlayerPosition.observe(viewLifecycleOwner) {
            if (shouldUpdateSeekBar) {
                binding.seekBar.progress = it.toInt()
                setCurrentPlayerTimeToTextView(it)
            }
        }

        songViewModel.curSongDuration.observe(viewLifecycleOwner) {
            binding.seekBar.max = it.toInt()
            binding.textViewSongDuration.text = timeFormatter(it)
        }
    }

    private fun setCurrentPlayerTimeToTextView(time: Long) {
        binding.textViewCurrentTime.text = timeFormatter(time)
    }

    private fun timeFormatter(time: Long): String {
        val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        return dateFormat.format(time)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}