package com.example.tmppp_library_management.builder;

import com.example.tmppp_library_management.book.Book;

public abstract class GiftPackage {
    protected Book book;
    protected GreetingCard card;
    protected Packaging packaging;

    public GiftPackage(Book book, GreetingCard card, Packaging packaging) {
        this.book = book;
        this.card = card;
        this.packaging = packaging;
    }

    public abstract double calculateTotalPrice();

    public Book getBook() { return book; }
    public GreetingCard getCard() { return card; }
    public Packaging getPackaging() { return packaging; }
}