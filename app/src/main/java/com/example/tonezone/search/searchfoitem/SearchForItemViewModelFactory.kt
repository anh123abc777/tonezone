package com.example.tonezone.search.searchfoitem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class SearchForItemViewModelFactory (): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(SearchForItemViewModel::class.java))
            return SearchForItemViewModel() as T
        throw IllegalArgumentException("Unknown VM class")
    }
}