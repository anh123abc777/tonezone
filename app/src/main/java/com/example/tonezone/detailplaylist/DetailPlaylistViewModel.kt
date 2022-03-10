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
import com.example.tonezone.network.ToneApiService
import com.example.tonezone.network.Track
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.await

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
            _playlistItems.value=try {
                val playlistItemsDeferred = ToneApi.retrofitService
                    .getPlaylistItemsAsync("Bearer ${token.value!!.value}",playlistInfo.id)
                val dataPlaylistItems = playlistItemsDeferred.await().items
                dataPlaylistItems.map {
                    it.track
                }
            } catch (e: Exception){
                Log.i("error",e.message!!+" los lis ")
                listOf<Track>()
            }
        }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}