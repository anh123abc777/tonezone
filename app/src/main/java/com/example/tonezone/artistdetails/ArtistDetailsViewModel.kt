package com.example.tonezone.artistdetails

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tonezone.adapter.LibraryAdapter
import com.example.tonezone.network.*
import kotlinx.coroutines.*

class ArtistDetailsViewModel(
    val token: String,
    val playlistInfo: PlaylistInfo,
    val user: User): ViewModel() {

    private val firebaseRepo = FirebaseRepository()

    private val _artistTopTracks = firebaseRepo.getTracksOfArtist(playlistInfo.id,playlistInfo.name)
    val artistTopTracks : LiveData<List<Track>>
        get() = _artistTopTracks

    private val _artist = MutableLiveData<Artist>()
    val artist : LiveData<Artist>
        get() = _artist

    private var _artistAlbums = firebaseRepo.getAlbumsOfArtist(playlistInfo.id)
    val artistAlbums : LiveData<List<Album>>
        get() = _artistAlbums

    private val _isFollowingArtist = firebaseRepo.checkObjectIsFollowed(user.id,playlistInfo.id,"artist")
    val isFollowingArtist : LiveData<Boolean>
        get() = _isFollowingArtist

    private val _isNavigateToMoreTracks = MutableLiveData<PlaylistInfo?>()
    val isNavigateToMoreTracks : LiveData<PlaylistInfo?>
        get() = _isNavigateToMoreTracks

    private val _isNavigateToMoreAlbums = MutableLiveData<PlaylistInfo?>()
    val isNavigateToMoreAlbums : LiveData<PlaylistInfo?>
        get() = _isNavigateToMoreAlbums

    private val _navigateToDetailPlaylist= MutableLiveData<PlaylistInfo>()
    val navigateToDetailPlaylist : LiveData<PlaylistInfo>
        get() = _navigateToDetailPlaylist

    private val _relateArtists = firebaseRepo.getRelateArtist(playlistInfo.id)
    val relateArtists : LiveData<List<Artist>>
        get() = _relateArtists

    private val job = Job()
    private val uiScope = CoroutineScope(job + Dispatchers.Main)

    init {
        getArtistProfile()
        getArtistAlbumsData()
//        checkIsFollowingArtist()
    }

    private fun getArtistTopTracks() {
        uiScope.launch {
            _artistTopTracks.value = try {
                val artistTopTracksDeferred = ToneApi.retrofitService
                    .getArtistTopTracksAsync(
                        "Bearer $token",
                        playlistInfo.id,
                        "VN"
                    )
                artistTopTracksDeferred.tracks!!
            } catch (e: Exception) {
                Log.i("error", e.message!!)
                listOf()
            }
        }
    }

    private fun getArtistProfile(){
        uiScope.launch {
               try {
                   _artist.value = ToneApi.retrofitService
                       .getArtist(
                           "Bearer $token",
                           playlistInfo.id)
                   firebaseRepo.insertArtist(_artist.value!!)
                   Log.i("getArtist","Failure ${_artist.value}")
               }catch (e: Exception){
                   Log.i("getArtist","Failure $e")

               }
        }
    }

    private fun getArtistAlbumsData(){
        uiScope.launch {
            try {
                val temp = ToneApi.retrofitService
                    .getArtistAlbums("Bearer $token",
                    playlistInfo.id).items
                if (temp != _artistAlbums.value)
                    firebaseRepo.insertAlbums(_artistAlbums.value!!)
            }catch (e: Exception){
            }
        }
    }

    private fun checkIsFollowingArtist(){
        uiScope.launch {
            _isFollowingArtist.value =
                try {
                    ToneApi.retrofitService
                        .checkUserIsFollowingArtist(
                            "Bearer $token",
                            playlistInfo.id
                        )[0]
                }catch (e: Exception){
                    Log.i("isFollowingArtist","Failure $e")
                    false
                }
        }
    }

    fun changeStateFollowArtist(){
        if(_isFollowingArtist.value != true){
            followArtist()
        }else
            unfollowArtist()
        _isFollowingArtist.value = !_isFollowingArtist.value!!
    }

    private fun unfollowArtist(){
        firebaseRepo.unfollowObject(user.id,playlistInfo.id,"artist")
        uiScope.launch {
            try {
                ToneApi.retrofitService
                    .unfollowArtist(
                        "Bearer $token",
                        playlistInfo.id
                    )
            }catch (e: Exception){
                Log.i("unfollowArtist","Failure $e")
            }
        }
    }

    private fun followArtist(){
        firebaseRepo.followObject(user.id,playlistInfo.id,"artist")
        uiScope.launch {
            try {
                ToneApi.retrofitService
                    .followArtist(
                        "Bearer $token",
                        playlistInfo.id)
            }catch (e: Exception){
                Log.i("followArtist","Failure $e")
            }
        }
    }

    fun navigateToMoreTracks(){
        _isNavigateToMoreTracks.value = playlistInfo
    }

    fun navigateToMoreTracksComplete(){
        _isNavigateToMoreTracks.value = null
    }

    fun navigateToMoreAlbums(){
        _isNavigateToMoreAlbums.value = playlistInfo
    }

    fun navigateToMoreAlbumsComplete(){
        _isNavigateToMoreAlbums.value = null
    }

    fun displayPlaylistDetails(dataItem: LibraryAdapter.DataItem){
        _navigateToDetailPlaylist.value = PlaylistInfo(
            dataItem.id.toString(),
            dataItem.name.toString(),
            dataItem.description.toString(),
            dataItem.image,
            dataItem.typeName.toString()
        )
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun displayPlaylistDetailsComplete() {
        _navigateToDetailPlaylist.value = null
    }

    override fun onCleared() {
        super.onCleared()
        uiScope.cancel()
    }
}