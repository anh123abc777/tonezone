package com.example.tonezone.search.searchfoitem

import android.content.ContentValues
import android.text.Editable
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tonezone.adapter.LibraryAdapter
import com.example.tonezone.network.*
import com.example.tonezone.utils.Type
import com.example.tonezone.yourlibrary.TypeItemLibrary
import kotlinx.coroutines.*
import kotlin.math.abs

class SearchForItemViewModel(val token: String): ViewModel() {

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _type = MutableLiveData<TypeItemLibrary>()
    val type : LiveData<TypeItemLibrary>
        get() = _type

    private var _searchedItems = MutableLiveData<List<LibraryAdapter.DataItem>>()
    val searchedItems : LiveData<List<LibraryAdapter.DataItem>>
        get() = _searchedItems

    private val _searchKey = MutableLiveData<Editable?>()
    val searchKey : LiveData<Editable?>
        get() = _searchKey

    private val firebaseRepo = FirebaseRepository()

    fun searchInFirebaseOtherWay(query: Editable?){
        _searchKey.value = query

        if (query != null && query.isNotBlank() && query.isNotEmpty()){
            val querySting = query.toString().lowercase()
            val list = mutableListOf<LibraryAdapter.DataItem>()

            //Artist
            firebaseRepo.db.collection("Artist")
                .whereGreaterThanOrEqualTo("name",querySting)
                .limit(5)
                .addSnapshotListener { value, _ ->
                    if (value!=null && !value.isEmpty) {
                        val artists = value.toObjects(Artist::class.java)
                        list += artists.map { LibraryAdapter.DataItem.ArtistItem(it) }
                        val temp =
                            list.sortedBy { it.name?.lowercase()?.compareTo(querySting) }
                        _searchedItems.value = temp

                    }
                }

            //Track
            firebaseRepo.db.collection("Track")
                .whereGreaterThanOrEqualTo("name",querySting)
                .limit(10)
                .addSnapshotListener { value, _ ->
                    if (value!=null && !value.isEmpty) {
                        val tracks = value.toObjects(Track::class.java)
                        list += tracks.map { LibraryAdapter.DataItem.TrackItem(it) }
                        val temp =
                            list.sortedBy { it.name?.lowercase()?.compareTo(querySting) }
                        _searchedItems.value = temp

                    }
                }

            //Playlist
            firebaseRepo.db.collection("Playlist")
                .whereGreaterThanOrEqualTo("name",querySting)
                .limit(5)
                .addSnapshotListener { value, _ ->
                    if (value!=null && !value.isEmpty) {
                        val playlists = value.toObjects(Playlist::class.java)
                        list += playlists.map { LibraryAdapter.DataItem.PlaylistItem(it) }
                        val temp =
                            list.sortedBy { it.name?.lowercase()?.compareTo(querySting) }
                        _searchedItems.value = temp

                    }
                }

            //Album
            firebaseRepo.db.collection("Album")
                .whereGreaterThanOrEqualTo("name",querySting)
                .limit(5)
                .addSnapshotListener { value, _ ->
                    if (value!=null && !value.isEmpty) {
                        val albums = value.toObjects(Album::class.java)
                        list += albums.map { LibraryAdapter.DataItem.AlbumItem(it) }
                        val temp =
                            list.sortedBy { it.name?.lowercase()?.compareTo(querySting) }
                        _searchedItems.value = temp

                    }
                }

        }
    }

    init {
        _searchKey.value = null
    }

    fun clearResult(){
        _searchedItems.value = listOf()
    }

