package com.example.tmppp_library_management.service;

import com.example.tmppp_library_management.abstractClasses.LibraryItem;
import com.example.tmppp_library_management.abstractClasses.User;
import com.example.tmppp_library_management.entity.Loan;
import com.example.tmppp_library_management.interfaces.IBorrowable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LoanService {
    private List<Loan> activeLoans = new ArrayList<>();

    public Loan createLoan(User user, IBorrowable borrowableItem) {
        if (borrowableItem.isBorrowed()) {
            throw new IllegalStateException("Item already borrowed");
        }
        borrowableItem.borrowItem(user.getUserId());

        LibraryItem item = (LibraryItem) borrowableItem;
        Loan loan = new Loan(item, borrowableItem, user, LocalDate.now());
        activeLoans.add(loan);
        return loan;
    }

    public void returnItem(Loan loan) {
        loan.getBorrowableItem().returnItem();
        loan.closeLoan(LocalDate.now());
        activeLoans.remove(loan);
    }

    public List<Loan> getActiveLoans() {
        return activeLoans;
    }
}

