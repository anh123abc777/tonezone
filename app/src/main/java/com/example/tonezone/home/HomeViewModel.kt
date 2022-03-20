package com.example.tonezone.home

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tonezone.network.GroupPlaylist
import com.example.tonezone.network.PlaylistInfo
import com.example.tonezone.network.ToneApi
import kotlinx.coroutines.*

class HomeViewModel(val token: String) : ViewModel() {

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(viewModelJob+ Dispatchers.Main)

    private var _groupPlaylists = MutableLiveData<List<GroupPlaylist>>()
    val groupPlaylists : LiveData<List<GroupPlaylist>>
        get() = _groupPlaylists

    private val _navigateToPlaylistDetails = MutableLiveData<PlaylistInfo>()
    val navigateToPlaylistDetails : LiveData<PlaylistInfo>
        get() = _navigateToPlaylistDetails

    init {
        getGroupPlaylistsData()
    }

    private fun getGroupPlaylistsData() {
        uiScope.launch {

            _groupPlaylists.value = try {
                val featuredPlaylistsDeferred= ToneApi.retrofitService
                    .getFeaturedPlaylistsAsync("Bearer $token")

                val chartsDeferred = ToneApi.retrofitService
                    .getChartsAsync("Bearer $token")

                listOf(
                    GroupPlaylist(
                        "feature playlist",
                        featuredPlaylistsDeferred.playlists.items
                    ),
                    GroupPlaylist("charts", chartsDeferred.playlists.items)
                )
            } catch (e: Exception) {
                listOf()
            }
        }
    }

     fun displayPlaylistDetails(playlistInfo: PlaylistInfo){
        _navigateToPlaylistDetails.value = playlistInfo
    }

    @SuppressLint("NullSafeMutableLiveData")
     fun displayPlaylistDetailsComplete(){
        _navigateToPlaylistDetails.value = null
    }

}