    fun searchInFirebase(query: Editable?){
        _searchKey.value = query

        if (query != null && query.toString()!="")
            if(query.isNotBlank() && query.isNotEmpty()) {
                firebaseRepo.db.collection("Search")
                    .whereArrayContains("search_keywords", query.toString().lowercase())
                    .limit(50)
                    .get()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val result =
                                it.result.toObjects(FirebaseRepository.SearchModel::class.java)

                            var list = listOf<LibraryAdapter.DataItem>()
                            result.forEach { item ->
                                when (item.type) {

                                    "track" -> {
                                        firebaseRepo.db.collection("Track")
                                            .whereEqualTo("id", item.id)
                                            .get()
                                            .addOnCompleteListener { doc ->
                                                if (doc.isSuccessful) {
                                                    val track =
                                                        doc.result.toObjects(Track::class.java)
                                                    list += listOf(
                                                        LibraryAdapter.DataItem.TrackItem(
                                                            track[0]
                                                        )
                                                    )
                                                    val temp = list.sortedBy { abs(it.name?.lowercase()?.compareTo(query.toString())!!)}
                                                    _searchedItems.value = temp

                                                }
                                            }
                                    }

                                    "album" -> {
                                        firebaseRepo.db.collection("Album")
                                            .whereEqualTo("id", item.id)
                                            .get()
                                            .addOnCompleteListener { doc ->
                                                if (doc.isSuccessful) {
                                                    val album =
                                                        doc.result.toObjects(Album::class.java)
                                                    list += listOf(
                                                        LibraryAdapter.DataItem.AlbumItem(
                                                            album[0]
                                                        )
                                                    )
                                                    val temp = list.sortedBy { abs(it.name?.lowercase()?.compareTo(query.toString())!!)}
                                                    _searchedItems.value = temp
                                                }
                                            }
                                    }

                                    "artist" -> {
                                        firebaseRepo.db.collection("Artist")
                                            .whereEqualTo("id", item.id)
                                            .get()
                                            .addOnCompleteListener { doc ->
                                                if (doc.isSuccessful) {
                                                    val artist =
                                                        doc.result.toObjects(Artist::class.java)
                                                    list += listOf(
                                                        LibraryAdapter.DataItem.ArtistItem(
                                                            artist[0]
                                                        )
                                                    )
                                                    val temp = list.sortedBy { abs(it.name?.lowercase()?.compareTo(query.toString())!!)}
                                                    _searchedItems.value = temp
                                                }
                                            }
                                    }

                                    "playlist" -> {
                                        firebaseRepo.db.collection("Playlist")
                                            .whereEqualTo("id", item    .id)
                                            .get()
                                            .addOnCompleteListener { doc ->
                                                if (doc.isSuccessful) {
                                                    val playlist =
                                                        doc.result.toObjects(Playlist::class.java)
                                                    list += listOf(
                                                        LibraryAdapter.DataItem.PlaylistItem(
                                                            playlist[0]
                                                        )
                                                    )
                                                    val temp = list.sortedBy { abs(it.name?.lowercase()?.compareTo(query.toString())!!)}
                                                    _searchedItems.value = temp
                                                }
                                            }
                                    }
                                }
                            }


                        } else {
                            Log.d(ContentValues.TAG, "Failure: ${it.exception!!.message}")
                        }
                    }
            }
            else
                _searchedItems.value = listOf()

        else
            _searchedItems.value = listOf()


    }

    fun searchInFirebase(query: Editable?, type: Type){
        _searchKey.value = query
        if (query != null && query.toString()!="")
            if(query.isNotBlank() && query.isNotEmpty()) {
                firebaseRepo.db.collection("Search")
                    .whereArrayContains("search_keywords", query.toString().lowercase())
                    .whereEqualTo("type",type.name.lowercase())
                    .limit(20)
                    .get()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val result =
                                it.result.toObjects(FirebaseRepository.SearchModel::class.java)

                            var list = listOf<LibraryAdapter.DataItem>()
                            result.forEach { item ->
                                when (item.type) {
                                    "track" -> {
                                        firebaseRepo.db.collection("Track")
                                            .whereEqualTo("id", item.id)
                                            .get()
                                            .addOnCompleteListener { doc ->
                                                if (doc.isSuccessful) {
                                                    val track =
                                                        doc.result.toObjects(Track::class.java)
                                                    list += listOf(
                                                        LibraryAdapter.DataItem.TrackItem(
                                                            track[0]
                                                        )
                                                    )
                                                    _searchedItems.value = list.sortedByDescending { it.name?.lowercase()?.compareTo(query.toString()) }
                                                }
                                            }
                                    }

                                    "album" -> {
                                        firebaseRepo.db.collection("Album")
                                            .whereEqualTo("id", item.id)
                                            .get()
                                            .addOnCompleteListener { doc ->
                                                if (doc.isSuccessful) {
                                                    val album =
                                                        doc.result.toObjects(Album::class.java)
                                                    list += listOf(
                                                        LibraryAdapter.DataItem.AlbumItem(
                                                            album[0]
                                                        )
                                                    )
                                                    _searchedItems.value = list.sortedByDescending { it.name?.lowercase()?.compareTo(query.toString()) }
                                                }
                                            }
                                    }

                                    "artist" -> {
                                        firebaseRepo.db.collection("Artist")
                                            .whereEqualTo("id", item.id)
                                            .get()
                                            .addOnCompleteListener { doc ->
                                                if (doc.isSuccessful) {
                                                    val artist =
                                                        doc.result.toObjects(Artist::class.java)
                                                    list += listOf(
                                                        LibraryAdapter.DataItem.ArtistItem(
                                                            artist[0]
                                                        )
                                                    )
                                                    _searchedItems.value = list.sortedByDescending { it.name?.lowercase()?.compareTo(query.toString()) }
                                                }
                                            }
                                    }

                                    "playlist" -> {
                                        firebaseRepo.db.collection("Playlist")
                                            .whereEqualTo("id", item.id)
                                            .get()
                                            .addOnCompleteListener { doc ->
                                                if (doc.isSuccessful) {
                                                    val playlist =
                                                        doc.result.toObjects(Playlist::class.java)
                                                    list += listOf(
                                                        LibraryAdapter.DataItem.PlaylistItem(
                                                            playlist[0]
                                                        )
                                                    )
                                                    _searchedItems.value = list.sortedByDescending { it.name?.lowercase()?.compareTo(query.toString()) }
                                                }
                                            }
                                    }
                                }
                            }
                        } else {
                            Log.d(ContentValues.TAG, "Failure: ${it.exception!!.message}")
                        }
                    }
            }
    }

    fun removeItem(item: LibraryAdapter.DataItem){
        _searchedItems.value = _searchedItems.value?.minus(item)
    }


//    fun search(query: Editable?) {
//        if (query != null)
//            if(query.isNotBlank() && query.isNotEmpty()){
//                _searchKey.value = query.toString()
//                uiScope.launch {
//                    _searchedItems.value = try {
//
//                        ToneApi.retrofitService
//                            .searchForItemAsync("Bearer $token", _searchKey.value!!)
//
//                    } catch (e: Exception) {
//
//                        SearchedItem()
//
//                    }
//                }
//            }
//            else
//                _searchedItems.value = SearchedItem()
//
//        else
//            _searchedItems.value = SearchedItem()
//    }

    fun filterType(type: TypeItemLibrary){
        _type.value = type
    }

}