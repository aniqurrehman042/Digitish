package com.example.test_04.models;

import java.io.Serializable;

public class Merchant implements Serializable {

    private String name;
    private String description;
    private String email;
    private String rating;
    private String products;
    private String website;

    public Merchant(String name, String description, String email, String rating, String products, String website) {
        this.name = name;
        this.description = description;
        this.email = email;
        this.rating = rating;
        this.products = products;
        this.website = website;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getProducts() {
        return products;
    }

    public void setProducts(String products) {
        this.products = products;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
}
