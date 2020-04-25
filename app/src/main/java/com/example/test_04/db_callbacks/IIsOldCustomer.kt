package com.example.test_04.db_callbacks

interface IIsOldCustomer {
    fun onCallback(successful: Boolean, oldCustomer: Boolean)
}