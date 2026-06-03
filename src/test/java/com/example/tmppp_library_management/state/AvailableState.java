package com.example.tmppp_library_management.state;

import com.example.tmppp_library_management.services.StockService;

public class AvailableState implements AvailabilityState {
    private StockService stockService = StockService.getInstance();

    @Override
    public String attemptBorrow(String isbn) {
        if (stockService.checkAvailability(isbn, 1)) {
            return "SUCCESS";
        }
        return "ERROR: Stoc insuficient";
    }

    @Override
    public String attemptReserve(String isbn) {
        if (stockService.checkAvailability(isbn, 1)) {
            return "SUCCESS";
        }
        return "ERROR: Nu se poate rezerva";
    }

    @Override
    public String attemptReturn(String isbn) {
        return "ERROR: Nicio carte de returnat";
    }

    @Override
    public String getStatusMessage() {
        return "DISPONIBILA - Poate fi imprumutata";
    }

    @Override
    public String getStatusName() {
        return "Disponibila";
    }
}