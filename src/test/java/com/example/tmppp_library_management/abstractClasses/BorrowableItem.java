package com.example.tmppp_library_management.abstractClasses;

import com.example.tmppp_library_management.interfaces.IBorrowable;

public abstract class BorrowableItem extends LibraryItem implements IBorrowable {
    protected boolean borrowed;
    protected int borrowerId;
    protected int borrowedCount;

    public BorrowableItem(int itemId, String title, int publicationDate, int pageCount) {
        super(itemId, title, publicationDate, pageCount);
        this.borrowed = false;
        this.borrowerId = -1;
        this.borrowedCount = 0;
    }

    @Override
    public void borrowItem() {
        if (!borrowed) {
            this.borrowed = true;
            this.borrowedCount++;
        }
    }

    @Override
    public void returnItem() {
        this.borrowed = false;
        this.borrowerId = -1;
    }

    @Override
    public boolean isAvailable() {
        return !borrowed;
    }

    @Override
    public int getBorrowedCount() {
        return borrowedCount;
    }

    public int getBorrowerId() {
        return borrowerId;
    }

    public void setBorrowerId(int borrowerId) {
        this.borrowerId = borrowerId;
    }
}