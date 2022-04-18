package com.example.tonezone.login

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginViewModel(val application: Application): ViewModel() {

    private val auth: FirebaseAuth= FirebaseAuth.getInstance()

    private val _registerUser= MutableLiveData<Boolean?>()
    val registerUser: LiveData<Boolean?>
        get() = _registerUser

    private val _isLoggingIn = MutableLiveData<Boolean>()
    val isLoggingIn: LiveData<Boolean>
        get() = _isLoggingIn

    init {
        _isLoggingIn.value = false
    }

    fun login(){
        _isLoggingIn.value = true
    }

    fun checkUserComplete(){
        _isLoggingIn.value = false
    }

    fun register(){
        _registerUser.value = true
    }

    fun navigateRegisterComplete(){
        _registerUser.value = null
    }

    fun forgotPassword(){

    }
}