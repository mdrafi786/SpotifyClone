package com.mdrafi.spotifyclone.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mdrafi.spotifyclone.R
import com.mdrafi.spotifyclone.adapters.SongAdapter
import com.mdrafi.spotifyclone.databinding.FragmentHomeBinding
import com.mdrafi.spotifyclone.other.Resource
import com.mdrafi.spotifyclone.other.Status
import com.mdrafi.spotifyclone.ui.viewModels.MainViewModel
import com.mdrafi.spotifyclone.utils.collectLifeCycleFlow
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val mainViewModel: MainViewModel by activityViewModels()

    lateinit var binding: FragmentHomeBinding

    @Inject
    lateinit var songAdapter: SongAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
        subscribeToObservers()
    }

    private fun setAdapter() = binding.rvAllSongs.apply {
        adapter = songAdapter
        layoutManager = LinearLayoutManager(requireContext())
        songAdapter.setItemClickListener {
            mainViewModel.playOrToggleSong(it)
        }
    }

    private fun subscribeToObservers() {
        collectLifeCycleFlow(mainViewModel.mediaItemFlow) { result ->
            when (result.status) {
                Status.LOADING -> binding.allSongsProgressBar.isVisible = true

                Status.SUCCESS -> {
                    binding.allSongsProgressBar.isVisible = false
                    result.data?.let { songs ->
                        songAdapter.songs = songs
                    }
                }

                Status.ERROR -> Unit
            }
        }
//        mainViewModel.mediaItems.observe(viewLifecycleOwner) { result ->
//            when (result.status) {
//                Status.SUCCESS -> {
//                    binding.allSongsProgressBar.isVisible = false
//                    result.data?.let { songs ->
//                        songAdapter.songs = songs
//                    }
//                }
//
//                Status.LOADING -> binding.allSongsProgressBar.isVisible = true
//                Status.ERROR -> Unit
//            }
//        }
    }
}