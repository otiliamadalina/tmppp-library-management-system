package com.example.tmppp_library_management.builder;

public class GreetingCard {
    private String message;
    private double price;
    private boolean isCustom;

    public GreetingCard(String message, double price, boolean isCustom) {
        this.message = message;
        this.price = price;
        this.isCustom = isCustom;
    }

    public String getMessage() { return message; }
    public double getPrice() { return price; }
    public boolean isCustom() { return isCustom; }
}