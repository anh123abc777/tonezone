package com.example.tonezone.detailplaylist

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tonezone.R
import com.example.tonezone.network.PlaylistInfo
import com.example.tonezone.network.ToneApi
import com.example.tonezone.network.Track
import com.example.tonezone.network.User
import com.example.tonezone.utils.Signal
import kotlinx.coroutines.*

class PlaylistDetailsViewModel
    (val token: String, var playlistInfo: PlaylistInfo, private val userProfile: User) : ViewModel() {
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(viewModelJob+ Dispatchers.Main)

    private val _playlistItems = MutableLiveData<List<Track>>()
    val playlistItems : LiveData<List<Track>>
        get() = _playlistItems

    private val _isUserFollowPlaylist = MutableLiveData<Boolean>()
    val isUserFollowPlaylist : LiveData<Boolean>
        get() = _isUserFollowPlaylist

    private val _selectedObjectID = MutableLiveData<Pair<String,Int>>()
    val selectedObjectID : LiveData<Pair<String,Int>>
        get() = _selectedObjectID

    private var _receivedSignal = MutableLiveData<Signal>()
    val receivedSignal : LiveData<Signal>
        get() = _receivedSignal

    private val _navigateYourPlaylists = MutableLiveData<String>()
    val navigateYourPlaylists : LiveData<String>
        get() = _navigateYourPlaylists

    init {
        getDataPlaylistItems()
        checkIfUserFollowPlaylist()
    }

    private fun getDataPlaylistItems() {
        uiScope.launch(Dispatchers.Main) {
            _playlistItems.value =
                when (playlistInfo.type) {
                    "artist" -> {

                        getArtistTopTracks()
                    }
                    else -> {
                        if(playlistInfo.id=="userSavedTrack")
                            getUserSavedTracks()
                        else
                            getPlaylistTracks()
                    }
                }
        }
    }

    fun getImageArtist(){
        uiScope.launch {
            playlistInfo.image =
                try {
                    ToneApi.retrofitService.getArtist("Bearer $token",playlistInfo.id).images?.get(0)?.url
                } catch (e: Exception){
                    Log.i("getImageArtist","Failure ${e.message}")
                    ""
                }
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

    private suspend fun getUserSavedTracks(): List<Track>{
        return try {
                ToneApi.retrofitService
                    .getUserSavedTracks("Bearer $token").items?.map { it.track }?: listOf()
            }catch (e: Exception){
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

    fun checkIfUserFollowPlaylist(): Boolean  =
        runBlocking {
            _isUserFollowPlaylist.value = try {
                ToneApi.retrofitService.checkUserFollowPlaylist(
                    "Bearer $token",
                    playlistInfo.id,
                    userProfile.id!!
                )[0]
            } catch (e: Exception) {
                false
            }
            _isUserFollowPlaylist.value!!
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

            else -> Log.i("receivedSignal","what is this???????")
        }
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


    private val _isShowingTracksDetails = MutableLiveData<Signal>()
    val isShowingTrackDetails : LiveData<Signal>
        get() = _isShowingTracksDetails

    private fun showArtistsOfTrack(){
        _isShowingTracksDetails.value = Signal.VIEW_ARTIST
    }

    private fun showAlbumOfTrack(){
        _isShowingTracksDetails.value = Signal.VIEW_ARTIST
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun showTracksDetailsComplete(){
        _isShowingTracksDetails.value = null
    }

    private fun likeTrack(){
        val isSaved = runBlocking {
            _selectedObjectID.value?.let { checkUserSavedTrack(it.first) }
        }?: false

        uiScope.launch {
            try {
                if (isSaved) {
                    ToneApi.retrofitService
                        .removeTracksForCurrentUser(
                            "Bearer $token"
                            ,_selectedObjectID.value!!.first)
                } else {
                    ToneApi.retrofitService
                        .saveTracksForCurrentUser(
                            "Bearer $token"
                            ,_selectedObjectID.value!!.first)
                }
            } catch (e: Exception){
                Log.i("likeTrack","Failure id ${_selectedObjectID.value?.first.toString()} bool $isSaved ${e.message.toString()}")
            }
        }
    }

    fun checkUserSavedTrack(id: String): Boolean =
        runBlocking {
            try {
                ToneApi.retrofitService.checkUserSavedTrack("Bearer $token", id)[0]
            } catch (e: Exception){
                false
            }
        }

    private fun likePlaylist(){
        uiScope.launch {
            try {
                if (_isUserFollowPlaylist.value == false) {

                    ToneApi.retrofitService.followPlaylist("Bearer $token", playlistInfo.id)
                    changeStateFollowPlaylist()
                }
                else {

                    ToneApi.retrofitService.unfollowPlaylist("Bearer $token", playlistInfo.id)
                    changeStateFollowPlaylist()
                }
            }catch (e: Exception){
                Log.i("errorLikePlaylist",e.message.toString())
            }
        }
    }

    private fun changeStateFollowPlaylist(){
        _isUserFollowPlaylist.value = !_isUserFollowPlaylist.value!!
    }

    private fun addToQueue(){
        TODO()
    }

    fun receiveSignal(signal: Signal){
        _receivedSignal.value = signal
    }

    override fun onCleared() {
        super.onCleared()
        uiScope.cancel()
    }
}