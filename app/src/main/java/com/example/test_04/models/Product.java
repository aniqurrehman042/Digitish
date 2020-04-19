package com.example.test_04.models;

import java.io.Serializable;

public class Product implements Serializable {

    private String productCode;
    private String productName;
    private String productCategory;
    private String merchantName;
    private boolean reviewed = false;

    public Product(String productCode, String productName, String productCategory, String merchantName) {
        this.productCode = productCode;
        this.productName = productName;
        this.productCategory = productCategory;
        this.merchantName = merchantName;
    }

    public boolean isReviewed() {
        return reviewed;
    }

    public void setReviewed(boolean reviewed) {
        this.reviewed = reviewed;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public String getMerchantName() {
        return merchantName;
    }
}
