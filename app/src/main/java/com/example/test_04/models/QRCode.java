package com.example.test_04.models;

import java.util.ArrayList;

public class QRCode {

    private String id;
    private ArrayList<String> productCodes;
    private String customerEmail;

    public QRCode(String id, ArrayList<String> productCodes, String customerEmail) {
        this.id = id;
        this.productCodes = productCodes;
        this.customerEmail = customerEmail;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getId() {
        return id;
    }

    public ArrayList<String> getProductCodes() {
        return productCodes;
    }
}
