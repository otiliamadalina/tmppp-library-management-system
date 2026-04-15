package com.example.tmppp_library_management.builder;

import com.example.tmppp_library_management.abstractClasses.Accessory;

public class Ribbon extends Accessory {
    private String color;

    public Ribbon(String id, String name, String material, String stockCode,
                  double price, String color) {
        super(id, name, material, stockCode, price);
        this.color = color;
    }

    public String getColor() { return color; }
}