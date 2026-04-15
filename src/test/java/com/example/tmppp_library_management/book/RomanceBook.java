package com.example.tmppp_library_management.book;

import com.example.tmppp_library_management.flyweight.Publisher;

public class RomanceBook extends Book {
    private int romanceLevel;
    private String tropes;

    public RomanceBook(int itemId, String title, int publicationDate, int pageCount,
                       Author author, String isbn, Publisher publisher,
                       int romanceLevel, String tropes, double price) {
        super(itemId, title, publicationDate, pageCount, author, isbn, publisher, price);
        this.romanceLevel = romanceLevel;
        this.tropes = tropes;
    }

    @Override
    public String getDescription() {
        return "RomanceBook: " + getTitle() + " de " + author.getName() +
                ", level: " + romanceLevel + ", tropi: " + tropes;
    }

    public int getRomanceLevel() {
        return romanceLevel;
    }

    public void setRomanceLevel(int romanceLevel) {
        this.romanceLevel = romanceLevel;
    }

    public String getTropes() {
        return tropes;
    }

    public void setTropes(String tropes) {
        this.tropes = tropes;
    }
}