package com.example.tmppp_library_management.state;

import com.example.tmppp_library_management.services.StockService;

public class BorrowedState implements AvailabilityState {
    private StockService stockService = StockService.getInstance();

    @Override
    public String attemptBorrow(String isbn) {
        return "ERROR: Toate cartile sunt imprumutate";
    }

    @Override
    public String attemptReserve(String isbn) {
        return "ERROR: Nu se poate rezerva - toate sunt imprumutate";
    }

    @Override
    public String attemptReturn(String isbn) {
        return "SUCCESS";
    }

    @Override
    public String getStatusMessage() {
        return "IMPRUMUTATA - Asteapta returnare";
    }

    @Override
    public String getStatusName() {
        return "Imprumutata";
    }
}