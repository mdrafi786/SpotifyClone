package com.mdrafi.spotifyclone.ui.fragments

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.bumptech.glide.RequestManager
import com.mdrafi.spotifyclone.R
import com.mdrafi.spotifyclone.data.models.Song
import com.mdrafi.spotifyclone.databinding.FragmentSongBinding
import com.mdrafi.spotifyclone.exoplayer.extensions.isPlaying
import com.mdrafi.spotifyclone.exoplayer.extensions.toSong
import com.mdrafi.spotifyclone.other.Status
import com.mdrafi.spotifyclone.ui.viewModels.MainViewModel
import com.mdrafi.spotifyclone.ui.viewModels.SongViewModel
import com.mdrafi.spotifyclone.utils.collectLifeCycleFlow
import com.mdrafi.spotifyclone.utils.constant.AppConstants.TIME_FORMAT
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@AndroidEntryPoint
class SongFragment : Fragment() {
    lateinit var binding: FragmentSongBinding

    @Inject
    lateinit var glide: RequestManager

    private val mainViewModel: MainViewModel by activityViewModels()
    private val songViewModel: SongViewModel by viewModels()

    private var curPlayingSong: Song? = null
    private var playBackState: PlaybackStateCompat? = null
    private var shouldUpdateSeekbar = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSongBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()
        with(binding) {
            ivPlayPauseDetail.setOnClickListener {
                curPlayingSong?.let { song ->
                    mainViewModel.playOrToggleSong(song, true)
                }
            }

            ivSkip.setOnClickListener {
                mainViewModel.skipToNextSong()
            }
            ivSkipPrevious.setOnClickListener {
                mainViewModel.skipToPreviousSong()
            }

            seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    position: Int,
                    fromUser: Boolean,
                ) {
                    if (fromUser)
                        tvCurTime.setCurPlayerTime(position.toLong())
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    shouldUpdateSeekbar = false
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    seekBar?.let {
                        mainViewModel.seekToPosition(it.progress.toLong())
                        shouldUpdateSeekbar = true
                    }
                }

            })
        }
    }

    private fun updateTitleAndSongImage(song: Song) {
        with(binding) {
            val title = "${song.title} - ${song.subtitle}"
            tvSongName.text = title
            glide.load(song.imageUrl).into(ivSongImage)
        }
    }

    private fun subscribeToObservers() {

        collectLifeCycleFlow(mainViewModel.mediaItemFlow){ result->
            when (result.status) {
                Status.SUCCESS -> {
                    result.data?.let { songs ->
                        if (curPlayingSong == null && songs.isNotEmpty()) {
                            val firstSong = songs[0]
                            curPlayingSong = firstSong
                            updateTitleAndSongImage(firstSong)
                        }
                    }
                }

                else -> Unit
            }
        }

        mainViewModel.curPlayingSong.observe(viewLifecycleOwner) {
            it?.let { mediaMetadataCompat ->
                curPlayingSong = mediaMetadataCompat.toSong()
                curPlayingSong?.let { song ->
                    updateTitleAndSongImage(song)
                }
            }
        }

        mainViewModel.playbackState.observe(viewLifecycleOwner) {
            playBackState = it
            binding.ivPlayPauseDetail.setImageResource(
                if (it?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play
            )
            binding.seekBar.progress = it?.position?.toInt() ?: 0
        }

        songViewModel.curSongDuration.observe(viewLifecycleOwner) {
            it?.let {
                with(binding) {
                    seekBar.max = it.toInt()
                    tvSongDuration.setCurPlayerTime(it)
                }
            }
        }

        songViewModel.curPlayerPosition.observe(viewLifecycleOwner) {
            it?.let {
                if (shouldUpdateSeekbar) {
                    binding.seekBar.progress = it.toInt()
                    binding.tvCurTime.setCurPlayerTime(it)
                }
            }
        }
    }

    private fun TextView.setCurPlayerTime(ms: Long) {
        val dateFormat = SimpleDateFormat(TIME_FORMAT, Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        text = dateFormat.format(ms)
    }

}