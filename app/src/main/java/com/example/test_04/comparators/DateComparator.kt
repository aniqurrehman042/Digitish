package com.example.test_04.comparators

import com.example.test_04.utils.DateUtils

open class DateComparator : Comparable<DateComparator> {

    var date: String = ""
    var type: String = ""

    override fun compareTo(other: DateComparator): Int {
        return DateUtils.stringToDateWithTime(date).compareTo(DateUtils.stringToDateWithTime(other.date))
    }

}