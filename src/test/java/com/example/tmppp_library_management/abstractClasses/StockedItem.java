package com.example.tmppp_library_management.abstractClasses;

import com.example.tmppp_library_management.entity.Stock;
import com.example.tmppp_library_management.interfaces.IStockable;

public abstract class StockedItem extends LibraryItem implements IStockable {
    protected Stock stock;

    public StockedItem(int itemId, String title, int publicationYear, int quantity) {
        super(itemId, title, publicationYear);
        this.stock = new Stock(quantity);
    }

    @Override
    public Stock getStock() {
        return stock;
    }
}

