package com.example.tmppp_library_management.builder;

import com.example.tmppp_library_management.abstractClasses.Accessory;

public class GiftTag extends Accessory {
    private String text;

    public GiftTag(String id, String name, String material, String stockCode,
                   double price, String text) {
        super(id, name, material, stockCode, price);
        this.text = text;
    }

    public String getText() { return text; }
}