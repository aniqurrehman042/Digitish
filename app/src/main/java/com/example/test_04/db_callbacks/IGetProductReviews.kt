package com.example.test_04.db_callbacks

import com.example.test_04.models.ProductReview

interface IGetProductReviews {

    fun onCallback(successful: Boolean, productReviews: ArrayList<ProductReview>)

}