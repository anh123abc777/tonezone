package com.example.tonezone

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tonezone.network.ToneApi
import com.example.tonezone.network.UserProfile
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import com.spotify.sdk.android.auth.LoginActivity.REQUEST_CODE
import kotlinx.coroutines.launch

class MainViewModel(private val activity: Activity): ViewModel() {

    var token = ""

    lateinit var userProfile : UserProfile

    private val builder =
        AuthorizationRequest.Builder(
            "0546209c8b9b4b66a8d49037c566caa6",
            AuthorizationResponse.Type.TOKEN,
            REDIRECT_URI)

    fun initAuthorization(){
        builder.setScopes(arrayOf("streaming",
            "playlist-read-private",
            "playlist-read-collaborative",
            "user-follow-read",
            "user-library-read",
            "playlist-modify-private",
            "playlist-modify-public",
            "user-library-modify"
        ))
        val request = builder.build()

        AuthorizationClient.openLoginActivity(activity, REQUEST_CODE, request)

    }

    fun getUserProfileData(){
        viewModelScope.launch {
            userProfile = try {
                ToneApi.retrofitService.getUserProfile("Bearer $token")
            }catch (e: Exception){
                UserProfile()
            }
        }
    }

}