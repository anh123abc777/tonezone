package com.example.tonezone.player

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.Album
import com.spotify.protocol.types.Artist
import com.spotify.protocol.types.ImageUri
import com.spotify.protocol.types.Track


class PlayerScreenViewModel(val application: Application) : ViewModel() {

    private val CLIENT_ID = "0546209c8b9b4b66a8d49037c566caa6"
    private val REDIRECT_URI = "com.tonezone://callback"
    private var mSpotifyAppRemote: SpotifyAppRemote? = null
    private var connectionParams = ConnectionParams.Builder(CLIENT_ID)
        .setRedirectUri(REDIRECT_URI)
        .showAuthView(true)
        .build()!!
    var imgUri = "https://picsum.photos/200/300"
    private var _currentTrack = MutableLiveData<Track>()
    val currentTrack : LiveData<Track>
    get() = _currentTrack

    init {
        onStart()
        _currentTrack.value = Track(Artist("",""),
            listOf(Artist("","")),
            Album("",""),
            0L,
            "",
            "",
            ImageUri(""),
            false,
            false
        )
    }

    private fun onStart(){
        SpotifyAppRemote.connect(application, connectionParams,
            object : Connector.ConnectionListener {
                override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                    mSpotifyAppRemote = spotifyAppRemote
                    Log.d("MainActivity", "Connected! Yay!")
                }
                override fun onFailure(throwable: Throwable) {
                    Log.e("MyActivity", throwable.message, throwable)
                }
            })
    }

    fun onPlay() {
        mSpotifyAppRemote!!.playerApi.play("spotify:playlist:37i9dQZF1DX2sUQwD7tbmL")
        mSpotifyAppRemote!!.playerApi
            .subscribeToPlayerState()
            .setEventCallback { playerState ->
                _currentTrack.value = playerState.track
            }
    }

    fun onPause() {
        mSpotifyAppRemote!!.playerApi.pause()
    }

    fun onNext() {
        mSpotifyAppRemote!!.playerApi.skipNext()
    }

    fun onPrevious(){
        mSpotifyAppRemote!!.playerApi.skipPrevious()
    }

    fun seekTo(posMs: Long){
        mSpotifyAppRemote!!.playerApi.seekTo(posMs)
    }

    fun onResume(){
        mSpotifyAppRemote!!.playerApi.resume()
    }

    fun disconnect(){
        SpotifyAppRemote.disconnect(mSpotifyAppRemote)
    }

    override fun onCleared() {
        super.onCleared()
//        SpotifyAppRemote.disconnect(mSpotifyAppRemote)
    }
}