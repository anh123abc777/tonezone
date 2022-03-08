package com.example.tonezone.detailplaylist

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tonezone.network.PlaylistInfo

class DetailPlaylistViewModelFactory
    (val application: Application,
     val playlistInfo: PlaylistInfo): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(DetailPlaylistViewModel::class.java))
            return DetailPlaylistViewModel(application,playlistInfo) as T
        throw IllegalArgumentException("Unknown VM clas")
    }
}