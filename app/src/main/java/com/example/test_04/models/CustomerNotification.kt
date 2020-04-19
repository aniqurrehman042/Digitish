package com.example.test_04.models

import java.io.Serializable

class CustomerNotification(var customerEmail: String, var merchantName: String, var title: String, var message: String, var type: String, var date: String, var amount: String) : Serializable {
}