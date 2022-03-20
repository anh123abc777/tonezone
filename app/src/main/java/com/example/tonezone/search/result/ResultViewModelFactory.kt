package com.example.tonezone.search.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class ResultViewModelFactory
    (val token: String,
     private val genreName: String): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ResultViewModel::class.java))
            return ResultViewModel(token,genreName) as T
        throw IllegalArgumentException("Unknown VM class")
    }
}