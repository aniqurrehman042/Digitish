package com.example.test_04.models

import android.graphics.Bitmap
import java.io.Serializable

class ProductReviewChat(var customerEmail: String, var customerName: String, var merchantName: String, var image: String, var message: String, var sender: String, var date: String, var customerProfilePic: String) : Serializable{

    var sent: Boolean = true

    var bitmap: Bitmap? = null

}