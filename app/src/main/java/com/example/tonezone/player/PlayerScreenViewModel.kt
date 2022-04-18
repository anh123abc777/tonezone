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
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.ts.DefaultTsPayloadReaderFactory
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import kotlinx.coroutines.*
import java.lang.IllegalArgumentException


class PlayerScreenViewModel(val application: Application) : ViewModel() {

    private val CLIENT_ID = "0546209c8b9b4b66a8d49037c566caa6"
//    private val REDIRECT_URI = "com.tonezone://callback"
//        .setRedirectUri(REDIRECT_URI)
//        .showAuthView(true)
//        .build()!!

    private val tokenRepository = TokenRepository(TonezoneDB.getInstance(application).tokenDao)
    val token = runBlocking(Dispatchers.IO) { tokenRepository.token}
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope( Dispatchers.Main)
    private val uiSeekBarScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private var _currentTrack = MutableLiveData<Track>()
    val currentTrack : LiveData<Track>
        get() = _currentTrack

    private var _playerState = MutableLiveData<PlayerState>()
    val playerState : LiveData<PlayerState>
        get() = _playerState

    private var _progress = MutableLiveData<Long>()
    val progress : LiveData<Long>
        get() = _progress

    private var _currentPlaylist = MutableLiveData<List<Track>>()
    val currentPlaylist : LiveData<List<Track>>
        get() = _currentPlaylist

    private var _isShuffling = MutableLiveData<Boolean>()
    val isShuffling : LiveData<Boolean>
        get() = _isShuffling

    private val extractorsFactory = DefaultExtractorsFactory()
        .setTsExtractorFlags(DefaultTsPayloadReaderFactory.FLAG_ALLOW_NON_IDR_KEYFRAMES)
        .setTsExtractorFlags(DefaultTsPayloadReaderFactory.FLAG_DETECT_ACCESS_UNITS)

    private var exoPlayer = SimpleExoPlayer.Builder(application.applicationContext!!)
        .setMediaSourceFactory(
            DefaultMediaSourceFactory(
                application.applicationContext,
                extractorsFactory)).build()

    init {
        _currentTrack.value = Track()
        _playerState.value = PlayerState.NONE
        _progress.value = 0L
        _isShuffling.value = false
    }


    fun onInit(pos: Int, listTrack : List<Track>?=listOf()) {

        exoPlayer.clearMediaItems()
        runBlocking {
            launch {
                listTrack!!.forEach { song ->
                    exoPlayer.addMediaItem(
                        MediaItem.fromUri(
                            song.preview_url!!
                        )
                    )
                }
            }
            _currentPlaylist.value = listTrack!!
            _currentTrack.value = listTrack[pos]
        }
    }

    fun onPlay(){
        exoPlayer.stop()
        exoPlayer.seekTo(posSongSelectedInGroup(),0L)
        exoPlayer.prepare()
        exoPlayer.play()
        _playerState.value = PlayerState.PLAY
    }

    fun onChangeState(){
        if(exoPlayer.isPlaying){
            onPause()
        } else{
            onResume()
        }
    }

    private fun onPause() {
        exoPlayer.pause()
        _playerState.value = PlayerState.PAUSE
        jobs.cancel()
    }

    private fun onResume(){
        exoPlayer.play()
        _playerState.value = PlayerState.PLAY
        initSeekBar()
    }

    fun onNext() {
        jobs.cancel()
        if(exoPlayer.hasNextWindow()){
            exoPlayer.seekTo(exoPlayer.currentWindowIndex + 1, 0L)
            _currentTrack.value = _currentPlaylist.value!![posSongSelectedInGroup()+1]
            _playerState.value = PlayerState.PLAY
        }
    }

    fun onPrevious(){
        jobs.cancel()
        if (exoPlayer.hasPreviousWindow()) {
            exoPlayer.seekTo(exoPlayer.currentWindowIndex - 1, 0L)
            _currentTrack.value = _currentPlaylist.value!![posSongSelectedInGroup() - 1]
            _playerState.value = PlayerState.PLAY
        }
    }

    fun posSongSelectedInGroup() = _currentPlaylist.value!!.indexOfFirst {
        it.id == currentTrack.value!!.id
    }

     var jobs = uiSeekBarScope.coroutineContext.job

    fun initSeekBar(){
         jobs = uiSeekBarScope.launch {

             var currentPosition = 0L
             while (this.isActive) {
                 if (_playerState.value == PlayerState.PLAY) {
                     if (currentPosition != 30000L) {
                                 currentPosition = exoPlayer.currentPosition
                                 _progress.value = currentPosition
                         Log.i("initSeekBar", "$this ${this.isActive}")
                         delay(500L)
                     }
                 } else {
                     this.cancel()
                     Log.i("initSeekBar", "$this ${this.isActive}")
                 }
             }
         }
        }

    fun seekTo(progress : Long){
        _progress.value = progress
        exoPlayer.seekTo(progress.toLong()*1000)
    }

    fun onShuffle(){
//        mSpotifyAppRemote!!.playerApi.setShuffle(!_isShuffling.value!!)
    }

    fun onRepeat(){
//        mSpotifyAppRemote!!.playerApi.setRepeat(1)
    }



    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    enum class PlayerState{PLAY, PAUSE, NONE}
}