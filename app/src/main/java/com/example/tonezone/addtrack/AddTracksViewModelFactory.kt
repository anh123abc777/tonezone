package com.example.tonezone.addtrack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tonezone.network.User

class AddTracksViewModelFactory(val user: User): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddTrackViewModel::class.java))
            return AddTrackViewModel(user) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}