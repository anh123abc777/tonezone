package com.example.tonezone.artistdetails

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tonezone.adapter.LibraryAdapter
import com.example.tonezone.network.*
import com.example.tonezone.utils.Type
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.*

class ArtistDetailsViewModel(
    val playlistInfo: PlaylistInfo,
    val user: FirebaseUser): ViewModel() {

    private val firebaseRepo = FirebaseRepository()

    private val _artistTopTracks = firebaseRepo.getTracksOfArtist(playlistInfo.id,playlistInfo.name,6)
    val tracks : LiveData<List<Track>>
        get() = _artistTopTracks

    private val _artist = firebaseRepo.getArtist(playlistInfo.id)
    val artist : LiveData<Artist>
        get() = _artist

    private var _artistAlbums = firebaseRepo.getAlbumsOfArtist(playlistInfo.id,playlistInfo.name,6)
    val artistAlbums : LiveData<List<Album>>
        get() = _artistAlbums

    private val _isFollowingArtist = firebaseRepo.checkObjectIsFollowed(user.uid,playlistInfo.id,Type.ARTIST)
    val isFollowingArtist : LiveData<Boolean>
        get() = _isFollowingArtist

    private val _isNavigateToMoreTracks = MutableLiveData<PlaylistInfo?>()
    val isNavigateToMoreTracks : LiveData<PlaylistInfo?>
        get() = _isNavigateToMoreTracks

    private val _isNavigateToMoreAlbums = MutableLiveData<PlaylistInfo?>()
    val isNavigateToMoreAlbums : LiveData<PlaylistInfo?>
        get() = _isNavigateToMoreAlbums

    private val _navigateToDetailPlaylist= MutableLiveData<PlaylistInfo>()
    val navigateToDetailPlaylist : LiveData<PlaylistInfo>
        get() = _navigateToDetailPlaylist

    private val _relateArtists = firebaseRepo.getRelateArtists(playlistInfo.id)
    val relateArtists : LiveData<List<Artist>>
        get() = _relateArtists

    private val job = Job()
    private val uiScope = CoroutineScope(job + Dispatchers.Main)

    fun changeStateFollowArtist(){
        if(_isFollowingArtist.value != true){
            followArtist()
        }else
            unfollowArtist()
        _isFollowingArtist.value = !_isFollowingArtist.value!!
    }

    private fun unfollowArtist(){
        firebaseRepo.unfollowObject(user.uid,playlistInfo.id)
//        firebaseRepo.submitArtistScore(user.uid,playlistInfo.id,0)
    }

    private fun followArtist(){
        firebaseRepo.followObject(user.uid,playlistInfo.id,Type.ARTIST)
//        firebaseRepo.submitArtistScore(user.uid,playlistInfo.id,1)
    }

    fun navigateToMoreTracks(){
        _isNavigateToMoreTracks.value = playlistInfo
    }

    fun navigateToMoreTracksComplete(){
        _isNavigateToMoreTracks.value = null
    }

    fun navigateToMoreAlbums(){
        _isNavigateToMoreAlbums.value = playlistInfo
    }

    fun navigateToMoreAlbumsComplete(){
        _isNavigateToMoreAlbums.value = null
    }

    fun displayPlaylistDetails(dataItem: LibraryAdapter.DataItem){
        _navigateToDetailPlaylist.value = PlaylistInfo(
            dataItem.id.toString(),
            dataItem.name.toString(),
            dataItem.description.toString(),
            dataItem.image,
            dataItem.typeName.toString()
        )
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun displayPlaylistDetailsComplete() {
        _navigateToDetailPlaylist.value = null
    }

    override fun onCleared() {
        super.onCleared()
        uiScope.cancel()
    }
}