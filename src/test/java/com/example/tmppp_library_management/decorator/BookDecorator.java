package com.example.tmppp_library_management.decorator;

import com.example.tmppp_library_management.book.Book;
import com.example.tmppp_library_management.interfaces.IBorrowable;

public abstract class BookDecorator implements IBorrowable {
    protected Book book;

    public BookDecorator(Book book) {
        this.book = book;
    }

    @Override
    public void borrowItem() {
        book.borrowItem();
    }

    @Override
    public boolean isAvailable() {
        return book.isAvailable();
    }

    @Override
    public void returnItem() {
        book.returnItem();
    }

    @Override
    public int getBorrowedCount() {
        return book.getBorrowedCount();
    }

    public abstract String getDescription();

    public Book getOriginalBook() {
        return book;
    }
}