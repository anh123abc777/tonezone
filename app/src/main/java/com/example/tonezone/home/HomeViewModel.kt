package com.example.tonezone.home

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.tonezone.database.Token
import com.example.tonezone.database.TokenRepository
import com.example.tonezone.database.TonezoneDB

class HomeViewModel(application: Application) : ViewModel() {

    val repository = TokenRepository(TonezoneDB.getInstance(application).tokenDao)
    var token = repository.token

}