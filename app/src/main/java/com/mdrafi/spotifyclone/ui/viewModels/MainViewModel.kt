package com.mdrafi.spotifyclone.ui.viewModels

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mdrafi.spotifyclone.data.models.Song
import com.mdrafi.spotifyclone.exoplayer.MusicServiceConnection
import com.mdrafi.spotifyclone.exoplayer.extensions.isPlayEnabled
import com.mdrafi.spotifyclone.exoplayer.extensions.isPlaying
import com.mdrafi.spotifyclone.exoplayer.extensions.isPrepared
import com.mdrafi.spotifyclone.other.Resource
import com.mdrafi.spotifyclone.utils.constant.AppConstants.MEDIA_ROOT_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val musicServiceConnection: MusicServiceConnection,
) : ViewModel() {

    private val _mediaItemFlow =
        MutableStateFlow<Resource<List<Song>>>(Resource.loading(null))
    val mediaItemFlow = _mediaItemFlow.asStateFlow()


    val isConnected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkError
    val playbackState = musicServiceConnection.playbackState
    val curPlayingSong = musicServiceConnection.curPlayingSong

    private val subscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {

        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>,
        ) {
            val items = children.map {
                Song(
                    mediaId = it.mediaId ?: "",
                    title = it.description.title.toString(),
                    subtitle = it.description.subtitle.toString(),
                    songUrl = it.description.mediaUri.toString(),
                    imageUrl = it.description.iconUri.toString()
                )
            }
            _mediaItemFlow.update {
                Resource.success(items)
            }
//            _mediaItems.postValue(Resource.success(items))
        }
    }

    init {
//        _mediaItems.postValue(Resource.loading(null))
        _mediaItemFlow.update {
            Resource.loading(null)
        }
        // subscribe to rootId(Id of Playlist root)
        musicServiceConnection.subscribe(
            MEDIA_ROOT_ID,
            subscriptionCallback
        )
    }

    fun skipToNextSong() = musicServiceConnection.transportControls.skipToNext()

    fun skipToPreviousSong() = musicServiceConnection.transportControls.skipToPrevious()

    fun seekToPosition(pos: Long) = musicServiceConnection.transportControls.seekTo(pos)

    fun playOrToggleSong(mediaItem: Song, toggle: Boolean = false) {
        val isPrepared = playbackState.value?.isPrepared ?: false
        if (isPrepared && mediaItem.mediaId == curPlayingSong.value?.getString(METADATA_KEY_MEDIA_ID)) {
            // for toggle to play and pause state
            playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> if (toggle) musicServiceConnection.transportControls.pause()
                    playbackState.isPlayEnabled -> musicServiceConnection.transportControls.play()
                }
            }
        } else {
            // for play new song
            musicServiceConnection.transportControls.playFromMediaId(mediaItem.mediaId, null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unsubscribe(MEDIA_ROOT_ID, subscriptionCallback)
    }
}