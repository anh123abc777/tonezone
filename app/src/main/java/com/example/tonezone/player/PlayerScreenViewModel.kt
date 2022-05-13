package com.example.tonezone.player

import android.app.Application
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.palette.graphics.Palette
import com.example.tonezone.database.TokenRepository
import com.example.tonezone.database.TonezoneDB
import com.example.tonezone.network.FirebaseRepository
import com.example.tonezone.network.Track
import com.example.tonezone.utils.Type
import com.example.tonezone.utils.createBitmapFromUrl
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.ts.DefaultTsPayloadReaderFactory
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.ShuffleOrder
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.*
import java.util.*


class PlayerScreenViewModel(val application: Application,val user: FirebaseUser?) : ViewModel() {

    private val tokenRepository = TokenRepository(TonezoneDB.getInstance(application).tokenDao)
    val token = runBlocking(Dispatchers.IO) { tokenRepository.token}
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope( Dispatchers.Main)
    private val uiSeekBarScope = CoroutineScope(viewModelJob + Dispatchers.Main)
    private val firebaseRepo = FirebaseRepository()

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

    private var _selectedTracks = MutableLiveData<List<Track>>()
    val selectedTracks : LiveData<List<Track>>
        get() = _selectedTracks

    private var originPlaylist = mutableListOf<Track>()

    private var _isShuffling = MutableLiveData<Boolean>()
    val isShuffling : LiveData<Boolean>
        get() = _isShuffling

    private var _repeatMode = MutableLiveData<Int>()
    val repeatMode: LiveData<Int>
        get() = _repeatMode

    private var _isFavorite = MutableLiveData<Boolean>()
    val isFavorite : LiveData<Boolean>
        get() = _isFavorite

    private var _isShowingCurrentPlaylist = MutableLiveData<Boolean?>()
    val isShowingCurrentPlaylist: LiveData<Boolean?>
        get() = _isShowingCurrentPlaylist

    private val extractorsFactory = DefaultExtractorsFactory()
        .setTsExtractorFlags(DefaultTsPayloadReaderFactory.FLAG_ALLOW_NON_IDR_KEYFRAMES)
        .setTsExtractorFlags(DefaultTsPayloadReaderFactory.FLAG_DETECT_ACCESS_UNITS)

    private var exoPlayer = ExoPlayer.Builder(application.applicationContext!!)
        .setMediaSourceFactory(
            DefaultMediaSourceFactory(
                application.applicationContext,
                extractorsFactory)).build()

    private var shuffleIndexes = mutableListOf<Int>()

    private val _isLongPress = MutableLiveData<Int>()
    val isLongPress : LiveData<Int>
        get() = _isLongPress

    val darkBackgroundDrawable = MutableLiveData<Drawable>()
    val colorOnPrimary = MutableLiveData<Int>()
    val darkColorOnPrimary = MutableLiveData<Int>()
    val lightColorOnPrimary = MutableLiveData<Int>()
    val darkTint = MutableLiveData<ColorStateList>()
    private val lightTint = MutableLiveData<ColorStateList>()
    val backgroundController = MutableLiveData<Drawable>()
    val lightBackgroundDrawable = MutableLiveData<Drawable>()
    val darkBackGround = MutableLiveData<Int>()

    init {
        _currentTrack.value = Track()
        _playerState.value = PlayerState.NONE
        _progress.value = 0L
        _isShuffling.value = false
        _repeatMode.value = 0
        _selectedTracks.value = listOf()
        _isLongPress.value = View.VISIBLE

    }


    fun onInit(pos: Int, listTrack : List<Track>?=listOf()) {
        exoPlayer.clearMediaItems()
        runBlocking {
            launch {
                listTrack!!.forEachIndexed { index,  song ->
                    val mediaItem: MediaItem =
                        MediaItem.Builder().setUri(song.preview_url).setMediaId(song.id).build()

                    exoPlayer.addMediaItem(index, mediaItem)
                }
            }
            _currentPlaylist.value = listTrack!!
            Log.i("PlayerScreen",listTrack.map{it.id}.toString())
            originPlaylist.clear()
            originPlaylist += listTrack
            _currentTrack.value = listTrack[pos]

        }
    }

