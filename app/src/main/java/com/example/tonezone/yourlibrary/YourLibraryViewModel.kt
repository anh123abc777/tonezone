package com.example.tonezone.yourlibrary

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tonezone.R
import com.example.tonezone.database.TokenRepository
import com.example.tonezone.database.TonezoneDB
import com.example.tonezone.network.*
import kotlinx.coroutines.*


class YourLibraryViewModel(application: Application) : ViewModel() {

    private val tokenRepository = TokenRepository(TonezoneDB.getInstance(application).tokenDao)
    val token = tokenRepository.token
//    private val job = Job()
//    private val uiScope = CoroutineScope(job + Dispatchers.Main)

    private var _userPlaylists = MutableLiveData<List<Playlist>>()
    val userPlaylists : LiveData<List<Playlist>>
        get() = _userPlaylists

    private var _followedArtists = MutableLiveData<List<Artist>>()
    val followedArtists : LiveData<List<Artist>>
        get() = _followedArtists

    private var _sortOption = MutableLiveData<SortOption>()
    val sortOption : LiveData<SortOption>
        get() = _sortOption

    private var _type = MutableLiveData<TypeItemLibrary>()
    val type : LiveData<TypeItemLibrary>
        get() = _type

    init {
        _sortOption.value = SortOption.Alphabetical
        _type.value = TypeItemLibrary.All
    }

    fun getDataUserPlaylists() = runBlocking{
        try {
            val userPlaylistsDeferred: Deferred<UserPlaylists>
            = ToneApi.retrofitService
                .getCurrentUserPlaylistsAsync("Bearer ${token.value!!.value}")
            _userPlaylists.value = userPlaylistsDeferred.await().items
        } catch (e: Exception) {
            Log.i("error",e.message!!)
        }
    }

    fun getDataFollowedArtists() = runBlocking{
        try {

            val followerArtistsDeferred: Deferred<DataFollowedArtists>
            = ToneApi.retrofitService
                .getFollowedArtistsAsync("Bearer ${token.value!!.value}","artist")
            _followedArtists.value = followerArtistsDeferred.await().artists.items!!
            Log.i("artists",_followedArtists.value.toString())

        }catch (e: java.lang.Exception){
            Log.i("errorGetFollowedArtists",e.message!!)
        }
    }

    fun changeSortOption(){
        _sortOption.value =
            if(_sortOption.value==SortOption.Alphabetical)
                SortOption.Creator
            else SortOption.Alphabetical
    }

    fun filterType(type: TypeItemLibrary){
        _type.value = type
    }
}