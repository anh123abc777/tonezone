package com.example.tonezone.player

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tonezone.network.User
import com.google.firebase.auth.FirebaseUser

@Suppress("UNCHECKED_CAST")
class PlayerScreenViewModelFactory
    (val application: Application,
     val user: FirebaseUser
): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(PlayerScreenViewModel::class.java))
            return PlayerScreenViewModel(application,user) as T
        throw IllegalArgumentException("Unknown VM class")
    }
}