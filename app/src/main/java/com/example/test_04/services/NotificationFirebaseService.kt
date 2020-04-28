package com.example.test_04.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.test_04.R
import com.example.test_04.db_callbacks.IIsOldCustomer
import com.example.test_04.models.ProductReview
import com.example.test_04.ui.CustomerHome
import com.example.test_04.ui.MerchantHome
import com.example.test_04.utils.DBUtils
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
        val review: Boolean = p0.data["Review"]!!.toBoolean()
        val title: String = p0.data["title"].toString()
        val message: String = p0.data["message"].toString()

        val customerEmail = p0.data["Customer Email"]

        val isProductReview = customerEmail != null && customerEmail.isNotEmpty()

        var intent = Intent(this, CustomerHome::class.java)

        if (receiverMerchant)
            intent = Intent(this, MerchantHome::class.java)

        intent.putExtra("Review", review)

        if (isProductReview) {
            val customerName = p0.data["Customer Name"]
            val merchantName = p0.data["Merchant Name"]
            val productCode = p0.data["Product Code"]
            val productName = p0.data["Product Name"]
            val productCategory = p0.data["Product Category"]
            val productRating = p0.data["Product Rating"]!!.toInt()
            val reviewTitle = p0.data["Review Title"]
            val reviewDescription = p0.data["Review Description"]
            val qrId = p0.data["QR Id"]
            val reviewed = p0.data["Reviewed"]!!.toBoolean()
            val completed = p0.data["Completed"]!!.toBoolean()
            val id = p0.data["Id"]
            val date = p0.data["Date"]

            val productReview = ProductReview(customerEmail, customerName, merchantName, productCode, productName, productCategory, productRating, reviewDescription, reviewTitle, qrId, completed, reviewed, date)
            productReview.id = id
            intent.putExtra("Product Review", productReview)
        }

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
            val myCustomerEmail = PreferencesUtils.getCustomerDetail(this, "Email")
            receiverNameOrEmail == myCustomerEmail
        }

        if (isMe) {
            sendNotification(largeIcon, title, message, pendingIntent, notificationManager, notificationID)
        }

        when (receiverNameOrEmail) {
            "General Audience" -> {
                sendNotification(largeIcon, title, message, pendingIntent, notificationManager, notificationID)
            }

            "Old Customers" -> {
                val myCustomerEmail = PreferencesUtils.getCustomerDetail(this, "Email")
                if (myCustomerEmail != null && myCustomerEmail.isNotEmpty()) {
                    DBUtils.isOldCustomer(myCustomerEmail, senderNameOrEmail, object : IIsOldCustomer {
                        override fun onCallback(successful: Boolean, oldCustomer: Boolean) {
                            if (successful && oldCustomer) {
                                sendNotification(largeIcon, title, message, pendingIntent, notificationManager, notificationID)
                            }
                        }
                    })
                }
            }
        }
    }

    private fun sendNotification(largeIcon: Bitmap, title: String, message: String, pendingIntent: PendingIntent, notificationManager: NotificationManager, notificationID: Int) {
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