    fun onPlay(posMs: Long=0L) {
//        exoPlayer.stop()

        try {

            if (_isShuffling.value != true) {
                exoPlayer.seekTo(posSongSelectedInGroup(), posMs)
                exoPlayer.prepare()
                exoPlayer.play()
                _playerState.value = PlayerState.PLAY
            } else {
                exoPlayer.seekTo(shuffleIndexes[posSongSelectedInGroup()], posMs)
                exoPlayer.prepare()
                exoPlayer.play()
                _playerState.value = PlayerState.PLAY
            }
        }catch (e: Exception){
            try {
                exoPlayer.seekTo(2, posMs)
                exoPlayer.prepare()
                exoPlayer.play()
                _playerState.value = PlayerState.PLAY
            }catch (e: Exception){
                exoPlayer.stop()
                _playerState.value = PlayerState.PAUSE

            }

        }
    }

    fun onPlay(track: Track){
        val pos = posSongSelectedInGroup(track)
        if (pos!=-1) {
            jobs.cancel()
            exoPlayer.seekTo(pos, 0L)
            _currentTrack.value = track
            _playerState.value = PlayerState.PLAY
        }
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
        if(posSongSelectedInGroup()<_currentPlaylist.value!!.size-1){
            _currentTrack.value = _currentPlaylist.value?.get(posSongSelectedInGroup()+1)
            _playerState.value = PlayerState.PLAY

        }
        /** do something new*/
        else{
            _currentTrack.value = currentPlaylist.value?.get(0)
            _playerState.value = PlayerState.PLAY
        }
    }

    fun onPrevious(){
        jobs.cancel()
        if(posSongSelectedInGroup()>0){
//            exoPlayer.seekTo(exoPlayer.currentMediaItemIndex-1, 0L)
            _currentTrack.value = _currentPlaylist.value?.get(posSongSelectedInGroup()-1)
            _playerState.value = PlayerState.PLAY
        }
    }

    fun posSongSelectedInGroup() = _currentPlaylist.value!!.indexOfFirst {
        it.id == _currentTrack.value?.id
    }

    private fun posSongSelectedInGroup(track: Track) = _currentPlaylist.value!!.indexOfFirst {
        it.id == track.id
    }

     var jobs = uiSeekBarScope.coroutineContext.job

    fun initSeekBar(){
        val start = System.nanoTime()
        var previousTrack = _currentTrack.value

        jobs = uiSeekBarScope.launch {

            var currentPosition = 0L
            while (this.isActive) {
                if (_playerState.value == PlayerState.PLAY) {
                    if (previousTrack==_currentTrack.value) {
                        currentPosition = exoPlayer.currentPosition
                        _progress.value = currentPosition
                        Log.i("initSeekBar", "$this ${this.isActive}")
                        delay(200L)
                        if (_currentTrack.value?.id != exoPlayer.currentMediaItem?.mediaId && _currentTrack.value!=Track()) {
                            _currentTrack.value =
                                _currentPlaylist.value?.find { it.id == exoPlayer.currentMediaItem?.mediaId }
                        }
                    }else {
                        this.cancel()
                    }
                }else{
                    this.cancel()
                }
            }
        }
        jobs.invokeOnCompletion {
            val time = (System.nanoTime() - start).toDouble()/1e6
            user?.let { it1 -> firebaseRepo.saveHistory(it1.uid,previousTrack!!.id,time/previousTrack.duration_ms!!,Type.TRACK) }
            if (_currentTrack.value == previousTrack)
                _playerState.value = PlayerState.PAUSE
        }
    }

    fun seekTo(progress : Int){
        _progress.value = progress.toLong()*1000
        exoPlayer.seekTo(progress.toLong()*1000)
    }

    fun changeStateShuffle(){
        if (_isShuffling.value == true){
            cancelShuffle()
        }else
            shuffle()
    }

