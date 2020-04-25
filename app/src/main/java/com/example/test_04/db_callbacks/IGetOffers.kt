package com.example.test_04.db_callbacks

import com.example.test_04.models.Offer

interface IGetOffers {
    fun onCallback(successful: Boolean, offers: ArrayList<Offer>)
}