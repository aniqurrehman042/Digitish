package com.example.test_04.db_callbacks

interface IsQRExpiredCallback {

    fun onCallback(found: Boolean, expired: Boolean, successful: Boolean, merchantName: String)

}