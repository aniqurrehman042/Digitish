package com.example.test_04.models

import com.example.test_04.comparators.DateComparator

class Offer(var offerTitle: String, var offerDesc: String, var merchantName: String, var generalAudience: Boolean, var validTill: String, date: String) : DateComparator() {

    init {
        super.date = date
        super.type = "Offer"
    }

}