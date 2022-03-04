package com.example.tonezone.yourlibrary

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tonezone.database.TokenRepository
import com.example.tonezone.database.TonezoneDB
import com.example.tonezone.network.ToneApi
import com.example.tonezone.network.UserPlaylists
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking

class YourLibraryViewModel(application: Application) : ViewModel() {

    private val tokenRepository = TokenRepository(TonezoneDB.getInstance(application).tokenDao)
    val token = tokenRepository.token

    private var _userPlaylists = MutableLiveData<UserPlaylists>()
    val userPlaylists : LiveData<UserPlaylists>
        get() = _userPlaylists

    fun getDataUserPlaylists() = runBlocking{
        try {
            val userPlaylistsDeferred: Deferred<UserPlaylists> = ToneApi.retrofitService
                .getCurrentUserPlaylistsAsync("Bearer ${token.value!!.value}")
            _userPlaylists.value = userPlaylistsDeferred.await()
        } catch (e: Exception) {
            Log.i("error",e.message!!)
        }
    }
}