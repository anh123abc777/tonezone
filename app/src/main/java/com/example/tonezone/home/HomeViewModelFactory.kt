package com.example.tonezone.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tonezone.network.User
import com.google.firebase.auth.FirebaseUser
import java.lang.IllegalArgumentException

class HomeViewModelFactory(
    private val token: String,
    private val user: User,
    private val firebaseUser: FirebaseUser): ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(HomeViewModel::class.java))
            return HomeViewModel(token,user, firebaseUser) as T
        throw IllegalArgumentException("Unknown VM class")
    }

}