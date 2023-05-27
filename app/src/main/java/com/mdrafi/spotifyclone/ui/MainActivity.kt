package com.mdrafi.spotifyclone.ui

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.bumptech.glide.RequestManager
import com.google.android.material.snackbar.Snackbar
import com.mdrafi.spotifyclone.R
import com.mdrafi.spotifyclone.adapters.SwipeSongAdapter
import com.mdrafi.spotifyclone.data.models.Song
import com.mdrafi.spotifyclone.databinding.ActivityMainBinding
import com.mdrafi.spotifyclone.exoplayer.extensions.isPlaying
import com.mdrafi.spotifyclone.exoplayer.extensions.toSong
import com.mdrafi.spotifyclone.other.Status.ERROR
import com.mdrafi.spotifyclone.other.Status.SUCCESS
import com.mdrafi.spotifyclone.ui.viewModels.MainViewModel
import com.mdrafi.spotifyclone.utils.collectLifeCycleFlow
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var glide: RequestManager

    @Inject
    lateinit var swipeSongAdapter: SwipeSongAdapter
    private val mainViewModel: MainViewModel by viewModels()
    private var curPlayingSong: Song? = null

    private lateinit var binding: ActivityMainBinding
    private var curPlayBackState: PlaybackStateCompat? = null

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        subscribeToObservers()
        binding.vpSong.adapter = swipeSongAdapter

        // handled play pause button click
        binding.ivPlayPause.setOnClickListener {
            curPlayingSong?.let { song ->
                mainViewModel.playOrToggleSong(song, true)
            }
        }

        // handled song state on swipe
        binding.vpSong.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val song = swipeSongAdapter.songs[position]
                if (curPlayBackState?.isPlaying == true) {
                    mainViewModel.playOrToggleSong(song)
                } else {
                    curPlayingSong = song
                }
            }
        })

        swipeSongAdapter.setItemClickListener {
            navController.navigate(R.id.globalActionToSongFragment)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment -> showBottomBar()
                R.id.songFragment -> showBottomBar(false)
                else -> showBottomBar()
            }
        }
    }

    private fun showBottomBar(value: Boolean = true) {
        with(binding) {
            ivCurSongImage.isVisible = value
            vpSong.isVisible = value
            ivPlayPause.isVisible = value
        }
    }

    private fun switchViewPagerToCurPlayingSong(song: Song) {
        // It will give -1 if song not found
        val newSongIndex = swipeSongAdapter.songs.indexOf(song)
        if (newSongIndex != -1) {
            binding.vpSong.currentItem = newSongIndex
            curPlayingSong = song
        }
    }

    private fun subscribeToObservers() {

        collectLifeCycleFlow(mainViewModel.mediaItemFlow) { result ->
            when (result.status) {

                SUCCESS -> {
                    result.data?.let { songs ->
                        swipeSongAdapter.songs = songs
                        if (songs.isNotEmpty()) {
                            curPlayingSong = songs[0]
                            glide.load((curPlayingSong ?: songs[0]).imageUrl)
                                .into(binding.ivCurSongImage)
                        }
                        switchViewPagerToCurPlayingSong(
                            curPlayingSong ?: return@collectLifeCycleFlow
                        )
                    }
                }

                else -> Unit
            }
        }

//        mainViewModel.mediaItems.observe(this) {
//            it?.let { result ->
//                when (result.status) {
//                    SUCCESS -> {
//                        result.data?.let { songs ->
//                            swipeSongAdapter.songs = songs
//                            if (songs.isNotEmpty()) {
//                                curPlayingSong = songs[0]
//                                glide.load((curPlayingSong ?: songs[0]).imageUrl)
//                                    .into(binding.ivCurSongImage)
//                            }
//                            switchViewPagerToCurPlayingSong(curPlayingSong ?: return@observe)
//                        }
//                    }
//
//                    else -> Unit
//                }
//            }
//        }

        mainViewModel.curPlayingSong.observe(this) {
            if (it == null) return@observe
            it.toSong()?.let { song ->
                curPlayingSong = song
                glide.load(song.imageUrl).into(binding.ivCurSongImage)
                switchViewPagerToCurPlayingSong(song)
            }
        }

        mainViewModel.playbackState.observe(this) {
            curPlayBackState = it
            binding.ivPlayPause.setImageResource(
                if (it?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play
            )
        }

        mainViewModel.isConnected.observe(this) {
            it.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    ERROR -> Snackbar.make(
                        binding.root,
                        result.message ?: getString(R.string.unknown_error),
                        Snackbar.LENGTH_LONG
                    ).show()

                    else -> Unit
                }
            }
        }

        mainViewModel.networkError.observe(this) {
            it.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    ERROR -> Snackbar.make(
                        binding.root,
                        result.message ?: getString(R.string.unknown_error),
                        Snackbar.LENGTH_LONG
                    ).show()

                    else -> Unit
                }
            }
        }
    }
}