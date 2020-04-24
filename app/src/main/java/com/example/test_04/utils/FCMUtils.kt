package com.example.test_04.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.test_04.models.CurrentCustomer
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONException
import org.json.JSONObject

class FCMUtils {

    companion object {

        private val FCM_API = "https://fcm.googleapis.com/fcm/send"
        private val serverKey =
                "key=" + "AAAAYSqjUZ4:APA91bH21S6j90aj6IGcI2j1H67JcVe9sv22KgUE36voQv7hgl6M5j8kbugu1g63l5q2DJBoD5W0SIJTiGtWPFFurGTD9RuZ29hnHZDLnl2cBVnyaEiBo5_awmBMv_qilhcCj_KrtG3Q"
        private val contentType = "application/json"

        fun sendMessage(context: Context, review: Boolean, receiverMerchant: Boolean, title: String, message: String, receiverNameOrEmail: String, senderNameOrEmail: String) {

            FirebaseMessaging.getInstance().subscribeToTopic("/topics/Enter_your_topic_name")
            val topic = "/topics/Enter_your_topic_name" //topic has to match what the receiver subscribed to

            val notification = JSONObject()
            val notificationBody = JSONObject()

            try {
                notificationBody.put("title", title)
                notificationBody.put("message", message)   //Enter your notification message
                notificationBody.put("Receiver Name Or Email", receiverNameOrEmail)
                notificationBody.put("Sender Name Or Email", senderNameOrEmail)
                notificationBody.put("Review", review)
                notificationBody.put("Merchant", receiverMerchant)
                notification.put("to", topic)
                notification.put("data", notificationBody)
                Log.e("TAG", "try")
            } catch (e: JSONException) {
                Log.e("TAG", "onCreate: " + e.message)
            }

            sendNotification(context, notification)
        }

        private fun sendNotification(context: Context, notification: JSONObject) {
            Log.e("TAG", "sendNotification")
            val jsonObjectRequest = object : JsonObjectRequest(FCM_API, notification,
                    Response.Listener<JSONObject> { response ->
                        Log.e("TAG", response.toString())
                    },
                    Response.ErrorListener {
                        Toast.makeText(context, "Request error", Toast.LENGTH_LONG).show()
                    }) {

                override fun getHeaders(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params["Authorization"] = serverKey
                    params["Content-Type"] = contentType
                    return params
                }
            }

            val requestQueue: RequestQueue = Volley.newRequestQueue(context.applicationContext)

            requestQueue.add(jsonObjectRequest)
        }
    }


}