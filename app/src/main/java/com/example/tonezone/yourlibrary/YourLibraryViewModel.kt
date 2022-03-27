package com.example.tonezone.yourlibrary

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tonezone.adapter.LibraryAdapter
import com.example.tonezone.network.*
import com.example.tonezone.utils.ObjectRequest
import com.example.tonezone.utils.Signal
import com.google.gson.Gson
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class YourLibraryViewModel(val token: String, val user: User) : ViewModel() {

    private val job = Job()
    private val uiScope = CoroutineScope(job + Dispatchers.Main)

    private val _userPlaylists = MutableLiveData<Playlists>()
    val userPlaylists : LiveData<Playlists>
        get() = _userPlaylists

     private val _followedArtists = MutableLiveData<Artists>()
    val followedArtists : LiveData<Artists>
        get() = _followedArtists

    private val _userSavedTracks = MutableLiveData<SavedTracks>()
    val userSavedTracks : LiveData<SavedTracks>
        get() = _userSavedTracks

    private val _sortOption = MutableLiveData<SortOption>()
    val sortOption : LiveData<SortOption>
        get() = _sortOption

    private val _type = MutableLiveData<TypeItemLibrary>()
    val type : LiveData<TypeItemLibrary>
        get() = _type

    private val _navigateToDetailPlaylist= MutableLiveData<PlaylistInfo>()
    val navigateToDetailPlaylist : LiveData<PlaylistInfo>
        get() = _navigateToDetailPlaylist

    private val _isShowBottomSheet = MutableLiveData<ObjectRequest>()
    val isShowBottomSheet : LiveData<ObjectRequest>
        get() = _isShowBottomSheet

    init {
        getDataUserSavedTracks()
        getDataFollowedArtists()
        getDataUserPlaylists()
        _sortOption.value = SortOption.Alphabetical
        _type.value = TypeItemLibrary.All

    }

    private fun getDataUserPlaylists(){
        viewModelScope.launch {
            try {
                _userPlaylists.value = ToneApi.retrofitService
                    .getCurrentUserPlaylistsAsync("Bearer $token")

            } catch (e: Exception) {
                Log.i("error", e.message!!)
            }
        }
    }

    private fun getDataFollowedArtists(){
        viewModelScope.launch {
            try {
                _followedArtists.value = ToneApi.retrofitService
                    .getFollowedArtistsAsync("Bearer $token", "artist").artists!!

            } catch (e: Exception) {
                Log.i("errorFollowedArtists", e.message!!)
                _followedArtists.value = Artists()
            }
        }
    }

    private fun getDataUserSavedTracks(){
        viewModelScope.launch {
            _userSavedTracks.value = try {
                ToneApi.retrofitService.getUserSavedTracks("Bearer $token")
            }catch (e: Exception){
                Log.i("errorUserSavedTracks",e.message.toString())
                SavedTracks()
            }
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

    fun displayPlaylistDetails(dataItem: LibraryAdapter.DataItem){
        _navigateToDetailPlaylist.value = PlaylistInfo(
            dataItem.id.toString(),
            dataItem.name.toString(),
            dataItem.description.toString(),
            dataItem.image,
            dataItem.uri.toString(),
            dataItem.typeName.toString()
        )
    }

    fun displayPlaylistDetails(playlist: Playlist){
        _navigateToDetailPlaylist.value = PlaylistInfo(
            playlist.id,
            playlist.name,
            playlist.description,
            if (playlist.images?.isNotEmpty() == true)
                playlist.images[0].url
            else "",
            playlist.uri,
            playlist.type
        )
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun displayPlaylistDetailsComplete() {
        _navigateToDetailPlaylist.value = null
    }

    private val _isCreatingPlaylist = MutableLiveData<Boolean>()
    val isCreatingPlaylist : LiveData<Boolean>
        get() = _isCreatingPlaylist

    fun requestToCreatePlaylist(){
        _isCreatingPlaylist.value = true
    }

    fun requestToCreatePlaylistComplete(){
        _isCreatingPlaylist.value = false
    }

    fun createPlaylist(playlistName: String){
        uiScope.launch {
            try {

                ToneApi.retrofitService2
                    .createPlaylist("Bearer $token",
                        user.id,
                        "{\"name\":\"$playlistName\"," +
                                "\"description\":\"New playlist description\"," +
                                "\"public\":false}").enqueue(object : Callback<String> {
                        override fun onResponse(call: Call<String>, response: Response<String>) {
                            val gson = Gson()
                            val playlist = gson.fromJson(response.body(),Playlist::class.java)
                            getDataUserPlaylists()
                            displayPlaylistDetails(playlist)
                            Log.i("createPlaylist",playlist.toString())
                        }
                        override fun onFailure(call: Call<String>, t: Throwable) {
                            Log.i("createPlaylist","Failure + ${t.message}")
                        }
                    })
            } catch (e: Exception){

                Log.i("createPlaylist","Failure + $e")
            }
        }
    }

    fun showBottomSheet(objectRequest: ObjectRequest){
        _isShowBottomSheet.value = objectRequest
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun showBottomSheetComplete(){
        _isShowBottomSheet.value = null
    }
}