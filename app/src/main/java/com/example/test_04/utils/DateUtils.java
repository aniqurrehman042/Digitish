package com.example.test_04.utils;

import android.content.Context;
import android.text.format.DateFormat;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    public static Date stringToDateWithTime(String dateString) {
        Date date = null;
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy - hh:mm a", Locale.US);
        try {
            date = format.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }

    public static Date stringToDate(String dateString) {
        Date date = null;
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy", Locale.US);
        try {
            date = format.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }

    public static String dateToStringWithTime(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy - hh:mm a", Locale.US);
        return format.format(date);
    }

    public static String dateToString(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy", Locale.US);
        return format.format(date);
    }

    public static String getCurrentDateInString() {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy - hh:mm a", Locale.US);
        return format.format(date);
    }

    public static boolean isSameDay(Date firstDate, Date secondDate) {

        int firstDay = Integer.parseInt((String) DateFormat.format("dd", firstDate));
        int firstMonth = Integer.parseInt((String) DateFormat.format("MM", firstDate));
        int firstYear = Integer.parseInt((String) DateFormat.format("yyyy", firstDate));
        int secondDay = Integer.parseInt((String) DateFormat.format("dd", secondDate));
        int secondMonth = Integer.parseInt((String) DateFormat.format("MM", secondDate));
        int secondYear = Integer.parseInt((String) DateFormat.format("yyyy", secondDate));

        if (firstYear == secondYear && firstMonth == secondMonth && firstDay == secondDay)
            return true;
        else
            return false;
    }

}
