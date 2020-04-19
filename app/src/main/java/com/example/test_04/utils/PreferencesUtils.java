package com.example.test_04.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.test_04.models.CurrentCustomer;

import static android.content.Context.MODE_PRIVATE;

public class PreferencesUtils {

    private PreferencesUtils(){ }

    public static void saveCustomerPoints(Context context, boolean merchantRated) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Customer", MODE_PRIVATE);
        int points = Integer.valueOf(CurrentCustomer.points);
        if (merchantRated)
            points += 20;
        else
            points += 10;

        String stringPoints= String.valueOf(points);
        CurrentCustomer.points = stringPoints;
        SharedPreferences.Editor sp = sharedPreferences.edit();
        sp.putString("Points", stringPoints);
        sp.apply();
    }

    public static void saveCustomerAttribute(Context context, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Customer", MODE_PRIVATE);
        sharedPreferences.edit().putString(key, value).apply();
    }

    public static String getMerchantDetail(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences("Merchant", MODE_PRIVATE);
        return sp.getString(key, null);
    }

    public static String getCustomerDetail(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences("Customer", MODE_PRIVATE);
        return sp.getString(key, null);
    }

}
