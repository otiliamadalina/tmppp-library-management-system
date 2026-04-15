package com.example.tmppp_library_management.interfaces;

public interface IBorrowable {
    void borrowItem(); ;
    boolean isAvailable();
    void returnItem();
    int getBorrowedCount();
}
