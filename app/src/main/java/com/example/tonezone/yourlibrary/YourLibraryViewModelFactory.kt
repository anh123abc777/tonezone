package com.example.tonezone.yourlibrary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tonezone.network.User
import com.google.firebase.auth.FirebaseUser

@Suppress("UNCHECKED_CAST")
class YourLibraryViewModelFactory
    (val firebaseUser: FirebaseUser):ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(YourLibraryViewModel::class.java))
            return YourLibraryViewModel(firebaseUser) as T
        throw IllegalArgumentException("Unknown VM class")
    }
}