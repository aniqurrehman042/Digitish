package com.example.test_04.models;

import com.example.test_04.comparators.DateComparator;

public class MerchantReview extends DateComparator {

    private String customerEmail;
    private String customerName;
    private String merchantName;
    private int responsiveness;
    private int afterSaleService;
    private int salesAgentSupport;
    private int productInfo;
    private int valueForPrice;
    private String qrId;

    public MerchantReview(String customerEmail, String customerName, String merchantName, int responsiveness, int afterSaleService, int salesAgentSupport, int productInfo, int valueForPrice, String qrId, String date) {
        this.customerEmail = customerEmail;
        this.customerName = customerName;
        this.merchantName = merchantName;
        this.responsiveness = responsiveness;
        this.afterSaleService = afterSaleService;
        this.salesAgentSupport = salesAgentSupport;
        this.productInfo = productInfo;
        this.valueForPrice = valueForPrice;
        this.qrId = qrId;

        setDate(date);
        setType("Merchant Review");
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

    public int getResponsiveness() {
        return responsiveness;
    }

    public int getAfterSaleService() {
        return afterSaleService;
    }

    public int getSalesAgentSupport() {
        return salesAgentSupport;
    }

    public int getProductInfo() {
        return productInfo;
    }

    public int getValueForPrice() {
        return valueForPrice;
    }

    public String getQrId() {
        return qrId;
    }
}
