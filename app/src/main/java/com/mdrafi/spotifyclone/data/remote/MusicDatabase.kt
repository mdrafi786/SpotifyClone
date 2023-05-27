package com.mdrafi.spotifyclone.data.remote

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mdrafi.spotifyclone.data.models.Song
import com.mdrafi.spotifyclone.utils.constant.AppConstants.SONG_COLLECTION
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class MusicDatabase {
    private val songCollection = Firebase.firestore.collection(SONG_COLLECTION)

    suspend fun getAllSongs(): List<Song> {
        return try {
            songCollection.get().await().toObjects(Song::class.java)
        } catch (ex: Exception) {
            emptyList()
        }
    }
}