    private fun shuffle(){
        shuffleIndexes.clear()
        val shuffleModel = ShuffleOrder.DefaultShuffleOrder(_currentPlaylist.value!!.size)
        shuffleIndexes.add(shuffleModel.firstIndex)
        var index = shuffleModel.firstIndex
        while (shuffleModel.getNextIndex(index)!=-1){
            shuffleIndexes.add(shuffleModel.getNextIndex(index))
            index = shuffleModel.getNextIndex(index)
        }

        exoPlayer.setShuffleOrder(shuffleModel)
        exoPlayer.shuffleModeEnabled = true
        _isShuffling.value = true
//        var afterCurrentTrack = originPlaylist.subList(posSongSelectedInGroup()+2,originPlaylist.size-1)
//        val beforeCurrentTrack = originPlaylist.subList(0,posSongSelectedInGroup()+1)
//        _currentPlaylist.value = beforeCurrentTrack+afterCurrentTrack.shuffled()
//        val shuffledPlaylist = originPlaylist.filter { it!=currentTrack.value }

        val shuffledPlaylist = mutableListOf<Track>()
        shuffleIndexes.forEach {
            shuffledPlaylist.add(originPlaylist[it])
        }
        _currentPlaylist.value = shuffledPlaylist

//        onInit()
//        if (_playerState.value ==PlayerState.PLAY)
//            onPlay(_progress.value!!)
    }

    fun swapItem(startPos: Int,endPos: Int){

        Collections.swap(_currentPlaylist.value,startPos,endPos)

        if(shuffleIndexes.isNotEmpty()) {
            Collections.swap(shuffleIndexes, startPos, endPos)
            exoPlayer.setShuffleOrder(ShuffleOrder.DefaultShuffleOrder(shuffleIndexes.toIntArray(),0))
        }else{

            Collections.swap(originPlaylist,startPos,endPos)
            val mediaItem = exoPlayer.getMediaItemAt(startPos)
            exoPlayer.removeMediaItem(startPos)
            exoPlayer.addMediaItem(endPos,mediaItem)
        }
    }

    private fun cancelShuffle(){
//        swapItem(0,2)
        _isShuffling.value = false
        _currentPlaylist.value = originPlaylist
        exoPlayer.shuffleModeEnabled = false

    }

    fun changeModeRepeat(){
        when(_repeatMode.value){
            Player.REPEAT_MODE_ALL -> setModeRepeatOne()
            Player.REPEAT_MODE_ONE -> cancelRepeat()
            Player.REPEAT_MODE_OFF -> setModeRepeatAll()
        }
    }

    private fun setModeRepeatAll(){
        exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
        _repeatMode.value = Player.REPEAT_MODE_ALL
    }

    private fun setModeRepeatOne(){
        exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
        _repeatMode.value = Player.REPEAT_MODE_ONE
    }

    private fun cancelRepeat(){
        exoPlayer.repeatMode = Player.REPEAT_MODE_OFF
        _repeatMode.value = Player.REPEAT_MODE_OFF
    }

    fun setupPropertiesView(){
        _isShowingCurrentPlaylist.value = null
    }

    fun showCurrentPlaylist(){
        _isShowingCurrentPlaylist.value = true
    }

    fun showCurrentPlaylistComplete(){
        _isShowingCurrentPlaylist.value = false
    }

