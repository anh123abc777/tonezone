package com.example.tonezone.home

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tonezone.database.TokenRepository
import com.example.tonezone.database.TonezoneDB
import com.example.tonezone.network.GroupPlaylist
import com.example.tonezone.network.PlaylistsObject
import com.example.tonezone.network.ToneApi
import kotlinx.coroutines.*

class HomeViewModel(application: Application) : ViewModel() {

    private val tokenRepository = TokenRepository(TonezoneDB.getInstance(application).tokenDao)
    val token = runBlocking(Dispatchers.IO) { tokenRepository.token}
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(viewModelJob+ Dispatchers.Main)

    private var _groupPlaylists = MutableLiveData<List<GroupPlaylist>>()
    val groupPlaylists : LiveData<List<GroupPlaylist>>
        get() = _groupPlaylists

    fun getGroupPlaylistsData() = uiScope.launch {

        _groupPlaylists.value =try {
            val featuredPlaylistsDeferred: Deferred<PlaylistsObject> = ToneApi.retrofitService
                .getFeaturedPlaylistsAsync("Bearer ${token.value!!.value}")

            val chartsDeferred : Deferred<PlaylistsObject> = ToneApi.retrofitService
                .getChartsAsync("Bearer ${token.value!!.value}")

            listOf(
                GroupPlaylist("feature playlist",featuredPlaylistsDeferred.await().playlists.items),
                GroupPlaylist("charts",chartsDeferred.await().playlists.items))
        }
        catch (e: Exception){
            listOf()
        }
    }

}