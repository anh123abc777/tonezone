package com.example.tonezone.yourlibrary

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tonezone.adapter.LibraryAdapter
import com.example.tonezone.network.*
import com.example.tonezone.utils.ObjectRequest
import com.example.tonezone.utils.Signal
import com.example.tonezone.utils.Type
import kotlinx.coroutines.*


class YourLibraryViewModel(val token: String, val user: User) : ViewModel() {

    private val job = Job()
    private val uiScope = CoroutineScope(job + Dispatchers.Main)
    private val firebaseRepo = FirebaseRepository()

    private var _userPlaylists = MutableLiveData<List<Playlist>>()
    val userPlaylists : LiveData<List<Playlist>>
        get() = _userPlaylists

     private var _followedArtists = firebaseRepo.getFollowedArtists(user.id)
    val followedArtists : LiveData<List<Artist>>
        get() = _followedArtists

    private val _savedTracks = firebaseRepo.getLikedTracks(user.id)
    val savedTracks : LiveData<List<Track>>
        get() = _savedTracks

    private var _savedAlbums = MutableLiveData<List<Album>>()
    val saveAlbums : LiveData<List<Album>>
        get() = _savedAlbums

    private val _sortOption = MutableLiveData<SortOption>()
    val sortOption : LiveData<SortOption>
        get() = _sortOption

    private val _type = MutableLiveData<TypeItemLibrary>()
    val type : LiveData<TypeItemLibrary>
        get() = _type

    private val _navigateToDetailPlaylist= MutableLiveData<PlaylistInfo>()
    val navigateToDetailPlaylist : LiveData<PlaylistInfo>
        get() = _navigateToDetailPlaylist

    private val _objectShowBottomSheet = MutableLiveData<Pair<ObjectRequest,String>>()
    val objectShowBottomSheet : LiveData<Pair<ObjectRequest,String>>
        get() = _objectShowBottomSheet

    private var _receivedSignal = MutableLiveData<Signal>()
    val receivedSignal : LiveData<Signal>
        get() = _receivedSignal

    init {
//        getDataFollowedArtists()
        getDataUserPlaylists()
        getDataSavedAlbums()
        _sortOption.value = SortOption.Alphabetical
        _type.value = TypeItemLibrary.All

    }

     fun getDataUserPlaylists(){
         _userPlaylists = firebaseRepo.getLikedPlaylists(user.id)
    }

    private fun getDataFollowedArtists(){
        _followedArtists = firebaseRepo.getFollowedArtists(user.id)
    }

    private fun getDataSavedAlbums(){
        _savedAlbums = firebaseRepo.getFollowedAlbums(user.id)
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
            dataItem.typeName.toString()
        )
    }

    private fun displayPlaylistDetails(playlist: Playlist){
        _navigateToDetailPlaylist.value = PlaylistInfo(
            playlist.id!!,
            playlist.name!!,
            playlist.description!!,
            if (playlist.images?.isNotEmpty() == true)
                playlist.images!![0].url
            else "",
            playlist.type!!
        )
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun displayPlaylistDetailsComplete() {
        _navigateToDetailPlaylist.value = null
    }

    private val _isCreatingPlaylist = MutableLiveData<Boolean>()
    val isCreatingPlaylist : LiveData<Boolean>
        get() = _isCreatingPlaylist

    fun requestToCreatePlaylist(){
        _isCreatingPlaylist.value = true
    }

    fun requestToCreatePlaylistComplete(){
        _isCreatingPlaylist.value = false
    }

    fun createPlaylist(playlistName: String){

        val playlist = firebaseRepo.createPlaylist(playlistName,user)
        getDataUserPlaylists()
        displayPlaylistDetails(playlist)

    }

    fun showBottomSheet(objectRequest: ObjectRequest,itemId : String){
        _objectShowBottomSheet.value = Pair(objectRequest,itemId)
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun showBottomSheetComplete(){
        _objectShowBottomSheet.value = null
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun handleSignalComplete(){
        _receivedSignal.value = null
    }

    fun handleSignal(){
        when(_receivedSignal.value){
            null -> Log.i("receivedSignal","don't have happen")

            Signal.LIKED_PLAYLIST -> unlikePlaylist()

            Signal.DELETE_PLAYLIST -> deletePlaylist()

            Signal.STOP_FOLLOWING -> unfollowArtist()

            else -> Log.i("receivedSignal","what is this???????")
        }
    }

    private fun unfollowArtist(){
        firebaseRepo
            .unfollowObject(user.id,_objectShowBottomSheet.value!!.second,Type.ARTIST)
        getDataFollowedArtists()
    }

    private fun unlikePlaylist(){
        firebaseRepo.unfollowObject(user.id,_objectShowBottomSheet.value!!.second,Type.PLAYLIST)
    }

    private fun deletePlaylist(){
        firebaseRepo.deletePlaylist(user.id,_objectShowBottomSheet.value!!.second)
        getDataUserPlaylists()
    }

    fun receiveSignal(signal: Signal){
        _receivedSignal.value = signal
    }

    override fun onCleared() {
        super.onCleared()
        uiScope.cancel()
    }
}