package com.example.tonezone.search.searchfoitem

import android.content.ContentValues
import android.text.Editable
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tonezone.adapter.LibraryAdapter
import com.example.tonezone.network.*
import com.example.tonezone.yourlibrary.TypeItemLibrary
import kotlinx.coroutines.*

class SearchForItemViewModel(val token: String): ViewModel() {

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _type = MutableLiveData<TypeItemLibrary>()
    val type : LiveData<TypeItemLibrary>
        get() = _type

    private var _searchedItems = MutableLiveData<List<LibraryAdapter.DataItem>>()
    val searchedItems : LiveData<List<LibraryAdapter.DataItem>>
        get() = _searchedItems

    private val _searchKey = MutableLiveData<String>()
    val searchKey : LiveData<String>
        get() = _searchKey

    private val firebaseRepo = FirebaseRepository()

    fun searchInFirebase(query: Editable?){
        if (query != null)
            if(query.isNotBlank() && query.isNotEmpty()) {
                firebaseRepo.db.collection("Search")
                    .whereArrayContains("search_keywords", query.toString())
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
                                                    _searchedItems.value = list
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
                                                    _searchedItems.value = list
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
                                                    _searchedItems.value = list
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
                                                    _searchedItems.value = list
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