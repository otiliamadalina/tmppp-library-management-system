package com.example.tmppp_library_management.state;

public class LostState implements AvailabilityState {

    @Override
    public String attemptBorrow(String isbn) {
        return "ERROR: Carte pierduta - nu se poate imprumuta";
    }

    @Override
    public String attemptReserve(String isbn) {
        return "ERROR: Carte pierduta - nu se poate rezerva";
    }

    @Override
    public String attemptReturn(String isbn) {
        return "ERROR: Carte pierduta - nu se poate returna";
    }

    @Override
    public String getStatusMessage() {
        return "LOST - Necesita inlocuire";
    }

    @Override
    public String getStatusName() {
        return "Pierduta";
    }
}