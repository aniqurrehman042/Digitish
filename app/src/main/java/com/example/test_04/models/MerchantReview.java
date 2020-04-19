package com.example.test_04.models;

public class MerchantReview {

    private String customerEmail;
    private String customerNAme;
    private String merchantName;
    private int responsiveness;
    private int afterSaleService;
    private int salesAgentSupport;
    private int productInfo;
    private int valueForPrice;
    private String qrId;
    private String date;

    public MerchantReview(String customerEmail, String customerNAme, String merchantName, int responsiveness, int afterSaleService, int salesAgentSupport, int productInfo, int valueForPrice, String qrId) {
        this.customerEmail = customerEmail;
        this.customerNAme = customerNAme;
        this.merchantName = merchantName;
        this.responsiveness = responsiveness;
        this.afterSaleService = afterSaleService;
        this.salesAgentSupport = salesAgentSupport;
        this.productInfo = productInfo;
        this.valueForPrice = valueForPrice;
        this.qrId = qrId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getCustomerNAme() {
        return customerNAme;
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
