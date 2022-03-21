package com.example.tonezone.detailplaylist

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tonezone.network.PlaylistInfo
import com.example.tonezone.network.ToneApi
import com.example.tonezone.network.Track
import com.example.tonezone.utils.Signal
import kotlinx.coroutines.*

class PlaylistDetailsViewModel
    (val token: String,  var playlistInfo: PlaylistInfo) : ViewModel() {
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(viewModelJob+ Dispatchers.Main)

    private val _playlistItems = MutableLiveData<List<Track>>()
    val playlistItems : LiveData<List<Track>>
        get() = _playlistItems

    private var _signal = MutableLiveData<Signal>()
    val signal : LiveData<Signal>
        get() = _signal

    init {
        getDataPlaylistItems()
    }

    private fun getDataPlaylistItems() {
        uiScope.launch(Dispatchers.Main) {
            _playlistItems.value =
                when (playlistInfo.type) {
                    "artist" -> getArtistTopTracks()
                    else -> {
                        if(playlistInfo.id=="userSavedTrack")
                            getUserSavedTracks()
                        else
                            getPlaylistTracks()
                    }
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

    fun handleSignal(){
        when(_signal.value){
            null -> Log.i("signal","don't have happen")

            Signal.LIKE_TRACK -> likeTrack()

            Signal.LIKE_PLAYLIST -> likePlaylist()

            Signal.ADD_TO_QUEUE -> addToQueue()
        }
    }

    private fun likeTrack(){
        TODO()
    }

    private fun likePlaylist(){
        TODO()
    }

    private fun addToQueue(){
        TODO()
    }

    fun receiveSignal(signal: Signal){
        _signal.value = signal
    }

    override fun onCleared() {
        super.onCleared()
        uiScope.cancel()
    }
}