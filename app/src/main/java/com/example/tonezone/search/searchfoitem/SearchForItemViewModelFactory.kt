package com.example.tonezone.search.searchfoitem

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class SearchForItemViewModelFactory (val application: Application): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(SearchForItemViewModel::class.java))
            return SearchForItemViewModel(application) as T
        throw IllegalArgumentException("Unknown VM class")
    }
}