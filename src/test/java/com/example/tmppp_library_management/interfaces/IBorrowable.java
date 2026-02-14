package com.example.tmppp_library_management.interfaces;

public interface IBorrowable {
    void borrowItem(int userId);

    boolean isBorrowed();

    void returnItem();
}
