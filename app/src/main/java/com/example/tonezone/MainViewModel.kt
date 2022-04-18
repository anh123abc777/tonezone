package com.example.tonezone

import android.app.Activity
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tonezone.network.FirebaseRepository
import com.example.tonezone.network.ToneApi
import com.example.tonezone.network.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import com.spotify.sdk.android.auth.LoginActivity.REQUEST_CODE
import kotlinx.coroutines.launch

class MainViewModel(private val activity: Activity): ViewModel() {

    var token = ""

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _firebaseAuth = MutableLiveData<FirebaseUser?>()
    val firebaseAuth: LiveData<FirebaseUser?>
        get() = _firebaseAuth

    private val firebaseRepo = FirebaseRepository()
    private lateinit var _userFirebase: MutableLiveData<User>
    val userFirebase: LiveData<User>
        get() = _userFirebase


    private val _user = MutableLiveData<User>()
    val user : LiveData<User>
        get() = _user

    private val builder =
        AuthorizationRequest.Builder(
            "0546209c8b9b4b66a8d49037c566caa6",
            AuthorizationResponse.Type.TOKEN,
            REDIRECT_URI)

    init {
        _firebaseAuth.value = auth.currentUser
    }

    fun checkUser(email: String, password: String){
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    _firebaseAuth.value = auth.currentUser
                }else{
                    Toast.makeText(activity,"Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun initUserFirebase(){
        _userFirebase = firebaseRepo.getUserProfile(_firebaseAuth.value!!.uid)
    }

    fun initAuthorization(){
        builder.setScopes(arrayOf("streaming",
            "playlist-read-private",
            "playlist-read-collaborative",
            "user-follow-read",
            "user-library-read",
            "playlist-modify-private",
            "playlist-modify-public",
            "user-library-modify",
            "user-follow-modify",
        ))
        val request = builder.build()

        AuthorizationClient.openLoginActivity(activity, REQUEST_CODE, request)

    }

    fun getCurrentUserProfileData(){
        viewModelScope.launch {
            _user.value = try {
                ToneApi.retrofitService.getCurrentUserProfile("Bearer $token")
            }catch (e: Exception){
                User()
            }
        }
    }

    fun logout(){
        auth.signOut()
        _firebaseAuth.value = auth.currentUser
    }

}