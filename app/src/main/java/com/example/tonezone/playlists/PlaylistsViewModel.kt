package com.example.tonezone.playlists

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tonezone.network.*
import kotlinx.coroutines.*

class PlaylistsViewModel(val token: String, val playlistInfo: PlaylistInfo) : ViewModel() {

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val firebaseRepo = FirebaseRepository()

    private var _categoryPlaylists : MutableLiveData<List<Playlist>>
    = firebaseRepo.getPlaylistsOfCategory(playlistInfo.id)
    val categoryPlaylists : LiveData<List<Playlist>>
        get() = _categoryPlaylists


    private fun getPlaylistsOfCategory() {
        _categoryPlaylists = firebaseRepo.getPlaylistsOfCategory(playlistInfo.id)
        uiScope.launch {
            try {
//                _categoryPlaylists.value = ToneApi.retrofitService
//                    .getCategoryPlaylistsAsync("Bearer $token", playlistInfo.id).playlists.items!!
//                val playlistIDs = _categoryPlaylists.value!!.map { it.id!! }
//
//                firebaseRepo.insertItemsToCategory(playlistInfo.id,playlistIDs)
//                firebaseRepo.insertPlaylist(_categoryPlaylists.value!!)
//
            } catch (e: Exception) {
                Log.i("result", e.message!!)
            }
        }
    }

}