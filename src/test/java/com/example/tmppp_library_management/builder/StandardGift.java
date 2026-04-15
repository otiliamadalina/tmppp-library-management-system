package com.example.tmppp_library_management.builder;

import com.example.tmppp_library_management.book.Book;

public class StandardGift extends GiftPackage {

    public StandardGift(Book book, GreetingCard card, Packaging packaging) {
        super(book, card, packaging);
    }

    @Override
    public double calculateTotalPrice() {
        return card.getPrice() + packaging.getPrice();
    }
}
