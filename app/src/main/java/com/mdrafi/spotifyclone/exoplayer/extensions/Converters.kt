package com.mdrafi.spotifyclone.exoplayer.extensions

import android.support.v4.media.MediaMetadataCompat
import com.mdrafi.spotifyclone.data.models.Song
import com.mdrafi.spotifyclone.utils.constant.AppConstants.EMPTY

fun MediaMetadataCompat.toSong(): Song? {
    return description?.let {
        Song(
            title = it.title.toString(),
            subtitle = it.subtitle.toString(),
            imageUrl = it.iconUri.toString(),
            mediaId = it.mediaId ?: EMPTY,
            songUrl = it.mediaUri.toString()
        )
    }
}

fun Song.toMediaMetadataCompact(): MediaMetadataCompat {
    return MediaMetadataCompat.Builder()
        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, subtitle)
        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId)
        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
        .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, title)
        .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, imageUrl)
        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, songUrl)
        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, imageUrl)
        .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, subtitle)
        .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, subtitle)
        .build()
}