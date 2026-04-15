package com.example.tmppp_library_management.entity;

public class Stock {
    private int stockId;
    private String itemId;
    private int quantity;
    private int availableQuantity;
    private int reservedQuantity;

    public Stock(int stockId, String itemId, int initialQuantity) {
        this.stockId = stockId;
        this.itemId = itemId;
        this.quantity = initialQuantity;
        this.availableQuantity = initialQuantity;
        this.reservedQuantity = 0;
    }

    public int getStockId() { return stockId; }
    public void setStockId(int stockId) { this.stockId = stockId; }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public int getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(int reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }
}
