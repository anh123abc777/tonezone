package com.example.tonezone

import SpotifyData
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.tonezone.databinding.ActivityMainBinding
import com.example.tonezone.network.ToneApi
import com.spotify.sdk.android.auth.AccountsQueryParameters.CLIENT_ID
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import com.spotify.sdk.android.auth.LoginActivity.REQUEST_CODE
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)

        val REDIRECT_URI = "https://tonezone.com/callback/"

        val builder =
            AuthorizationRequest.Builder("0546209c8b9b4b66a8d49037c566caa6", AuthorizationResponse.Type.TOKEN, REDIRECT_URI)

        builder.setScopes(arrayOf("streaming"))
        val request = builder.build()

        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request)

        setupNav()
    }

    private fun setupNav(){
        navController = findNavController(R.id.nav_host)
        binding.bottomBar.setupWithNavController(navController)
        binding.bottomBar.selectedItemId = R.id.homeFragment

        NavigationUI.setupActionBarWithNavController(this,navController)
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.homeFragment,R.id.searchFragment,R.id.yourLibraryFragment))
        setupActionBarWithNavController(navController,appBarConfiguration)
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val uri = intent.data

        if (uri != null) {
            val response = AuthorizationResponse.fromUri(uri)
            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                }
                AuthorizationResponse.Type.ERROR -> {
                }
                else -> {
                }
            }
        }
    }

    var albumTrack = SpotifyData("nbnb,kjhlk", listOf(),0,"",0,"",0)

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            val response = AuthorizationClient.getResponse(resultCode, intent)
            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
//                    binding.textView.text= response.accessToken
                    gido(response)
//                    binding.textView.text =  "Success" + albumTrack.href

                }
                AuthorizationResponse.Type.ERROR -> {
//                    binding.textView.text =  "Not Found"

                }
                else -> {
//                    binding.textView.text =  "Not Found"
                }
            }
        }
    }

    private fun gido(response: AuthorizationResponse) = runBlocking {
        try {
            var getAlbumTrackDeferred : Deferred<SpotifyData>

            getAlbumTrackDeferred = ToneApi.retrofitService
                .getAlbumStracks(
                    "Bearer " + response.accessToken,

                    "ES"
                )
            albumTrack = getAlbumTrackDeferred.await()

        }catch (e: Exception){
            albumTrack =  SpotifyData(e.message!!, listOf(),0,"",0,"",0)
        }
    }
}