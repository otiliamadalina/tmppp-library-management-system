package com.example.tmppp_library_management.state;

import com.example.tmppp_library_management.book.Book;
import com.example.tmppp_library_management.entity.Stock;
import com.example.tmppp_library_management.entity.Loan;
import com.example.tmppp_library_management.services.LoanService;
import com.example.tmppp_library_management.services.StockService;
import com.example.tmppp_library_management.state.*;

public class AvailabilityService {
    private AvailabilityState state;
    private StockService stockService;

    public AvailabilityService() {
        this.stockService = StockService.getInstance();
        this.state = new AvailableState();
    }

    private void updateState(String isbn) {
        Stock stock = stockService.getStock(isbn);
        if (stock == null) {
            state = new LostState();
            return;
        }

        int available = stock.getAvailableQuantity();
        int reserved = stock.getReservedQuantity();
        int total = stock.getQuantity();
        int borrowed = total - available - reserved;

        if (available > 0) {
            state = new AvailableState();
        } else if (reserved > 0) {
            state = new ReservedState();
        } else if (borrowed > 0) {
            state = new BorrowedState();
        } else {
            state = new LostState();
        }
    }

    // actualizeaza stocul cand se creeaza un imprumut
    public void onLoanCreated(String isbn) {
        Stock stock = stockService.getStock(isbn);
        if (stock != null && stock.getAvailableQuantity() > 0) {
            stockService.decreaseStock(isbn, 1);
            System.out.println("[Availability] Stoc actualizat pentru imprumut: " + isbn +
                    " (Disponibil acum: " + stock.getAvailableQuantity() + ")");
        }
        updateState(isbn);
    }

    // ctualizeaza stocul cand se returneaza un imprumut
    public void onLoanReturned(String isbn) {
        stockService.increaseStock(isbn, 1);
        System.out.println("[Availability] Stoc actualizat pentru returnare: " + isbn);
        updateState(isbn);
    }

    // erifica daca o carte poate fi imprumutata
    public boolean canBeBorrowed(String isbn) {
        updateState(isbn);
        String result = state.attemptBorrow(isbn);
        return result.equals("SUCCESS") || result.equals("SUCCESS_CONVERT");
    }

    // obtine statusul pentru o carte (folosit in UI)
    public String getBookStatusForDisplay(String isbn) {
        updateState(isbn);
        return state.getStatusMessage();
    }

    public boolean borrow(String isbn) {
        updateState(isbn);
        String result = state.attemptBorrow(isbn);

        if (result.equals("SUCCESS") || result.equals("SUCCESS_CONVERT")) {
            if (result.equals("SUCCESS_CONVERT")) {
                stockService.releaseItem(isbn, 1);
            }
            stockService.decreaseStock(isbn, 1);
            return true;
        }
        return false;
    }

    public boolean reserve(String isbn) {
        updateState(isbn);
        String result = state.attemptReserve(isbn);

        if (result.equals("SUCCESS")) {
            stockService.reserveItem(isbn, 1);
            return true;
        }
        return false;
    }

    public boolean returnBook(String isbn) {
        updateState(isbn);
        String result = state.attemptReturn(isbn);

        if (result.equals("SUCCESS")) {
            stockService.increaseStock(isbn, 1);
            if (state instanceof ReservedState) {
                stockService.releaseItem(isbn, 1);
            }
            return true;
        }
        return false;
    }

    public String getStatusMessage(String isbn) {
        updateState(isbn);
        return state.getStatusMessage();
    }

    public String getStatusName(String isbn) {
        updateState(isbn);
        return state.getStatusName();
    }

    public AvailabilityInfo getAvailabilityInfo(String isbn) {
        Stock stock = stockService.getStock(isbn);
        if (stock == null) {
            return new AvailabilityInfo(0, 0, 0, "Necunoscuta");
        }

        int total = stock.getQuantity();
        int available = stock.getAvailableQuantity();
        int reserved = stock.getReservedQuantity();

        int borrowed = 0;
        try {
            LoanService loanService = new LoanService();
            for (Loan loan : loanService.getActiveLoans()) {
                Book book = loan.getBook();
                if (book != null && book.getIsbn().equals(isbn)) {
                    borrowed++;
                }
            }
        } catch (Exception e) {
            System.out.println("Eroare la calcularea imprumuturilor: " + e.getMessage());
        }

        return new AvailabilityInfo(available, reserved, borrowed, getStatusName(isbn));
    }

    public static class AvailabilityInfo {
        public final int available;
        public final int reserved;
        public final int borrowed;
        public final String status;

        public AvailabilityInfo(int available, int reserved, int borrowed, String status) {
            this.available = available;
            this.reserved = reserved;
            this.borrowed = borrowed;
            this.status = status;
        }
    }
}