package com.example.tonezone

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.tonezone.database.Token
import com.example.tonezone.database.TokenRepository
import com.example.tonezone.database.TonezoneDB
import com.example.tonezone.databinding.ActivityMainBinding
import com.example.tonezone.network.SpotifyData
import com.example.tonezone.network.ToneApi
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import com.spotify.sdk.android.auth.LoginActivity.REQUEST_CODE
import kotlinx.coroutines.*
const val REDIRECT_URI = "com.tonezone://callback"


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private  var token = Token("")
    private lateinit var repository: TokenRepository
//    var exoPlayer: SimpleExoPlayer? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)


        val builder =
            AuthorizationRequest.Builder(
                "0546209c8b9b4b66a8d49037c566caa6",
                AuthorizationResponse.Type.TOKEN,
                REDIRECT_URI)
        builder.setScopes(arrayOf("streaming","playlist-read-private"))
        val request = builder.build()
        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request)


        repository = TokenRepository(TonezoneDB.getInstance(application).tokenDao)

        setupNav()

//        val extractorsFactory = DefaultExtractorsFactory()
//            .setTsExtractorFlags(DefaultTsPayloadReaderFactory.FLAG_ALLOW_NON_IDR_KEYFRAMES)
//            .setTsExtractorFlags(DefaultTsPayloadReaderFactory.FLAG_DETECT_ACCESS_UNITS)
//        exoPlayer = SimpleExoPlayer.Builder(application.applicationContext!!)
//            .setMediaSourceFactory(
//                DefaultMediaSourceFactory(
//                    application.applicationContext,
//                    extractorsFactory
//                )
//            )
//            .build()

    }


    private fun setupNav(){
        navController = findNavController(R.id.nav_host)
        binding.bottomBar.setupWithNavController(navController)
        binding.bottomBar.selectedItemId = R.id.homeFragment
    }


    var albumTrack = SpotifyData("nbnb,kjhlk", listOf(),0,"",0,"",0)

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            val response = AuthorizationClient.getResponse(resultCode, intent)
            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    token.value = response.accessToken
//                    gido(response)
                }
                AuthorizationResponse.Type.ERROR -> {
                    token.value =  "Not Found"

                }
                else -> {
                    token.value =  "Not Found"
                }
            }
        }

        runBlocking(Dispatchers.IO) {
            repository.clear()
        }
        runBlocking(Dispatchers.IO) {
            repository.insert(token)
        }
    }

    @SuppressLint("HardwareIds")
    private fun getToken(response: AuthorizationResponse) = runBlocking {
        try {

            var getAlbumTrackDeferred: Deferred<SpotifyData> = ToneApi.retrofitService
                .getAlbumTracksAsync(
                    "Bearer " + response.accessToken,

                    "ES"
                )
            albumTrack = getAlbumTrackDeferred.await()

        } catch (e: Exception) {
            albumTrack = SpotifyData(e.message!!, listOf(), 0, "", 0, "", 0)
        }
    }
//        val adapter = TrackAdapter(OnClickListener {
////            onStartMusic()
////            exoPlayer!!.prepare()
////            exoPlayer!!.play()
//            Toast.makeText(this@MainActivity,it.name,Toast.LENGTH_SHORT).show()
//        })
////        binding.groupPlaylist.adapter = adapter
////        adapter.submitList(albumTrack.items)
//    }


//    fun onStartMusic() = runBlocking{
//        exoPlayer!!.clearMediaItems()
//            exoPlayer!!.addMediaItem(MediaItem.fromUri("http://api.mp3.zing.vn/api/streaming/audio/ZZUB0I0E/128"))
//
//        delay(600L)
//    }

    override fun onDestroy() {
        runBlocking(Dispatchers.IO) {
            repository.clear()
        }
        super.onDestroy()
    }
}