package com.example.tmppp_library_management.builder;

public class Packaging {
    private String type;
    private String color;
    private double price;

    public Packaging(String type, String color, double price) {
        this.type = type;
        this.color = color;
        this.price = price;
    }

    public String getColor() { return color; }
    public String getType() { return type; }
    public double getPrice() { return price; }
}
