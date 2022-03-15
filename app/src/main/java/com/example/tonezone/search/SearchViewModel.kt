package com.example.tonezone.search

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tonezone.database.TokenRepository
import com.example.tonezone.database.TonezoneDB
import com.example.tonezone.network.CategoriesObject
import com.example.tonezone.network.Category
import com.example.tonezone.network.ToneApi
import com.example.tonezone.network.Topic
import kotlinx.coroutines.*

class SearchViewModel(application: Application) : ViewModel() {

    private val tokenRepository = TokenRepository(TonezoneDB.getInstance(application).tokenDao)
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    val token = tokenRepository.token

    private var _categories = MutableLiveData<List<Category>>()
    val categories : LiveData<List<Category>>
        get() = _categories

    fun getGenres() =
        uiScope.launch{
            _categories.value = try {
                var categoriesDeferred: Deferred<CategoriesObject> = ToneApi.retrofitService
                    .getCategoriesAsync("Bearer ${token.value!!.value}")
                categoriesDeferred.await().categories.items
            } catch (e: Exception) {
                Log.i("error",e.message.toString())
                listOf()
            }
    }

    override fun onCleared() {
        super.onCleared()
        uiScope.cancel()
    }
}