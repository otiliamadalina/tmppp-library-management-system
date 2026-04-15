package com.example.tmppp_library_management.observer;

public interface BookSubject {
    void attach(BookObserver observer);
    void detach(BookObserver observer);
    void notifyObservers(BookEvent event);
}