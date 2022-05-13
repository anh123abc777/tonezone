package com.example.tonezone

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.work.*
import com.example.tonezone.work.ToneZoneWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class DevByteApplication: Application() {

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    private fun delayedInit() {
        applicationScope.launch {
            setupRecurringWork()
        }
    }

    private fun setupRecurringWork() {

        val repeatingRequest
                = PeriodicWorkRequestBuilder<ToneZoneWorker>(1, TimeUnit.DAYS)
            .build()

//        WorkManager.getInstance(applicationContext).enqueue(repeatingRequest)

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            ToneZoneWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingRequest)
    }

    override fun onCreate() {
        super.onCreate()
        delayedInit()
        Log.i("ToneZoneWorker","ToneZoneWorker")
    }
}