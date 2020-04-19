package com.example.test_04.utils;

import com.example.test_04.models.CurrentCustomer;
import com.example.test_04.models.QRCode;

import java.util.ArrayList;
import java.util.Arrays;

public class QRUtils {

    private QRUtils() {
    }

    public static boolean hasEmail(String qrCode) {
        if (!qrCode.contains("@"))
            return false;
        else
            return true;
    }

    public static String extractCustomerEmail(String qrCode) {

        int dotIndex = qrCode.indexOf(".com");
        char[] charArray = new char[dotIndex + 4];

        for (int i = 0; i < charArray.length; i++) {
            charArray[i] = qrCode.charAt(i);
        }

        String customerEmail = String.valueOf(charArray);

        return customerEmail;
    }

    public static String getId(String qrCode) {

        int lastIndex = qrCode.indexOf("/") - 1;
        char[] charArray = new char[lastIndex + 1];

        for (int i = 0; i < charArray.length; i++) {
            charArray[i] = qrCode.charAt(i);
        }

        String qrId = String.valueOf(charArray);

        return qrId;
    }

    public static QRCode getQr(String qrCodeString) {

        QRCode qrCode = null;

        int index = qrCodeString.indexOf("//") + 2;
        String id = QRUtils.getId(qrCodeString);
        ArrayList<String> productCodes = new ArrayList<>(Arrays.asList(qrCodeString.substring(index).split(",")));
        qrCode = new QRCode(id, productCodes, CurrentCustomer.email);

        return qrCode;
    }

    public static ArrayList<String> extractProductCodes(String productCodes) {
        return new ArrayList<>(Arrays.asList(productCodes.split(",")));
    }

}
