package com.example.test_04.db_callbacks

import com.example.test_04.models.ProductReview

interface IGetProductReview {
    fun onCallback(successful: Boolean, productReview: ProductReview)
}