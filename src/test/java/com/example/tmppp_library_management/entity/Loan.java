package com.example.tmppp_library_management.entity;

import com.example.tmppp_library_management.book.Book;
import com.example.tmppp_library_management.bridge.Penalty;
import com.example.tmppp_library_management.decorator.BookDecorator;
import com.example.tmppp_library_management.interfaces.IBorrowable;
import com.example.tmppp_library_management.user.Member;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Loan {
    private int loanId;
    private Member user;
    private IBorrowable item;
    private LocalDate startDate;
    private LocalDate returnDate;
    private boolean isActive;
    private Penalty penalty;

    public Loan() {
        this.isActive = true;
    }

    public void close() {
        this.isActive = false;
        if (user != null) {
            user.setCurrentLoans(user.getCurrentLoans() - 1);
        }
    }

    public int getLoanId() { return loanId; }
    public void setLoanId(int loanId) { this.loanId = loanId; }

    public Member getUser() { return user; }
    public void setUser(Member user) { this.user = user; }

    public IBorrowable getItem() { return item; }
    public void setItem(IBorrowable item) { this.item = item; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Book getBook() {
        if (item instanceof Book) {
            return (Book) item;
        } else if (item instanceof BookDecorator) {
            return ((BookDecorator) item).getOriginalBook();
        }
        return null;
    }

    public String getBookTitle() {
        Book book = getBook();
        return book != null ? book.getTitle() : "Carte necunoscuta";
    }

    public void setPenalty(Penalty penalty) {
        this.penalty = penalty;
    }

    public Penalty getPenalty() {
        return penalty;
    }

    public double calculatePenalty() {
        if (penalty == null) return 0;

        int daysLate = 0;
        if (LocalDate.now().isAfter(returnDate)) {
            daysLate = (int) java.time.temporal.ChronoUnit.DAYS.between(returnDate, LocalDate.now());
        }

        double bookPrice = 0;
        if (item instanceof Book) {
            bookPrice = ((Book) item).getPrice();
        } else if (item instanceof BookDecorator) {
            bookPrice = ((BookDecorator) item).getOriginalBook().getPrice();
        }

        return penalty.getAmount(daysLate, bookPrice);
    }

    public String getPenaltyType() {
        return penalty != null ? penalty.getType() : "Nicio penalitate";
    }

    public String getPenaltyDescription() {
        return penalty != null ? penalty.getDescription() : "Fara penalitate";
    }
}