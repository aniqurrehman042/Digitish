package com.example.test_04.db_callbacks

import com.example.test_04.models.Customer

interface GetCustomerCallback {

    fun onCallback(customer: Customer)

}