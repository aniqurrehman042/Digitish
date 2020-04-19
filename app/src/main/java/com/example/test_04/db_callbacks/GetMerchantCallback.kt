package com.example.test_04.db_callbacks

import com.example.test_04.models.Merchant

interface GetMerchantCallback {

    fun onCallback(merchant: Merchant?, successful: Boolean)

}