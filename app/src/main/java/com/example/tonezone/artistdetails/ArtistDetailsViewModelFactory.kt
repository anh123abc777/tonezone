package com.example.tonezone.artistdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tonezone.network.PlaylistInfo
import com.example.tonezone.network.User
import com.google.firebase.auth.FirebaseUser

@Suppress("UNCHECKED_CAST")
class ArtistDetailsViewModelFactory(
    val playlistInfo: PlaylistInfo,
    val user: FirebaseUser): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ArtistDetailsViewModel::class.java))
            return ArtistDetailsViewModel(playlistInfo,user) as T
        throw IllegalArgumentException("Unknown VM class")
    }
}