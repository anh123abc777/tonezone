package com.example.tonezone.search.result

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tonezone.search.SearchViewModel
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class ResultViewModelFactory
    (val application: Application,
     private val genreName: String): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ResultViewModel::class.java))
            return ResultViewModel(application,genreName) as T
        throw IllegalArgumentException("Unknown VM class")
    }
}