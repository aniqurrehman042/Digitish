package com.example.test_04.utils;

public class StringUtils {

    private StringUtils() {}


    public static String addLineBreaks(String singleLine) {
        while (singleLine.contains("\\n"))
            singleLine = singleLine.replace("\\n", "\n");

        return singleLine;
    }
}
