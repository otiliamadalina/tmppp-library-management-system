package com.example.tmppp_library_management.book;

import com.example.tmppp_library_management.flyweight.Publisher;

public class FantasyBook extends Book {
    private String targetAge;

    public FantasyBook(int itemId, String title, int publicationDate, int pageCount,
                       Author author, String isbn, Publisher publisher, double price) {
        super(itemId, title, publicationDate, pageCount, author, isbn, publisher, price);
    }

    public String getTargetAge() {
        return targetAge;
    }

    public void setTargetAge(String targetAge) {
        this.targetAge = targetAge;
    }

    @Override
    public String getDescription() {
        return "Fantasy: " + getTitle() + " de " + getAuthor().getName();
    }
}
