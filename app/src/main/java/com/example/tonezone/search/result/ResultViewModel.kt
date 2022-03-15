package com.example.tonezone.search.result

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tonezone.database.TokenRepository
import com.example.tonezone.database.TonezoneDB
import com.example.tonezone.network.*
import kotlinx.coroutines.*

class ResultViewModel(val application: Application,val genreName: String) : ViewModel() {

    private val tokenRepository = TokenRepository(TonezoneDB.getInstance(application).tokenDao)
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    val token = tokenRepository.token

    private var _categoryPlaylists = MutableLiveData<List<Playlist>>()
    val categoryPlaylists : LiveData<List<Playlist>>
        get() = _categoryPlaylists

    fun getSearchResultData() =
        uiScope.launch{
            _categoryPlaylists.value = try {
                val categoryPlaylistsDeferred: Deferred<PlaylistsObject> = ToneApi.retrofitService
                    .getCategoryPlaylistsAsync("Bearer ${token.value!!.value}",genreName)
                categoryPlaylistsDeferred.await().playlists.items
            } catch (e: Exception) {
                Log.i("result",e.message!!)
                listOf()
            }
        }

}