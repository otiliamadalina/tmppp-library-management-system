package com.example.tmppp_library_management.abstractClasses;

public abstract class StockedItem extends LibraryItem {
    protected int quantity;
    protected int availableQuantity;
    protected int reservedQuantity;

    public StockedItem(int itemId, String title, int publicationDate, int pageCount) {
        super(itemId, title, publicationDate, pageCount);
        this.quantity = 0;
        this.availableQuantity = 0;
        this.reservedQuantity = 0;
    }

    public boolean reserve(int quantity) {
        if (availableQuantity >= quantity) {
            availableQuantity -= quantity;
            reservedQuantity += quantity;
            return true;
        }
        return false;
    }

    public void release(int quantity) {
        if (reservedQuantity >= quantity) {
            availableQuantity += quantity;
            reservedQuantity -= quantity;
        }
    }

    public void updateStock(int change) {
        this.quantity += change;
        this.availableQuantity += change;
    }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }
    public int getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(int reservedQuantity) { this.reservedQuantity = reservedQuantity; }
}