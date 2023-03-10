package com.example.tonezone.playlistdetails

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tonezone.R
import com.example.tonezone.network.*
import com.example.tonezone.utils.Signal
import com.example.tonezone.utils.Type
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.*

class PlaylistDetailsViewModel
    ( var playlistInfo: PlaylistInfo, val firebaseUser: FirebaseUser) : ViewModel() {
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(viewModelJob+ Dispatchers.Main)
    private val firebaseRepo = FirebaseRepository()

    private var _playlistItems = getDataPlaylistItems()
    val playlistItems : LiveData<List<Track>>
        get() = _playlistItems

    private var _isPlaylistFollowed = firebaseRepo.checkObjectIsFollowed(firebaseUser.uid,playlistInfo.id,Type.PLAYLIST)
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

    private var _isSaveHistory = MutableLiveData<Playlist>()
    val isSavedHistory : LiveData<Playlist>
        get() = _isSaveHistory

    private var _queueTrack = MutableLiveData<Track?>()
    val queueTrack : LiveData<Track?>
        get() = _queueTrack

    private var _isRequestingToAddTracks = MutableLiveData<String?>()
    val isRequestingToAddTracks: LiveData<String?>
        get() = _isRequestingToAddTracks

    private var _isRequestingToAddToOtherPlaylist = MutableLiveData<Boolean>()
    val isRequestingToAddToOtherPlaylist: LiveData<Boolean>
        get() = _isRequestingToAddToOtherPlaylist

    private var _removedTrack = MutableLiveData<Pair<Int,Track>?>()
    val removeTrack : LiveData<Pair<Int,Track>?>
        get() = _removedTrack

    fun setPlaylists(list: List<Track>){
        _playlistItems.value = list
    }

    private fun getDataPlaylistItems(): MutableLiveData<List<Track>> {

        Log.i("PlaylistInfo","$playlistInfo")
        return when (playlistInfo.type) {
                "artist" -> {
                    getArtistTopTracks()
                }

                "album" -> {
                    getAlbumTracks()
                }

                "playlist" -> {
                    if(playlistInfo.id=="liked_track")
                        getUserSavedTracks()
                    else
                        getPlaylistTracks()
                }

                else -> {

                        MutableLiveData()
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
        Log.i("trackABCs","Something")
        return firebaseRepo.getLikedTracks(firebaseUser.uid)

    }

    private fun getArtistTopTracks(): MutableLiveData<List<Track>> {
        return firebaseRepo.getTracksOfArtist(playlistInfo.id,playlistInfo.name)
    }

    fun initStateLikedItems(){
        val trackIDs = _playlistItems.value!!.map { it.id }
        _stateLikedOfTracks = firebaseRepo
            .checkObjectIsFollowed(firebaseUser.uid,trackIDs,Type.TRACK)
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

            Signal.ADD_SONGS -> requestToAddSongs()

            Signal.REMOVE_FROM_THIS_PLAYLIST -> removeFromThisPlaylistTemp()

            Signal.EDIT_PLAYLIST -> TODO()

            Signal.SHARE -> TODO()

            Signal.ADD_TO_OTHER_PLAYLIST -> addToOtherPlaylist()

            else -> Log.i("receivedSignal","what is this???????")
        }
    }

    private fun addToOtherPlaylist(){
        _isRequestingToAddToOtherPlaylist.value = true
    }

    fun addToOtherPlaylistComplete(){
        _isRequestingToAddToOtherPlaylist.value = false
    }

    private fun removeFromThisPlaylistTemp(){
        _removedTrack.value = Pair(getIndexSelectedTrack(),getSelectedTrack())
        _playlistItems.value = _playlistItems.value?.minus(listOf(getSelectedTrack()))
    }

     fun undoRemove(){
         val previousPlaylist = _playlistItems.value!!.toMutableList()
         previousPlaylist.add(_removedTrack.value!!.first,_removedTrack.value!!.second)
         _playlistItems.value = previousPlaylist
         _removedTrack.value = null
     }

     fun removeFromThisPlaylistForever(){
        firebaseRepo.removeTrackFromPlaylist(playlistInfo.id,_selectedObjectID.value!!.first)
    }

    fun removeTrackFromThisPlaylistComplete(){
        _removedTrack.value = null
    }

    private fun getIndexSelectedTrack(): Int =
        playlistItems.value?.indexOf(getSelectedTrack())!!

    private fun getSelectedTrack(): Track =
        playlistItems.value!!.find { it.id == selectedObjectID.value!!.first }!!



    fun requestToAddSongs(){
        _isRequestingToAddTracks.value = playlistInfo.id
    }

    fun navigateToTheAddSongViewComplete(){
        _isRequestingToAddTracks.value = null
    }


    private fun deletePlaylist(){
        firebaseRepo.unfollowObject(firebaseUser.uid,_selectedObjectID.value!!.first)
    }

    private fun addToPlaylist(){
        _navigateYourPlaylists.value =
            _playlistItems.value?.find { it.id == selectedObjectID.value!!.first}!!.id
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun addToPlaylistComplete(){
        _navigateYourPlaylists.value = null
    }

    private val _isShowingTrackDetails = MutableLiveData<Signal>()
    val isShowingTrackDetails : LiveData<Signal>
        get() = _isShowingTrackDetails

    private fun showArtistsOfTrack(){
        _isShowingTrackDetails.value = Signal.VIEW_ARTIST
    }

    private fun showAlbumOfTrack(){
        _isShowingTrackDetails.value = Signal.VIEW_ALBUM
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun showTracksDetailsComplete(){
        _isShowingTrackDetails.value = null
    }

    fun likeTrack(){
        val indexOfTrack = _playlistItems.value!!
            .indexOfFirst { it.id == _selectedObjectID.value!!.first }
        Log.i("likeTrack","${_stateLikedOfTracks.value!![indexOfTrack]}")

        if (_stateLikedOfTracks.value!![indexOfTrack]) {
            firebaseRepo
                .unfollowObject(firebaseUser.uid,_selectedObjectID.value!!.first)
        } else {
            firebaseRepo
                .followObject(firebaseUser.uid,_selectedObjectID.value!!.first,Type.TRACK)
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

    fun likePlaylist(){
        uiScope.launch {
            try {
                if (_isPlaylistFollowed.value == false) {

                    firebaseRepo.followObject(firebaseUser.uid,playlistInfo.id,Type.PLAYLIST)
                    changeStateFollowPlaylist()
                }
                else {

                    firebaseRepo.unfollowObject(firebaseUser.uid,playlistInfo.id)
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
        _queueTrack.value = _playlistItems.value?.find { it.id== _selectedObjectID.value!!.first}
    }

    fun addToQueueComplete(){
        _queueTrack.value = null
    }

    fun receiveSignal(signal: Signal){
        _receivedSignal.value = signal
    }

    fun checkIsOwnedByUser(){
        if (playlistInfo.id != "liked_track")
            _isOwnedByUser.value = currentPlaylist.value!!.owner!!.id == firebaseUser.uid
    }

    fun saveHistory(){
        if (_isSaveHistory.value != _currentPlaylist.value){
            firebaseRepo.saveHistory(firebaseUser.uid,playlistInfo.id,0.0,Type.PLAYLIST)
        }
        _isSaveHistory.value = _currentPlaylist.value
    }


    override fun onCleared() {
        super.onCleared()
        uiScope.cancel()
    }
}