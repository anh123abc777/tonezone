package com.example.tonezone.notifycation

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.tonezone.MainActivity
import com.example.tonezone.R
import com.example.tonezone.network.Track
import com.example.tonezone.utils.createBitmapFromUrl
import com.example.tonezone.utils.displayArtistNames
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking


class CreateNotification {

    val CHANNEL_ID = "channel"

    val ACTION_PREVIOUS = "action_previous"
    val ACTION_PLAY =  "action_play"
    val ACTION_NEXT = "action_next"

    lateinit var notification: Notification

    fun createNotification(context: Context, track: Track, playButton: Int, pos: Int,size: Int){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationManagerCompat = NotificationManagerCompat.from(context)
            val mediaSessionCompat = MediaSessionCompat(context,"tag")

            val thumbnail = runBlocking(Dispatchers.IO) { createBitmapFromUrl(context,track.album?.images?.get(0)?.url ?: "")}

            /** Previous **/
            var pendingIntentPrevious: PendingIntent?
            var drwPrevious : Int
            if (pos==0){
                pendingIntentPrevious = null
                drwPrevious = 0
            }else{
                val intentPrevious = Intent(context,NotificationActionService::class.java)
                    .setAction(ACTION_PREVIOUS)
                pendingIntentPrevious = PendingIntent
                    .getBroadcast(context,
                        0,
                        intentPrevious,
                        PendingIntent.FLAG_UPDATE_CURRENT)
                drwPrevious = R.drawable.ic_previous
            }

            /** Play **/
            val intentPlay = Intent(context,NotificationActionService::class.java)
                .setAction(ACTION_PLAY)
            val pendingIntentPlay = PendingIntent
                .getBroadcast(context,
                    0,
                    intentPlay,
                    PendingIntent.FLAG_UPDATE_CURRENT)


            /** Next **/
            var pendingIntentNext: PendingIntent?
            var drwNext : Int
            if (pos==size-1){
                pendingIntentNext = null
                drwNext = 0
            }else{
                val intentNext = Intent(context,NotificationActionService::class.java)
                    .setAction(ACTION_NEXT)
                pendingIntentNext = PendingIntent
                    .getBroadcast(context,
                        0,
                        intentNext,
                        PendingIntent.FLAG_UPDATE_CURRENT)
                drwNext = R.drawable.ic_next
            }

            val contentIntent = PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT
            )

            notification = NotificationCompat.Builder(context,CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_home_24)
                .setContentTitle(track.name)
                .setContentText(displayArtistNames(track.artists!!))
                .setLargeIcon(thumbnail)
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .addAction(drwPrevious,"Previous",pendingIntentPrevious)
                .addAction(playButton,"Play",pendingIntentPlay)
                .addAction(drwNext,"Next",pendingIntentNext)
                .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0,1,2)
                    .setMediaSession(mediaSessionCompat.sessionToken))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(contentIntent)
                .build()


            notification.flags = notification.flags or Notification.FLAG_ONGOING_EVENT
            notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL

            notificationManagerCompat.notify(1,notification)
        }
    }
}