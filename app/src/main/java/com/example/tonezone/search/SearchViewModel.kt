package com.example.tonezone.search

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tonezone.database.TokenRepository
import com.example.tonezone.database.TonezoneDB
import com.example.tonezone.network.ToneApi
import com.example.tonezone.network.Topic
import kotlinx.coroutines.*

class SearchViewModel(application: Application) : ViewModel() {

    private val tokenRepository = TokenRepository(TonezoneDB.getInstance(application).tokenDao)
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    val token = tokenRepository.token

    private var _topics = MutableLiveData<Topic>()
    val topics : LiveData<Topic>
        get() = _topics

    fun getGenres() =
        runBlocking{
            _topics.value = try {
                var getGenres: Deferred<Topic> = ToneApi.retrofitService
                    .getGenresAsync("Bearer ${token.value!!.value}")
                getGenres.await()
            } catch (e: Exception) {
                Log.i("error",token.value!!.value!!)
                Topic(listOf("alo","á lồ"))
            }

    }

}