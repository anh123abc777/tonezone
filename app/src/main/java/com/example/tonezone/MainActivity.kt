@file:Suppress("DEPRECATION")

package com.example.tonezone

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.palette.graphics.Palette
import com.example.tonezone.databinding.ActivityMainBinding
import com.example.tonezone.network.FirebaseRepository
import com.example.tonezone.network.Track
import com.example.tonezone.notifycation.CreateNotification
import com.example.tonezone.notifycation.OnClearFromRecentService
import com.example.tonezone.player.PlayerScreenViewModel
import com.example.tonezone.utils.createBitmapFromUrl
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking


const val REDIRECT_URI = "com.tonezone://callback"


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var notificationManager: NotificationManager

    private lateinit var mainViewModel: MainViewModel

    private val playerViewModel: PlayerScreenViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)

        val factory = MainViewModelFactory(this)
        mainViewModel = ViewModelProvider(this,factory).get(MainViewModel::class.java)

        binding.viewModel = playerViewModel
        binding.lifecycleOwner = this


        setupNav()
        setupMiniPlayer()

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            createChannel()
            registerReceiver(broadcastReceiver, IntentFilter("TRACKS_TRACKS"))
            startService(Intent(baseContext, OnClearFromRecentService::class.java))
        }
        setupPlayerNotification()
        handleLogin()
        setupSeekbar()
        setupPlayerNavController()

    }


    private fun setupSeekbar(){
        binding.simpleSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser) {
                    playerViewModel.seekTo(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
        setupProgressBackgroundTintSeekbar()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun setupProgressBackgroundTintSeekbar(){

        playerViewModel.currentTrack.observe(this) {
            if (it!=Track()){
                val bitmap = runBlocking(Dispatchers.IO) {
                    createBitmapFromUrl(
                        applicationContext,
                        playerViewModel.currentTrack.value?.album?.images?.get(0)?.url ?: ""
                    )
                }

    //             Asynchronous
                Palette.from(bitmap).generate { palette ->
                    val txtBackgroundColor = palette?.mutedSwatch?.bodyTextColor
                    binding.simpleSeekBar.progressBackgroundTintList =
                        txtBackgroundColor?.let { color ->
                            ColorStateList.valueOf(
                                color
                            )
                        }
                }
            }
        }
    }


    private fun handleLogin(){

        mainViewModel.firebaseAuth.observe(this){
            if (it!=null){
                val firebaseRepo = FirebaseRepository()
//                firebaseRepo.putRecommendedTracks(it.uid,this)
                mainViewModel.initUserFirebase()
                mainViewModel.firebaseUser.observe(this){
                    if (it!=null){
                        navigateToHome()
                    }
                }
            }else{
                navController.navigate(R.id.loginFragment)
            }
        }


    }

    private fun setupPlayerNotification(){
        playerViewModel.playerState.observeForever{ state ->

            playerViewModel.currentTrack.observeForever { track ->

                if (track != Track()) {

                    when (state) {
                        PlayerScreenViewModel.PlayerState.PLAY -> {
                            CreateNotification.createNotification(
                                this,
                                track,
                                R.drawable.ic_custom_pause,
                                playerViewModel.posSongSelectedInGroup(),
                                playerViewModel.currentPlaylist.value?.size ?: 0,
                            )
                        }

                        PlayerScreenViewModel.PlayerState.NONE ->{
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                notificationManager = getSystemService(NotificationManager::class.java)
                            }
                            notificationManager.cancelAll()
                        }

                        else -> {
                            CreateNotification.createNotification(
                                this,
                                track,
                                R.drawable.ic_custom_play,
                                playerViewModel.posSongSelectedInGroup(),
                                playerViewModel.currentPlaylist.value?.size ?: 0,
                            )
                        }
                    }
                }else{
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            notificationManager = getSystemService(NotificationManager::class.java)
                        }
                        notificationManager.cancelAll()

                }
            }
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent) {

            when(intent.extras?.get("action_name")){
                CreateNotification.ACTION_PREVIOUS -> {
                    playerViewModel.onPrevious()
                }

                CreateNotification.ACTION_PLAY -> {
                    playerViewModel.onChangeState()

                }

                CreateNotification.ACTION_NEXT -> {
                    playerViewModel.onNext()
                }

                else -> Toast.makeText(context,"WTF is this",Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun createChannel(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                CreateNotification.CHANNEL_ID,"ToneZone",NotificationManager.IMPORTANCE_LOW)

            notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setupMiniPlayer(){
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.isHideable = false

        binding.miniPlayer.miniPlayerFrame.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        binding.miniPlayer.playerViewModel = playerViewModel
    }


    private fun setupNav(){
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomBar.setupWithNavController(navController)


//        binding.bottomBar.selectedItemId = R.id.homeFragment


        navController.addOnDestinationChangedListener { a, destination, b ->
            when(destination.id){
                R.id.loginFragment,R.id.registerFragment,R.id.splashScreenFragment -> {
                    binding.bottomBar.visibility = View.GONE
                }
                else  -> {
                    binding.bottomBar.visibility = View.VISIBLE
                }

            }


            Log.i("backstack",navController.backQueue.map { it.destination.displayName }.toString())
        }

    }

    private fun setupPlayerNavController(){
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_player) as NavHostFragment

        val playerNavController =  navHostFragment.navController
        playerNavController.enableOnBackPressed(true)

        if (playerNavController.currentDestination != null){
            when(playerNavController.currentDestination?.id){
                R.id.yourPlaylistFragment -> {binding.playerController.visibility = View.GONE}
            }
        }
    }

    override fun onBackPressed() {
        val currentDestination= navController.currentDestination
        if (currentDestination != null) {
            when(currentDestination.id) {
                R.id.loginFragment,R.id.splashScreenFragment -> {
                    finish()
                }

                else -> {
                    super.onBackPressed()
                }
            }
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        // Check if result comes from the correct activity
//        if (requestCode == LoginActivity.REQUEST_CODE) {
//            val response = AuthorizationClient.getResponse(resultCode, intent)
//            when (response.type) {
//                AuthorizationResponse.Type.TOKEN -> {
//                    mainViewModel.token = response.accessToken
//                    mainViewModel.getCurrentUserProfileData()
//                }
//                AuthorizationResponse.Type.ERROR -> {
//                    mainViewModel.token =  "Not Found"
//
//                }
//                else -> {
//                    mainViewModel.token =  "Not Found"
//                }
//            }
//
//            runBlocking(Dispatchers.IO) {
//                repository.clear()
//            }
//
//            runBlocking(Dispatchers.IO) {
//                repository.insert(Token(response.accessToken))
//            }
//        }
    }

    private fun navigateToHome(){
                navController.popBackStack()
                navController.navigate(R.id.a)
    }

    override fun onDestroy() {
        super.onDestroy()

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            notificationManager.cancelAll()
        }
//        unregisterReceiver(broadcastReceiver)

    }

}