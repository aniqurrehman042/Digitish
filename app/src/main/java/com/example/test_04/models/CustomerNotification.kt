package com.example.test_04.models

import com.example.test_04.comparators.DateComparator
import java.io.Serializable

class CustomerNotification(var customerName: String, var title: String, var message: String, type: String, date: String) : DateComparator(), Serializable {

    init {
        super.type = type
        super.date = date
    }

}