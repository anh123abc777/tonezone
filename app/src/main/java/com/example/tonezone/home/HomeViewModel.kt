package com.example.tonezone.home

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tonezone.network.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job


class HomeViewModel(val user: User) : ViewModel() {

    private val firebaseRepo = FirebaseRepository()
    private val job = Job()
    private val uiScope = CoroutineScope(job + Dispatchers.Default)

    private var _groupPlaylists = firebaseRepo.getDataHomeScreen(user,uiScope)
    val groupPlaylists : LiveData<List<GroupPlaylist>>
        get() = _groupPlaylists

    private val _navigateToPlaylistDetails = MutableLiveData<PlaylistInfo>()
    val navigateToPlaylistDetails : LiveData<PlaylistInfo>
        get() = _navigateToPlaylistDetails

    init {
        Log.i("HomeCheck","${_groupPlaylists.value}")
    }

     fun displayPlaylistDetails(playlistInfo: PlaylistInfo){
        _navigateToPlaylistDetails.value = playlistInfo
    }

    @SuppressLint("NullSafeMutableLiveData")
     fun displayPlaylistDetailsComplete(){
        _navigateToPlaylistDetails.value = null
    }

    override fun onCleared() {
        job.cancel()
        super.onCleared()
    }
}