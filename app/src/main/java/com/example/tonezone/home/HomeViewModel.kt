package com.example.tonezone.home

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tonezone.network.*
import kotlinx.coroutines.*

class HomeViewModel(val token: String, val user: User) : ViewModel() {

    private var _groupPlaylists = MutableLiveData<List<GroupPlaylist>>()
    val groupPlaylists : LiveData<List<GroupPlaylist>>
        get() = _groupPlaylists

    private val _navigateToPlaylistDetails = MutableLiveData<PlaylistInfo>()
    val navigateToPlaylistDetails : LiveData<PlaylistInfo>
        get() = _navigateToPlaylistDetails

    private val firebaseRepo = FirebaseRepository()

    init {
        getGroupPlaylistsData()

        Log.i("sublist","${firebaseRepo.checkObjectIsFollowed(user.id,"u8Y8ybfKA6Lxtw4ChfCr","playlist")}")
    }

    private fun getGroupPlaylistsData() {

        val featuredPlaylists = firebaseRepo.getFeaturePlaylists()
        featuredPlaylists.observeForever(){
            if (it!=null){
                val availableList = _groupPlaylists.value?: listOf()
                _groupPlaylists.value =
                    mutableListOf(GroupPlaylist("feature playlist", it)) + availableList
            }
        }

        val charts = firebaseRepo.getAlbumReleases()
        charts.observeForever(){
            if (it!=null){
                val availableList = _groupPlaylists.value?: listOf()
                _groupPlaylists.value =
                    mutableListOf(GroupPlaylist("new releases",convertAlbumsToPlaylists(it))) + availableList
            }
        }
//        uiScope.launch {
//
//            _groupPlaylists.value = try {
//                val featuredPlaylists = firebaseRepo.getFeaturePlaylists()
//
////                val featuredPlaylists = ToneApi.retrofitService
////                    .getFeaturedPlaylistsAsync("Bearer $token").playlists.items
//
//                val charts = ToneApi.retrofitService
//                    .getChartsAsync("Bearer $token").playlists.items
//
//                val albumReleases = firebaseRepo.getAlbumReleases().value!!
////                val albumReleases = ToneApi.retrofitService
////                    .getNewAlbumReleases("Bearer $token").albums.items
//
//                mutableListOf(
//                    GroupPlaylist("feature playlist", featuredPlaylists),
//                    GroupPlaylist("charts", charts),
//                    GroupPlaylist("new releases",convertAlbumsToPlaylists((albumReleases)))
//                )
//
//            } catch (e: Exception) {
//                listOf()
//            }
//        }
    }

    private fun convertAlbumsToPlaylists(albums: List<Album>): List<Playlist> =
        albums.map {
            Playlist(
                id = it.id!!,
                description = it.album_group!!,
                images = it.images!!,
                name = it.name!!,
                owner = Owner(),
                public = false,
                type = it.type!!,
            )
        }


     fun displayPlaylistDetails(playlistInfo: PlaylistInfo){
        _navigateToPlaylistDetails.value = playlistInfo
    }

    @SuppressLint("NullSafeMutableLiveData")
     fun displayPlaylistDetailsComplete(){
        _navigateToPlaylistDetails.value = null
    }

    override fun onCleared() {
        super.onCleared()
    }
}