package com.example.tonezone.yourlibrary

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tonezone.adapter.LibraryAdapter
import com.example.tonezone.network.*
import com.example.tonezone.utils.ObjectRequest
import com.example.tonezone.utils.Signal
import com.example.tonezone.utils.Type
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*


class YourLibraryViewModel(val firebaseUser: User) : ViewModel() {

    private val job = Job()
    private val uiScope = CoroutineScope(job + Dispatchers.Main)
    private val firebaseRepo = FirebaseRepository()

    private val _dataItems = firebaseRepo.getFollowedObjects(firebaseUser.id)
    val dataItems : LiveData<List<LibraryAdapter.DataItem>>
        get() = _dataItems

    private val _sortOption = MutableLiveData<SortOption?>()
    val sortOption : LiveData<SortOption?>
        get() = _sortOption

    private val _type = MutableLiveData<TypeItemLibrary>()
    val type : LiveData<TypeItemLibrary>
        get() = _type

    private val _navigateToDetailPlaylist= MutableLiveData<PlaylistInfo>()
    val navigateToDetailPlaylist : LiveData<PlaylistInfo>
        get() = _navigateToDetailPlaylist

    private val _objectShowBottomSheet = MutableLiveData<Pair<ObjectRequest,String>>()
    val objectShowBottomSheet : LiveData<Pair<ObjectRequest,String>>
        get() = _objectShowBottomSheet

    private var _receivedSignal = MutableLiveData<Signal>()
    val receivedSignal : LiveData<Signal>
        get() = _receivedSignal


    init {

//        getDataFollowedArtists()
//        getDataUserPlaylists()
//        getDataSavedAlbums()
        _sortOption.value = SortOption.Alphabetical
        _type.value = TypeItemLibrary.All

    }
//
//     fun getDataUserPlaylists(){
//         _userPlaylists = firebaseRepo.getLikedPlaylists(firebaseUser.id)
//    }
//
//    private fun getDataFollowedArtists(){
//        _followedArtists = firebaseRepo.getFollowedArtists(firebaseUser.id)
//    }
//
//    private fun getDataSavedAlbums(){
//        _savedAlbums = firebaseRepo.getFollowedAlbums(firebaseUser.id)
//    }

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
            dataItem.typeName.toString()
        )
    }

    private fun displayPlaylistDetails(playlist: Playlist){
        _navigateToDetailPlaylist.value = PlaylistInfo(
            id=playlist.id!!,
            name = playlist.name!!,
            description = playlist.description!!,
            image = playlist.images?.get(0)?.url,
            type = playlist.type!!
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

        val yourPlaylistNum= _dataItems.value!!.filter { it.typeName=="playlist" }
            .map { (it as LibraryAdapter.DataItem.PlaylistItem).playlist }
            .filter { it.owner!!.id==firebaseUser.id }.size+1

        val name = if(playlistName=="") "My playlist #$yourPlaylistNum" else playlistName

        val playlist = Playlist(
            name = name,
            type = "playlist",
            owner = Owner(firebaseUser.display_name,firebaseUser.id),
            id = firebaseUser.id
        )
        firebaseRepo.db.collection("Playlist")
            .document()
            .set(playlist)
            .addOnSuccessListener {

                firebaseRepo.db.collection("Playlist")
                    .whereEqualTo("id",firebaseUser.id)
                    .get()
                    .addOnCompleteListener{ documents ->
                        if (documents!=null && documents.isSuccessful){
                            for (doc in documents.result.documents){
                                playlist.id = doc.id

                                firebaseRepo.db.collection("Playlist")
                                    .document(doc.id)
                                    .set(playlist)
                                    .addOnCompleteListener {

                                        firebaseRepo.followObject(
                                            firebaseUser.id,
                                            doc.id,
                                            Type.PLAYLIST
                                        )
                                        displayPlaylistDetails(playlist)
                                    }
                            }
                        }
                    }
            }
//        val playlist = firebaseRepo.createPlaylist(playlistName,firebaseUser)
////        getDataUserPlaylists()

    }

    fun showBottomSheet(objectRequest: ObjectRequest,itemId : String){
        _objectShowBottomSheet.value = Pair(objectRequest,itemId)
    }


    @SuppressLint("NullSafeMutableLiveData")
    fun handleSignalComplete(){
        _receivedSignal.value = null
        showBottomSheetComplete()
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun showBottomSheetComplete(){
        _objectShowBottomSheet.value = null
    }

    fun handleSignal(){
        when(_receivedSignal.value){
            null -> Log.i("receivedSignal","don't have happen")

            Signal.LIKED_PLAYLIST -> unlikePlaylist()

            Signal.DELETE_PLAYLIST -> deletePlaylist()

            Signal.STOP_FOLLOWING -> unfollowArtist()

            Signal.PIN_PLAYLIST -> pinObject()

            Signal.UNPIN_PLAYLIST -> unpinObject()

            Signal.UNPIN_ARTIST -> unpinObject()

            Signal.PIN_ARTIST -> pinObject()

            else -> Log.i("receivedSignal","what is this???????")
        }
    }

    private fun unpinObject(){
        firebaseRepo.unpinObject(firebaseUser.id,_objectShowBottomSheet.value!!.second)
    }

    private fun pinObject(){
        firebaseRepo.pinObject(firebaseUser.id,_objectShowBottomSheet.value!!.second)
    }

    private fun unfollowArtist(){
        firebaseRepo
            .unfollowObject(firebaseUser.id,_objectShowBottomSheet.value!!.second)
//        getDataFollowedArtists()
    }

    private fun unlikePlaylist(){
        firebaseRepo.unfollowObject(firebaseUser.id,_objectShowBottomSheet.value!!.second)
    }

    private fun deletePlaylist(){
        firebaseRepo.deletePlaylist(firebaseUser.id,_objectShowBottomSheet.value!!.second)
//        getDataUserPlaylists()
    }

    fun receiveSignal(signal: Signal){
        _receivedSignal.value = signal
    }

    override fun onCleared() {
        super.onCleared()
        uiScope.cancel()
    }
}