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
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.test_04.R
import com.example.test_04.ui.CustomerHome
import com.example.test_04.ui.MerchantHome
import com.example.test_04.utils.PreferencesUtils
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

class NotificationFirebaseService : FirebaseMessagingService() {

    private val ADMIN_CHANNEL_ID = "admin_channel"

    override fun onMessageReceived(p0: RemoteMessage?) {
        super.onMessageReceived(p0)

        val receiverMerchant: Boolean = p0?.data?.get("Merchant")!!.toBoolean()
        val receiverNameOrEmail = p0?.data?.get("Receiver Name Or Email")
        val senderNameOrEmail = p0?.data?.get("Sender Name Or Email")
        val review: Boolean = p0?.data?.get("Review")!!.toBoolean()

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
        var isReviewByThisCustomer: Boolean = false

        isMe = if (receiverMerchant) {
            val myMerchantName = PreferencesUtils.getMerchantDetail(this, "Merchant Name")
            val myCustomerName = PreferencesUtils.getCustomerDetail(this, "Email")
            if ((senderNameOrEmail == myCustomerName && review))
                isReviewByThisCustomer = true
            receiverNameOrEmail == myMerchantName || (senderNameOrEmail == myCustomerName && review)
        } else {
            val myCustomerName = PreferencesUtils.getCustomerDetail(this, "Email")
            receiverNameOrEmail == myCustomerName
        }

        var title: String = p0?.data?.get("title").toString()
        var message: String = p0?.data?.get("message").toString()

        if (isReviewByThisCustomer) {
            title = "Review Published"
            message = "Your review has been published"
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
        val adminChannelDescription = "Device to devie notification"

        val adminChannel: NotificationChannel
        adminChannel = NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_HIGH)
        adminChannel.description = adminChannelDescription
        adminChannel.enableLights(true)
        adminChannel.lightColor = Color.RED
        adminChannel.enableVibration(true)
        notificationManager?.createNotificationChannel(adminChannel)
    }
}
