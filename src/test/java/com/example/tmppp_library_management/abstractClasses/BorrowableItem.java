package com.example.tmppp_library_management.abstractClasses;

import com.example.tmppp_library_management.interfaces.IBorrowable;
import com.example.tmppp_library_management.interfaces.IStockable;

public abstract class BorrowableItem extends StockedItem implements IBorrowable {
    protected boolean borrowed = false;
    protected int borrowedByUserId = -1;

    public BorrowableItem(int itemId, String title, int publicationYear, int quantity) {
        super(itemId, title, publicationYear, quantity);
    }

    @Override
    public void borrowItem(int userId) {
        if (!borrowed && stock.getQuantity() > 0) {
            borrowed = true;
            borrowedByUserId = userId;
            stock.setQuantity(stock.getQuantity() - 1);
        } else {
            throw new IllegalStateException("Item not available to borrow");
        }
    }

    @Override
    public boolean isBorrowed() {
        return borrowed;
    }

    @Override
    public void returnItem() {
        if (borrowed) {
            borrowed = false;
            borrowedByUserId = -1;
            stock.setQuantity(stock.getQuantity() + 1);
        }
    }
}


