package com.example.tonezone.detailplaylist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tonezone.network.PlaylistInfo

@Suppress("UNCHECKED_CAST")
class PlaylistDetailsViewModelFactory
    (val token: String,
     val playlistInfo: PlaylistInfo): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(PlaylistDetailsViewModel::class.java))
            return PlaylistDetailsViewModel(token,playlistInfo) as T
        throw IllegalArgumentException("Unknown VM class")
    }
}