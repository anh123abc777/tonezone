package com.example.tonezone.detailplaylist

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tonezone.network.PlaylistInfo
import com.example.tonezone.network.ToneApi
import com.example.tonezone.network.Track
import kotlinx.coroutines.*

class PlaylistDetailsViewModel
    (val token: String,  val playlistInfo: PlaylistInfo) : ViewModel() {
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(viewModelJob+ Dispatchers.Main)

    private val _playlistItems = MutableLiveData<List<Track>>()
    val playlistItems : LiveData<List<Track>>
        get() = _playlistItems

    private var _onClick = MutableLiveData<Boolean>()
    val onClick : LiveData<Boolean>
        get() = _onClick

    init {
        _onClick.value = false
        getDataPlaylistItems()
    }

    private fun getDataPlaylistItems() {
        uiScope.launch(Dispatchers.Main) {
            _playlistItems.value =
                if (playlistInfo.type == "artist")
                    getArtistTopTracks()
                else
                    getPlaylistTracks()
        }
    }

    private suspend fun getPlaylistTracks(): List<Track> {
        return try {
            val playlistItemsDeferred = ToneApi.retrofitService
                .getPlaylistItemsAsync("Bearer $token", playlistInfo.id)
            val dataPlaylistItems = playlistItemsDeferred.items
            dataPlaylistItems.map {
                it.track
            }
        } catch (e: Exception) {
            Log.i("error", e.message!! )
            listOf()
        }
    }

    private suspend fun getArtistTopTracks(): List<Track> {
        return try {
            val artistTopTracksDeferred = ToneApi.retrofitService
                .getArtistTopTracksAsync(
                    "Bearer $token",
                    playlistInfo.id,
                    "VN")
            artistTopTracksDeferred.tracks!!
        } catch (e: Exception) {
            Log.i("error", e.message!! )
            listOf()
        }
    }


    override fun onCleared() {
        super.onCleared()
        uiScope.cancel()
    }
}