package com.example.tmppp_library_management.observer;

import com.example.tmppp_library_management.book.Book;

public interface BookObserver {
    void update(BookEvent event);
}