package com.example.tmppp_library_management.state;

import com.example.tmppp_library_management.services.StockService;

public class ReservedState implements AvailabilityState {
    private StockService stockService = StockService.getInstance();

    @Override
    public String attemptBorrow(String isbn) {
        if (stockService.getStock(isbn) != null &&
                stockService.getStock(isbn).getReservedQuantity() > 0) {
            return "SUCCESS_CONVERT";
        }
        return "ERROR: Nicio rezervare activa";
    }

    @Override
    public String attemptReserve(String isbn) {
        return "ERROR: Carte deja rezervata";
    }

    @Override
    public String attemptReturn(String isbn) {
        return "SUCCESS";
    }

    @Override
    public String getStatusMessage() {
        return "REZERVATA - Asteapta ridicare";
    }

    @Override
    public String getStatusName() {
        return "Rezervata";
    }
}