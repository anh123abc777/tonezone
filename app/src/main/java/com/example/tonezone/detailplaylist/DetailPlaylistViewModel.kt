package com.example.tonezone.detailplaylist

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tonezone.database.TokenRepository
import com.example.tonezone.database.TonezoneDB
import com.example.tonezone.network.PlaylistInfo
import com.example.tonezone.network.ToneApi
import com.example.tonezone.network.Track
import kotlinx.coroutines.*

class DetailPlaylistViewModel
    (application: Application,  val playlistInfo: PlaylistInfo) : ViewModel() {
    private val tokenRepository = TokenRepository(TonezoneDB.getInstance(application).tokenDao)
    val token = runBlocking(Dispatchers.IO) { tokenRepository.token}
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(viewModelJob+ Dispatchers.Main)


    private val _playlistItems = MutableLiveData<List<Track>>()
    val playlistItems : LiveData<List<Track>>
        get() = _playlistItems

    fun getDataPlaylistItems()=
        uiScope.launch(Dispatchers.Main) {
            _playlistItems.value =
                if (playlistInfo.type=="artist")
                    getArtistTopTracks()
                else
                    getPlaylistTracks()
        }

    private suspend fun getPlaylistTracks(): List<Track> {
        return try {
            val playlistItemsDeferred = ToneApi.retrofitService
                .getPlaylistItemsAsync("Bearer ${token.value!!.value}", playlistInfo.id)
            val dataPlaylistItems = playlistItemsDeferred.await().items
            dataPlaylistItems.map {
                it.track
            }
        } catch (e: Exception) {
            Log.i("error", e.message!! )
            listOf()
        }
    }

    private suspend fun getArtistTopTracks(): List<Track> {
        return try {
            val artistTopTracksDeferred = ToneApi.retrofitService
                .getArtistTopTracksAsync(
                    "Bearer ${token.value!!.value}",
                    playlistInfo.id,
                    "VN")
            artistTopTracksDeferred.await().tracks!!
        } catch (e: Exception) {
            Log.i("error", e.message!! )
            listOf()
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}