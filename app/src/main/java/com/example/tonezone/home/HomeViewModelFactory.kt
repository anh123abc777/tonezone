package com.example.tonezone.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tonezone.network.User
import com.google.firebase.auth.FirebaseUser
import java.lang.IllegalArgumentException

class HomeViewModelFactory(
    private val user: User): ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(HomeViewModel::class.java))
            return HomeViewModel( user) as T
        throw IllegalArgumentException("Unknown VM class")
    }

}