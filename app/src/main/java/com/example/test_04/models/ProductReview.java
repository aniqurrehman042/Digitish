package com.example.test_04.models;

import com.example.test_04.comparators.DateComparator;

import java.io.Serializable;

public class ProductReview extends DateComparator implements Serializable {

    private String customerEmail;
    private String customerName;
    private String merchantName;
    private String productCode;
    private String productName;
    private String productCategory;
    private int productRating;
    private String reviewDescription;
    private String reviewTitle;
    private String qrId;
    private boolean completed;
    private String id;
    private boolean reviewed;

    public ProductReview(String customerEmail, String customerName, String merchantName, String productCode, String productName, String productCategory, int productRating, String reviewDescription, String reviewTitle, String qrId, boolean completed, boolean reviewed, String date) {
        this.customerEmail = customerEmail;
        this.customerName = customerName;
        this.merchantName = merchantName;
        this.productCode = productCode;
        this.productName = productName;
        this.productRating = productRating;
        this.reviewDescription = reviewDescription;
        this.reviewTitle = reviewTitle;
        this.qrId = qrId;
        this.productCategory = productCategory;
        this.completed = completed;
        this.reviewed = reviewed;
        setDate(date);
        setType("Product Review");
    }

    public void setReviewed(boolean reviewed) {
        this.reviewed = reviewed;
    }

    public boolean isReviewed() {
        return reviewed;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isCompleted() {
        return completed;
    }

    public String getQrId() {
        return qrId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getProductName() {
        return productName;
    }

    public int getProductRating() {
        return productRating;
    }

    public String getReviewDescription() {
        return reviewDescription;
    }

    public String getReviewTitle() {
        return reviewTitle;
    }
}
