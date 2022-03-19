package com.example.tonezone.yourlibrary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Suppress("UNCHECKED_CAST")
class YourLibraryViewModelFactory
    (val token: String):ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(YourLibraryViewModel::class.java))
            return YourLibraryViewModel(token) as T
        throw IllegalArgumentException("Unknown VM class")
    }
}