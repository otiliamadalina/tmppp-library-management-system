package com.example.tmppp_library_management.entity;

import com.example.tmppp_library_management.abstractClasses.LibraryItem;
import com.example.tmppp_library_management.abstractClasses.User;
import com.example.tmppp_library_management.interfaces.IBorrowable;

import java.time.LocalDate;

public class Loan {
    private LibraryItem item;
    private IBorrowable borrowableItem;
    private User user;
    private LocalDate loanDate;
    private LocalDate returnDate;

    public Loan(LibraryItem item, IBorrowable borrowableItem, User user, LocalDate loanDate) {
        this.item = item;
        this.borrowableItem = borrowableItem;
        this.user = user;
        this.loanDate = loanDate;
    }

    public IBorrowable getBorrowableItem() {
        return borrowableItem;
    }

    public String getItemTitle() {
        return item.getTitle();
    }

    public User getUser() {
        return user;
    }

    public LocalDate getLoanDate() {
        return loanDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public boolean isActive() {
        return returnDate == null;
    }

    public void closeLoan(LocalDate returnDate) {
        this.returnDate = returnDate;
        borrowableItem.returnItem();
    }
}
