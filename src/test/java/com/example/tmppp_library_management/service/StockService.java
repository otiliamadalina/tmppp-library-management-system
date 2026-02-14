package com.example.tmppp_library_management.service;

import com.example.tmppp_library_management.entity.Stock;
import com.example.tmppp_library_management.interfaces.IStockable;

public class StockService {
    public void updateStock(IStockable item, int quantityChange) {
        Stock stock = item.getStock();
        int newQuantity = stock.getQuantity() + quantityChange;
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        stock.setQuantity(newQuantity);
    }
}

