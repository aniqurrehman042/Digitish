package com.example.test_04.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.test_04.R
import com.example.test_04.ui.CustomerHome
import com.example.test_04.ui.MerchantHome
import com.example.test_04.utils.PreferencesUtils
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*


class NotificationFirebaseService : FirebaseMessagingService() {

    private val ADMIN_CHANNEL_ID = "admin_channel"

    private val SUBSCRIBE_TO = "Enter_your_topic_name"

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        val receiverMerchant: Boolean = p0.data["Merchant"]!!.toBoolean()
        val receiverNameOrEmail = p0.data["Receiver Name Or Email"]
        val senderNameOrEmail = p0.data["Sender Name Or Email"]
        val review = p0.data["Review"]
        val title: String = p0.data["title"].toString()
        val message: String = p0.data["message"].toString()

        var intent = Intent(this, CustomerHome::class.java)

        if (receiverMerchant)
            intent = Intent(this, MerchantHome::class.java)

        intent.putExtra("Review", review)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random().nextInt(3000)

        /*
        Apps targeting SDK 26 or above (Android O) must implement notification channels and add its notifications
        to at least one of them.
      */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupChannels(notificationManager)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT
        )

        val largeIcon = BitmapFactory.decodeResource(
                resources,
                R.drawable.digitish_round
        )

        var isMe: Boolean

        isMe = if (receiverMerchant) {
            val myMerchantName = PreferencesUtils.getMerchantDetail(this, "Merchant Name")
            receiverNameOrEmail == myMerchantName
        } else {
            val myCustomerName = PreferencesUtils.getCustomerDetail(this, "Email")
            receiverNameOrEmail == myCustomerName
        }

        if (isMe) {
            val notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationBuilder = NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
                    .setSmallIcon(R.drawable.digitish_round)
                    .setLargeIcon(largeIcon)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setSound(notificationSoundUri)
                    .setContentIntent(pendingIntent)

            //Set notification color to match your app color template
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                notificationBuilder.color = resources.getColor(R.color.darkest)
            }
            notificationManager.notify(notificationID, notificationBuilder.build())
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun setupChannels(notificationManager: NotificationManager?) {
        val adminChannelName = "New notification"
        val adminChannelDescription = "Device to device notification"

        val adminChannel: NotificationChannel
        adminChannel = NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_HIGH)
        adminChannel.description = adminChannelDescription
        adminChannel.enableLights(true)
        adminChannel.lightColor = Color.RED
        adminChannel.enableVibration(true)
        notificationManager?.createNotificationChannel(adminChannel)
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)

        /*
          This method is invoked whenever the token refreshes
          OPTIONAL: If you want to send messages to this application instance
          or manage this apps subscriptions on the server side,
          you can send this token to your server.
        */
        /*
          This method is invoked whenever the token refreshes
          OPTIONAL: If you want to send messages to this application instance
          or manage this apps subscriptions on the server side,
          you can send this token to your server.
        */
        val token = p0

        // Once the token is generated, subscribe to topic with the userId

        // Once the token is generated, subscribe to topic with the userId
        FirebaseMessaging.getInstance().subscribeToTopic(SUBSCRIBE_TO)
        Log.i("TAG", "onTokenRefresh completed with token: $token")

    }
}
