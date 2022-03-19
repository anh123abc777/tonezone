package com.example.tonezone

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.tonezone.database.Token
import com.example.tonezone.database.TokenRepository
import com.example.tonezone.database.TonezoneDB
import com.example.tonezone.databinding.ActivityMainBinding
import com.example.tonezone.network.SpotifyData
import com.example.tonezone.network.ToneApi
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import com.spotify.sdk.android.auth.LoginActivity.REQUEST_CODE
import kotlinx.coroutines.*
const val REDIRECT_URI = "com.tonezone://callback"


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val viewModel : MainViewModel by viewModels {
        MainViewModelFactory(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)

        viewModel.initAuthorization()

        setupNav()

        temp()

    }

    private fun temp(){
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.isHideable = false

        Glide.with(binding.miniPlayer.thumbnail.context)
            .load("https://picsum.photos/200/200")
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.ic_baseline_home_24)
                    .error(R.drawable.ic_outline_search_24))
            .into(binding.miniPlayer.thumbnail)

        binding.miniPlayer.miniPlayerFrame.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }


    private fun setupNav(){
//        navController = findNavController(R.id.nav_host)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomBar.setupWithNavController(navController)
        binding.bottomBar.selectedItemId = R.id.homeFragment

    }

//    override fun onSupportNavigateUp(): Boolean {
//        val navController = this.findNavController(R.id.nav_host)
//        return navController.navigateUp()
//    }

    var albumTrack = SpotifyData("nbnb,kjhlk", listOf(),0,"",0,"",0)

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            val response = AuthorizationClient.getResponse(resultCode, intent)
            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    viewModel.token = response.accessToken
                }
                AuthorizationResponse.Type.ERROR -> {
                    viewModel.token =  "Not Found"

                }
                else -> {
                    viewModel.token =  "Not Found"
                }
            }
        }


    }
}