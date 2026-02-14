package com.example.tmppp_library_management;

import com.example.tmppp_library_management.abstractClasses.BorrowableItem;
import com.example.tmppp_library_management.abstractClasses.User;
import com.example.tmppp_library_management.entity.*;
import com.example.tmppp_library_management.service.LoanService;

public class Main {
    public static void main(String[] args) {
        User user = new User(1, "Alice", "alice@gmail.com");

        BorrowableItem book = new Book(101, "Clean Code", 2008, 3, "Robert C. Martin", "Prentice Hall", "978-0132350884");

        Newspaper newspaper = new Newspaper(201, "The Times", 2026, 5, "The Times Publisher");

        System.out.println(newspaper.getDescription() +
                ", Stock: " + newspaper.getStock().getQuantity());

        System.out.println(book.getDescription() +
                ", Stock: " + book.getStock().getQuantity());

        LoanService loanService = new LoanService();

        if (!book.isBorrowed()) {
            Loan loan = loanService.createLoan(user, book);
            System.out.println("Loan created for book: " + loan.getItemTitle() +
                    " to user " + loan.getUser().getUserName());
        }

        Loan activeLoan = loanService.getActiveLoans().get(0);
        loanService.returnItem(activeLoan);
        System.out.println("Book returned: " + activeLoan.getItemTitle());
    }
}
