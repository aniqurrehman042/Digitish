package com.example.test_04.models

import android.graphics.Bitmap
import com.example.test_04.comparators.DateComparator
import java.io.Serializable

class ProductReviewChat(var customerEmail: String, var customerName: String, var merchantName: String, var productCode: String, var qrId: String, var image: String, var message: String, var sender: String, date: String, var customerProfilePic: String) : DateComparator(), Serializable{

    var sent: Boolean = true

    var bitmap: Bitmap? = null

    var productName: String? = null

    init {
        super.date = date
        super.type = "Product Review Chat"
    }

}