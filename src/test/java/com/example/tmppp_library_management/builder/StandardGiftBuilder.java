package com.example.tmppp_library_management.builder;

import com.example.tmppp_library_management.book.Book;

public class StandardGiftBuilder implements GiftPackageBuilder {
    private StandardGift result;
    private Book book;
    private String message;

    @Override
    public void reset() {
        this.result = null;
        this.book = null;
        this.message = null;
    }

    @Override
    public void buildBook(Book book) {
        this.book = book;
    }

    @Override
    public void buildGreetingCard(String message) {
        this.message = message;
    }

    @Override
    public void buildPackaging() {
    }

    @Override
    public GiftPackage getResult() {
        GreetingCard card = new GreetingCard(message, 5.0, false);

        Packaging packaging = new Packaging("standard", "rosu", 2.0);

        this.result = new StandardGift(book, card, packaging);
        return this.result;
    }
}
