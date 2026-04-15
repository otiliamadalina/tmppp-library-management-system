package com.example.tmppp_library_management.observer;

import com.example.tmppp_library_management.book.Book;
import java.time.LocalDateTime;

public class BookEvent {
    private final EventType type;
    private final Book book;
    private final LocalDateTime timestamp;

    public BookEvent(EventType type, Book book) {
        this.type = type;
        this.book = book;
        this.timestamp = LocalDateTime.now();
    }

    public EventType getType() { return type; }
    public Book getBook() { return book; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public String getMessage() {
        switch (type) {
            case BOOK_ADDED: return "Carte adaugata: " + book.getTitle();
            case BOOK_REMOVED: return "Carte stearsa: " + book.getTitle();
            case BOOK_UPDATED: return "Carte actualizata: " + book.getTitle();
            case BOOK_RESTRICTION_CHANGED: return "Restrictii schimbate pentru: " + book.getTitle();
            default: return "Eveniment carte";
        }
    }
}