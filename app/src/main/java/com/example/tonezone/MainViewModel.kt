package com.example.tonezone

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import com.spotify.sdk.android.auth.LoginActivity.REQUEST_CODE

class MainViewModel(private val activity: Activity): ViewModel() {

    var token = ""

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
        ))
        val request = builder.build()

        AuthorizationClient.openLoginActivity(activity, REQUEST_CODE, request)

    }

}