package com.example.tonezone.player

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tonezone.database.TokenRepository
import com.example.tonezone.database.TonezoneDB
import com.example.tonezone.network.ToneApi
import com.example.tonezone.network.Track
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import kotlinx.coroutines.*


class PlayerScreenViewModel(val application: Application) : ViewModel() {

    private val CLIENT_ID = "0546209c8b9b4b66a8d49037c566caa6"
    private val REDIRECT_URI = "com.tonezone://callback"
    private var mSpotifyAppRemote: SpotifyAppRemote? = null
    private var connectionParams = ConnectionParams.Builder(CLIENT_ID)
        .setRedirectUri(REDIRECT_URI)
        .showAuthView(true)
        .build()!!

    private val tokenRepository = TokenRepository(TonezoneDB.getInstance(application).tokenDao)
    val token = runBlocking(Dispatchers.IO) { tokenRepository.token}
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(viewModelJob+ Dispatchers.Main)

    private var _currentTrack = MutableLiveData<Track>()
    val currentTrack : LiveData<Track>
        get() = _currentTrack

    private var _playerState = MutableLiveData<PlayerState>()
    val playerState : LiveData<PlayerState>
        get() = _playerState

    private var _progress = MutableLiveData<Long>()
    val progress : LiveData<Long>
        get() = _progress

    private var _uriTrackResponse = MutableLiveData<String>()
    val uriTrackResponse : LiveData<String>
        get() = _uriTrackResponse

    private var _isShuffling = MutableLiveData<Boolean>()
    val isShuffling : LiveData<Boolean>
        get() = _isShuffling
    init {
        onStart()
        _currentTrack.value = Track()
        _playerState.value = PlayerState.NONE
        _progress.value = 0L
        _isShuffling.value = false
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

    fun onPlay(uriPlaylist: String?, pos: Int) {
        mSpotifyAppRemote!!.playerApi.skipToIndex(uriPlaylist,pos)
        mSpotifyAppRemote!!.playerApi
            .subscribeToPlayerState()
            .setEventCallback { playerState ->
                if(playerState.track.uri!=null)
                _uriTrackResponse.value = playerState.track.uri
            }
        _playerState.value = PlayerState.PLAY

    }

    fun initSeekBar()=
        uiScope.launch {
            var currentPosition = 0L
            while (this.isActive){
                if(_playerState.value == PlayerState.PLAY) {
                    if (currentPosition != currentTrack.value!!.duration_ms) {
                        mSpotifyAppRemote!!.playerApi
                            .subscribeToPlayerState()
                            .setEventCallback { state ->
                                currentPosition = state.playbackPosition
                                _progress.value = currentPosition
                                _isShuffling.value = state.playbackOptions.isShuffling
                            }

                        delay(500L)
                    }
                } else{
                    this.cancel()
                }
            }
        }

    fun getImageTrack() =  uiScope.launch {
        _currentTrack.value = try {
            val id = _uriTrackResponse.value
                ?.substring(_uriTrackResponse.value!!.lastIndexOf(":")+1, _uriTrackResponse.value!!.length)
            val trackDeferred = ToneApi.retrofitService
                .getTrackAsync("Bearer ${token.value!!.value}", id!!)
            trackDeferred.await()
        }catch (e: Exception){
            Track()
        }
    }


    fun onShuffle(){
        mSpotifyAppRemote!!.playerApi.setShuffle(!_isShuffling.value!!)
    }

    fun onRepeat(){
        mSpotifyAppRemote!!.playerApi.setRepeat(1)
    }

    fun onChangeState(){
        when(_playerState.value){
            PlayerState.PAUSE -> onResume()
            PlayerState.PLAY -> onPause()
        }
    }

    private fun onPause() {
        mSpotifyAppRemote!!.playerApi.pause()
        _playerState.value = PlayerState.PAUSE
    }

    private fun onResume(){
        mSpotifyAppRemote!!.playerApi.resume()
        _playerState.value = PlayerState.PLAY
        initSeekBar().start()
    }

    fun onNext() {
        _playerState.value = PlayerState.PAUSE
        mSpotifyAppRemote!!.playerApi.skipNext()
        _playerState.value = PlayerState.PLAY
    }

    fun onPrevious(){
        _playerState.value = PlayerState.PAUSE
        mSpotifyAppRemote!!.playerApi.skipPrevious()
        _playerState.value = PlayerState.PLAY
    }

    fun seekTo(posMs: Long){
        mSpotifyAppRemote!!.playerApi.seekTo(posMs)
    }

    fun disconnect(){
        SpotifyAppRemote.disconnect(mSpotifyAppRemote)
        _playerState.value = PlayerState.NONE
    }

    override fun onCleared() {
        super.onCleared()
        uiScope.cancel()
//        SpotifyAppRemote.disconnect(mSpotifyAppRemote)
    }

    enum class PlayerState{PLAY, PAUSE, NONE}
}