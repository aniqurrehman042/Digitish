package com.example.test_04.comparators

open class DateComparator : Comparable<DateComparator> {

    var date: String = ""
    var type: String = ""

    override fun compareTo(other: DateComparator): Int {
        return date.compareTo(other.date)
    }

}