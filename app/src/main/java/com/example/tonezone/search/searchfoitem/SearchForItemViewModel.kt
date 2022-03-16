package com.example.tonezone.search.searchfoitem

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tonezone.database.TokenRepository
import com.example.tonezone.database.TonezoneDB
import com.example.tonezone.network.SearchedItem
import com.example.tonezone.network.ToneApi
import com.example.tonezone.yourlibrary.TypeItemLibrary
import kotlinx.coroutines.*

class SearchForItemViewModel(application: Application): ViewModel() {

    private val tokenRepository = TokenRepository(TonezoneDB.getInstance(application).tokenDao)
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    val token = tokenRepository.token

    private var _type = MutableLiveData<TypeItemLibrary>()
    val type : LiveData<TypeItemLibrary>
        get() = _type

    private var _searchedItems = MutableLiveData<SearchedItem>()
    val searchedItems : LiveData<SearchedItem>
        get() = _searchedItems

    fun search(query: String) =
        uiScope.launch{
            _searchedItems.value = try {
                val searchedItemDeferred: Deferred<SearchedItem> = ToneApi.retrofitService
                    .searchForItemAsync("Bearer ${token.value!!.value}",query)
                Log.i("result",searchedItemDeferred.await().toString())
                searchedItemDeferred.await()
            } catch (e: Exception) {
                Log.i("result",e.message!!)
                SearchedItem()
            }
//            ToneApi.retrofitService2.searchForItemAsync("Bearer ${token.value!!.value}",query)
//                .enqueue(object: Callback<String> {
//                    override fun onResponse(call: Call<String>, response: Response<String>) {
//                        Log.i("searchedItems",response.body().toString())
//                    }
//
//                    override fun onFailure(call: Call<String>, t: Throwable) {
//                        Log.i("searchedItems","Failure ${t.message}")
//                    }
//                })
        }

    fun filterType(type: TypeItemLibrary){
        _type.value = type
    }

}