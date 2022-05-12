package com.example.tonezone.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tonezone.network.Category
import com.example.tonezone.network.FirebaseRepository
import com.example.tonezone.network.ToneApi
import kotlinx.coroutines.*

class SearchViewModel() : ViewModel() {

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _categories = MutableLiveData<List<Category>>()
    val categories : LiveData<List<Category>>
        get() = _categories

    private val firebaseRepo = FirebaseRepository()

    init {
        getGenres()
    }

    private fun getGenres(){
        _categories = firebaseRepo.getCategories()

    }

    override fun onCleared() {
        super.onCleared()
        uiScope.cancel()
    }
}