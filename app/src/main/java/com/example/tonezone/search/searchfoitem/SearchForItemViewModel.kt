package com.example.tonezone.search.searchfoitem

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tonezone.network.SearchedItem
import com.example.tonezone.network.ToneApi
import com.example.tonezone.yourlibrary.TypeItemLibrary
import kotlinx.coroutines.*

class SearchForItemViewModel(val token: String): ViewModel() {

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _type = MutableLiveData<TypeItemLibrary>()
    val type : LiveData<TypeItemLibrary>
        get() = _type

    private var _searchedItems = MutableLiveData<SearchedItem>()
    val searchedItems : LiveData<SearchedItem>
        get() = _searchedItems

    fun search(query: String) {
        uiScope.launch {
            _searchedItems.value = try {

               ToneApi.retrofitService
                    .searchForItemAsync("Bearer $token", query)

            } catch (e: Exception) {

                SearchedItem()

            }
        }
    }

    fun filterType(type: TypeItemLibrary){
        _type.value = type
    }

}