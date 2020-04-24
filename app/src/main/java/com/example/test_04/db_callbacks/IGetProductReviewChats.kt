package com.example.test_04.db_callbacks

import com.example.test_04.models.MerchantReview
import com.example.test_04.models.ProductReviewChat

interface IGetProductReviewChats {
    fun onCallback(successful: Boolean, productReviewChats: ArrayList<ProductReviewChat>)
}