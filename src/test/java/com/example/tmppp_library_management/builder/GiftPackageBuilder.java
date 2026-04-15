package com.example.tmppp_library_management.builder;

import com.example.tmppp_library_management.book.Book;

public interface GiftPackageBuilder {
    void reset();
    void buildBook(Book book);
    void buildGreetingCard(String message);
    void buildPackaging();
    GiftPackage getResult();
}