    fun initPrimaryColor(){
        val bitmap = runBlocking(Dispatchers.IO) {
            createBitmapFromUrl(
                application,
                currentTrack.value?.album?.images?.get(0)?.url?:"https://picsum.photos/200/200"
            )
        }

        Palette.from(bitmap).generate { palette ->

            palette?.darkMutedSwatch?.let {
                darkColorOnPrimary.value = it.titleTextColor
            }

            palette?.mutedSwatch?.let {
                val lightDrawable = GradientDrawable()
                lightDrawable.setColor(it.rgb)
                lightDrawable.cornerRadius = 32f
                lightBackgroundDrawable.value = lightDrawable
                lightTint.value = ColorStateList.valueOf(it.titleTextColor)
                lightColorOnPrimary.value = it.rgb
            }

            palette?.darkMutedSwatch?.let {
                val drawable = GradientDrawable()
                drawable.setColor(it.rgb)
                darkBackgroundDrawable.value = drawable
                darkBackGround.value = it.rgb

                colorOnPrimary.value = it.titleTextColor
                darkTint.value = ColorStateList.valueOf(it.titleTextColor)

                val gd = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(
                        Color.TRANSPARENT,
                        it.rgb,
                        it.rgb,
                        it.rgb,
                        it.rgb,
                        it.rgb,
                        it.rgb,
                        it.rgb,
                        it.rgb,
                        it.rgb,
                        it.rgb,
                        it.rgb,
                    )
                )
                gd.cornerRadius = 0f

                backgroundController.value = gd

            }
        }
    }

    fun selectTrack(track: Track){
        val newList = mutableListOf<Track>()
        newList += _selectedTracks.value?: listOf()
        if (!newList.contains(track))
            newList.add(track)
        _selectedTracks.value = newList
    }

    fun unselectTrack(track: Track){
        val newList = mutableListOf<Track>()
        newList += _selectedTracks.value?: listOf()
        newList.remove(track)
        _selectedTracks.value = newList
    }

    fun selectALlTracks(){
        _selectedTracks.value = _currentPlaylist.value
    }

    fun clearSelectedTracks(){
        _selectedTracks.value = listOf()
    }

    fun receiveLongPressEvent(){
        _isLongPress.value = View.GONE
    }

    fun handleLongPressEventComplete(){
        _isLongPress.value = View.VISIBLE
    }

    fun hideTracksFromPlaylist(){
        val mutableListCurrentPlaylist = mutableListOf<Track>()
        mutableListCurrentPlaylist += _currentPlaylist.value!!
        mutableListCurrentPlaylist -= _selectedTracks.value?: listOf()

        _currentPlaylist.value = mutableListCurrentPlaylist

        originPlaylist.forEachIndexed { index, track ->
            if (_selectedTracks.value!!.contains(track)) {
                exoPlayer.removeMediaItem(index)

                if (shuffleIndexes.isNotEmpty()){
                    shuffleIndexes.remove(index)
                }
            }
        }

        originPlaylist -= _selectedTracks.value?: listOf()

        shuffleIndexes.clear()
        (_currentPlaylist.value as MutableList<Track>).forEachIndexed { _, track ->
            shuffleIndexes.add(originPlaylist.indexOf(track))
        }

        _selectedTracks.value = listOf()

    }

    fun checkIsLikeTrack(){
        user?.let {
            firebaseRepo.db.collection("Followed")
                .document(it.uid)
                .get()
                .addOnSuccessListener {
                    if (it["tracks"] != null) {
                        val followedObjects = it["tracks"] as List<String>
                        _isFavorite.value = followedObjects.contains(_currentTrack.value!!.id)
                    } else {
                        _isFavorite.value = false
                    }
                }
        }
    }

    fun likeTrack(){
        if (_isFavorite.value != true)
            user?.let { firebaseRepo.followObject(it.uid, _currentTrack.value!!.id,Type.TRACK) }
        else
            user?.let { firebaseRepo.unfollowObject(it.uid, _currentTrack.value!!.id) }

        _isFavorite.value = !_isFavorite.value!!
    }

    fun addToQueue(track: Track){
        originPlaylist.add(track)
        _currentPlaylist.value = _currentPlaylist.value?.plus(track)
        if (shuffleIndexes.isNotEmpty()){
            shuffleIndexes.add(shuffleIndexes.size)
        }
        exoPlayer.addMediaItem(MediaItem.Builder().setUri(track.preview_url).setMediaId(track.id).build())
    }

    fun closePlayer(){
        _playerState.value = PlayerState.PAUSE
        exoPlayer.pause()
        exoPlayer.clearMediaItems()
        _currentPlaylist.value = listOf()
        _currentTrack.value = Track()
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    enum class PlayerState{PLAY, PAUSE, NONE}
}