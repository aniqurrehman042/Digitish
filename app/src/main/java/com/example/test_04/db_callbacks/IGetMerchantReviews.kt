package com.example.test_04.db_callbacks

import com.example.test_04.models.MerchantReview

interface IGetMerchantReviews {
    fun onCallback(successful: Boolean, merchantReviews: ArrayList<MerchantReview>)
}