package com.example.tonezone.yourlibrary

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tonezone.adapter.LibraryAdapter
import com.example.tonezone.network.*
import kotlinx.coroutines.*


class YourLibraryViewModel(val token: String) : ViewModel() {

    private val _userPlaylists = MutableLiveData<Playlists>()
    val userPlaylists : LiveData<Playlists>
        get() = _userPlaylists

     private val _followedArtists = MutableLiveData<Artists>()
    val followedArtists : LiveData<Artists>
        get() = _followedArtists

    private val _userSavedTracks = MutableLiveData<SavedTracks>()
    val userSavedTracks : LiveData<SavedTracks>
        get() = _userSavedTracks

    private val _sortOption = MutableLiveData<SortOption>()
    val sortOption : LiveData<SortOption>
        get() = _sortOption

    private val _type = MutableLiveData<TypeItemLibrary>()
    val type : LiveData<TypeItemLibrary>
        get() = _type

    private val _navigateToDetailPlaylist= MutableLiveData<PlaylistInfo>()
    val navigateToDetailPlaylist : LiveData<PlaylistInfo>
        get() = _navigateToDetailPlaylist

    init {
        getDataUserSavedTracks()
        getDataFollowedArtists()
        getDataUserPlaylists()
        _sortOption.value = SortOption.Alphabetical
        _type.value = TypeItemLibrary.All

    }

    private fun getDataUserPlaylists(){
        viewModelScope.launch {
            try {
                _userPlaylists.value = ToneApi.retrofitService
                    .getCurrentUserPlaylistsAsync("Bearer $token")

            } catch (e: Exception) {
                Log.i("error", e.message!!)
            }
        }
    }

    private fun getDataFollowedArtists(){
        viewModelScope.launch {
            try {
                _followedArtists.value = ToneApi.retrofitService
                    .getFollowedArtistsAsync("Bearer $token", "artist").artists!!

            } catch (e: Exception) {
                Log.i("errorFollowedArtists", e.message!!)
                _followedArtists.value = Artists()
            }
        }
    }

    private fun getDataUserSavedTracks(){
        viewModelScope.launch {
            _userSavedTracks.value = try {
                ToneApi.retrofitService.getUserSavedTracks("Bearer $token")
            }catch (e: Exception){
                Log.i("errorUserSavedTracks",e.message.toString())
                SavedTracks()
            }
        }
    }

    fun changeSortOption(){
        _sortOption.value =
            if(_sortOption.value==SortOption.Alphabetical)
                SortOption.Creator
            else SortOption.Alphabetical
    }

    fun filterType(type: TypeItemLibrary){
        _type.value = type
    }

    fun displayPlaylistDetails(dataItem: LibraryAdapter.DataItem){
        _navigateToDetailPlaylist.value = PlaylistInfo(
            dataItem.id.toString(),
            dataItem.name.toString(),
            dataItem.description.toString(),
            dataItem.image,
            dataItem.uri.toString(),
            dataItem.typeName.toString()
        )
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun displayPlaylistDetailsComplete() {
        _navigateToDetailPlaylist.value = null
    }
}