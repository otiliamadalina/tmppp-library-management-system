package com.example.tmppp_library_management.state;

public interface AvailabilityState {
    String attemptBorrow(String isbn);
    String attemptReserve(String isbn);
    String attemptReturn(String isbn);
    String getStatusMessage();
    String getStatusName();
}