package com.example.tonezone.search.result

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tonezone.network.*
import kotlinx.coroutines.*

class ResultViewModel(val token: String,val genreName: String) : ViewModel() {

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _categoryPlaylists = MutableLiveData<List<Playlist>>()
    val categoryPlaylists : LiveData<List<Playlist>>
        get() = _categoryPlaylists

    init {
        getSearchResultData()
    }

    private fun getSearchResultData() {
        uiScope.launch {
            _categoryPlaylists.value = try {
               ToneApi.retrofitService
                    .getCategoryPlaylistsAsync("Bearer $token", genreName).playlists.items

            } catch (e: Exception) {
                Log.i("result", e.message!!)
                listOf()
            }
        }
    }

}