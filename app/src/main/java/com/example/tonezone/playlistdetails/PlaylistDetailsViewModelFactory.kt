package com.example.tonezone.playlistdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tonezone.network.PlaylistInfo
import com.example.tonezone.network.User
import com.google.firebase.auth.FirebaseUser

@Suppress("UNCHECKED_CAST")
class PlaylistDetailsViewModelFactory(
     val playlistInfo: PlaylistInfo,
     private val firebaseUser: FirebaseUser
     ): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(PlaylistDetailsViewModel::class.java))
            return PlaylistDetailsViewModel(playlistInfo,firebaseUser) as T
        throw IllegalArgumentException("Unknown VM class")
    }
}