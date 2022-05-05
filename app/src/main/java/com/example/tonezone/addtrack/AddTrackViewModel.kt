package com.example.tonezone.addtrack

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.tonezone.network.FirebaseRepository
import com.example.tonezone.network.Track
import com.example.tonezone.network.User

class AddTrackViewModel(user: User): ViewModel() {

    private val firebaseRepo = FirebaseRepository()
    private val _groupTracks = firebaseRepo.getRecommendedTracks(user.id)
    val groupTracks: LiveData<List<Track>>
        get() = _groupTracks
}