package com.example.tonezone.work

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.tonezone.network.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth

private const val TAG = "ToneZoneWorker"

class ToneZoneWorker(context: Context,params: WorkerParameters): Worker(context,params) {
    override fun doWork(): Result {

        val user =  FirebaseAuth.getInstance().currentUser

        user?.let {
            FirebaseRepository().putRecommendedTracks(it.uid)
            Log.i(TAG, "${user.uid}")
        }

        return try {
            Result.success(workDataOf("id" to user?.uid))
        }catch (e: Exception){
            Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "UploadDataWorker"
    }

}