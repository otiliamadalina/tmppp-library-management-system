package com.example.tmppp_library_management.abstractClasses;

import com.example.tmppp_library_management.interfaces.ISellable;

public abstract class Accessory implements ISellable {
    protected String id;
    protected String name;
    protected String material;
    protected String stockCode;
    protected double price;

    public Accessory(String id, String name, String material, String stockCode, double price) {
        this.id = id;
        this.name = name;
        this.material = material;
        this.stockCode = stockCode;
        this.price = price;
    }

    @Override
    public double getPrice() {
        return price;
    }

    @Override
    public String getStockCode() {
        return stockCode;
    }

    public String getName() {
        return name;
    }

    public String getMaterial() {
        return material;
    }
}