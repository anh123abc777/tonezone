package com.example.tonezone.playlistdetails

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tonezone.R
import com.example.tonezone.network.*
import com.example.tonezone.utils.Signal
import kotlinx.coroutines.*

class PlaylistDetailsViewModel
    (val token: String, var playlistInfo: PlaylistInfo, private val user: User) : ViewModel() {
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(viewModelJob+ Dispatchers.Main)
    private val firebaseRepo = FirebaseRepository()

    private var _playlistItems = getDataPlaylistItems()
    val playlistItems : LiveData<List<Track>>
        get() = _playlistItems

    private var _isPlaylistFollowed = firebaseRepo.checkObjectIsFollowed(user.id,playlistInfo.id,"playlist")
    val isUserPlaylistFollowed : LiveData<Boolean>
        get() = _isPlaylistFollowed

    private val _selectedObjectID = MutableLiveData<Pair<String,Int>>()
    val selectedObjectID : LiveData<Pair<String,Int>>
        get() = _selectedObjectID

    private var _receivedSignal = MutableLiveData<Signal>()
    val receivedSignal : LiveData<Signal>
        get() = _receivedSignal

    private val _navigateYourPlaylists = MutableLiveData<String>()
    val navigateYourPlaylists : LiveData<String>
        get() = _navigateYourPlaylists

    private val _isOwnedByUser = MutableLiveData<Boolean>()
    val isOwnedByUser : LiveData<Boolean>
        get() = _isOwnedByUser

    private var _currentPlaylist = firebaseRepo.getPlaylist(playlistInfo.id)
    val currentPlaylist : LiveData<Playlist>
        get() = _currentPlaylist

    private var _stateLikedOfTracks = MutableLiveData<List<Boolean>>()
    val stateLikedOfTracks : LiveData<List<Boolean>>
        get() = _stateLikedOfTracks


    private fun getDataPlaylistItems(): MutableLiveData<List<Track>> {

        return when (playlistInfo.type) {
                "artist" -> {
                    getArtistTopTracks()
                }

                "album" -> {
                    getAlbumTracks()
                }

                else -> {
                    if(playlistInfo.id=="userSavedTrack")
                        getUserSavedTracks()
                    else
                        getPlaylistTracks()
                }
            }
    }

    private fun getAlbumTracks(): MutableLiveData<List<Track>>{
        return firebaseRepo.getTracksOfAlbum(playlistInfo.id)
    }

    private fun getPlaylistTracks(): MutableLiveData<List<Track>>{
        return firebaseRepo.getTracksOfPlaylist(playlistInfo.id)
    }

    private fun getUserSavedTracks(): MutableLiveData<List<Track>>{
        return firebaseRepo.getLikedTracks(user.id)

    }

    private fun getArtistTopTracks(): MutableLiveData<List<Track>> {
        return firebaseRepo.getTracksOfArtist(playlistInfo.id,playlistInfo.name)
    }

    fun getStateItemsLiked(){
        val trackIDs = _playlistItems.value!!.map { it.id }
        _stateLikedOfTracks = firebaseRepo
            .checkObjectIsFollowed(user.id,trackIDs,"track")
    }

    fun showBottomSheet(objectID: String, buttonID: Int ){
        _selectedObjectID.value = Pair(objectID,buttonID)
    }

    fun showBottomSheet(){
        _selectedObjectID.value = Pair(playlistInfo.id,R.id.more_option)
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun showBottomSheetComplete(){
        _selectedObjectID.value = null
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun handleSignalComplete(){
        _receivedSignal.value = null
    }

    fun handleSignal(){
        when(_receivedSignal.value){
            null -> Log.i("receivedSignal","don't have happen")

            Signal.LIKE_PLAYLIST -> likePlaylist()

            Signal.LIKED_PLAYLIST -> likePlaylist()

            Signal.LIKE_TRACK -> likeTrack()

            Signal.LIKED_TRACK -> likeTrack()

            Signal.ADD_TO_QUEUE -> addToQueue()

            Signal.VIEW_ARTIST -> showArtistsOfTrack()

            Signal.VIEW_ALBUM -> showAlbumOfTrack()

            Signal.ADD_TO_PLAYLIST -> addToPlaylist()

            Signal.DELETE_PLAYLIST -> deletePlaylist()

            Signal.ADD_SONGS -> TODO()

            Signal.EDIT_PLAYLIST -> TODO()

            Signal.SHARE -> TODO()

            else -> Log.i("receivedSignal","what is this???????")
        }
    }

    private fun deletePlaylist(){
        firebaseRepo.unfollowObject(user.id,_selectedObjectID.value!!.first,"playlist")
    }

    private fun addToPlaylist(){
        _navigateYourPlaylists.value =
            _playlistItems.value?.find { it.id == selectedObjectID.value!!.first}!!.uri
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun addToPlaylistComplete(){
        _navigateYourPlaylists.value = null
    }

    private fun getCurrentTrack(): Track? =
        playlistItems.value!!.find { it.id == selectedObjectID.value!!.first }


    private val _isShowingTrackDetails = MutableLiveData<Signal>()
    val isShowingTrackDetails : LiveData<Signal>
        get() = _isShowingTrackDetails

    private fun showArtistsOfTrack(){
        _isShowingTrackDetails.value = Signal.VIEW_ARTIST
    }

    private fun showAlbumOfTrack(){
        _isShowingTrackDetails.value = Signal.VIEW_ARTIST
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun showTracksDetailsComplete(){
        _isShowingTrackDetails.value = null
    }

    private fun likeTrack(){
        val indexOfTrack = _playlistItems.value!!
            .indexOfFirst { it.id == _selectedObjectID.value!!.first }
        Log.i("likeTrack","${_stateLikedOfTracks.value!![indexOfTrack]}")

        if (_stateLikedOfTracks.value!![indexOfTrack]) {
            firebaseRepo
                .unfollowObject(user.id,_selectedObjectID.value!!.first,"track")
        } else {
            firebaseRepo
                .followObject(user.id,_selectedObjectID.value!!.first,"track")
        }
        var newState = mutableListOf<Boolean>()
        newState += _stateLikedOfTracks.value!!
        newState[indexOfTrack] = !newState[indexOfTrack]
        _stateLikedOfTracks.value = newState
    }

    fun checkedTrackIsLiked(): Boolean{
        val indexOfTrack = _playlistItems.value!!
            .indexOfFirst { it.id == _selectedObjectID.value!!.first }
        return _stateLikedOfTracks.value?.get(indexOfTrack) ?: false
    }

    private fun likePlaylist(){
        uiScope.launch {
            try {
                if (_isPlaylistFollowed.value == false) {

                    firebaseRepo.followObject(user.id,playlistInfo.id,"playlist")
                    changeStateFollowPlaylist()
                }
                else {

                    firebaseRepo.unfollowObject(user.id,playlistInfo.id,"playlist")
                    changeStateFollowPlaylist()
                }
            }catch (e: Exception){
                Log.i("errorLikePlaylist",e.message.toString())
            }
        }
    }

    private fun changeStateFollowPlaylist(){
        _isPlaylistFollowed.value = !_isPlaylistFollowed.value!!
    }

    private fun addToQueue(){
        TODO()
    }

    fun receiveSignal(signal: Signal){
        _receivedSignal.value = signal
    }

    fun checkIsOwnedByUser(){
        _isOwnedByUser.value = currentPlaylist.value!!.owner?.id == user.id
    }

    override fun onCleared() {
        super.onCleared()
        uiScope.cancel()
    }
}