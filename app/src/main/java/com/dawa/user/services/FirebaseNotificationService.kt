package com.dawa.user.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.dawa.user.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

private const val TAG = "FirebaseNotificationSer"

class FirebaseNotificationService : FirebaseMessagingService() {


    override fun onNewToken(token: String) {
        // ignore - we use topic rather than token
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {


        Log.d(TAG, "From: ${remoteMessage.from}")

        val message = remoteMessage.data["message"].toString()
        val title = remoteMessage.data["title"].toString()
        val chanel = remoteMessage.data["chanel"].toString()

        sendNotification(message, title, chanel)

    }

    private fun sendNotification(messageBody: String, title: String, chanel: String) {

//        val intent = Intent(this, HomeActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

//        val pendingIntent = PendingIntent.getActivity(this, 0, null, PendingIntent.FLAG_ONE_SHOT)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, chanel).setContentTitle(title)
            .setSmallIcon(R.drawable.ic_add_black_24dp).setContentText(messageBody)
            .setAutoCancel(true).setSound(defaultSoundUri)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(chanel, "Dawa", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(Random(100).nextInt(), notificationBuilder.build())
    }


}