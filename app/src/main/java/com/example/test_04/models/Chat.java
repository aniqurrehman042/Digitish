package com.example.test_04.models;

import android.graphics.Bitmap;

import com.example.test_04.utils.DateUtils;

import java.io.Serializable;
import java.text.SimpleDateFormat;

public class Chat implements Serializable, Comparable<Chat> {

    private String customerEmail;
    private String customerName;
    private String merchantEmail;
    private String merchantName;
    private String image;
    private String message;
    private String sender;
    private String date;
    private boolean sent = true;
    private boolean read;
    private transient Bitmap bitmap = null;

    public Chat(String customerEmail, String customerName, String merchantEmail, String merchantName, String image, String message, String sender, boolean read, String date) {
        this.customerEmail = customerEmail;
        this.customerName = customerName;
        this.merchantEmail = merchantEmail;
        this.merchantName = merchantName;
        this.image = image;
        this.message = message;
        this.sender = sender;
        this.read = read;
        this.date = date;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getMerchantEmail() {
        return merchantEmail;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public String getImage() {
        return image;
    }

    public String getMessage() {
        return message;
    }

    public String getSender() {
        return sender;
    }

    public String getDate() {
        return date;
    }

    @Override
    public int compareTo(Chat o) {
        return DateUtils.stringToDateWithTime(getDate()).compareTo(DateUtils.stringToDateWithTime(o.getDate()));
    }
}
