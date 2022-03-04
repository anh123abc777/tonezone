package com.example.tonezone.yourlibrary

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class YourLibraryViewModelFactory
    (val application: Application):ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(YourLibraryViewModel::class.java))
            return YourLibraryViewModel(application) as T
        throw IllegalArgumentException("Unknown VM class")
    }
}