package com.example.tmppp_library_management.entity;

import com.example.tmppp_library_management.abstractClasses.StockedItem;

public class Newspaper extends StockedItem {
    private String publisher;

    public Newspaper(int itemId, String title, int publicationYear, int quantity, String publisher) {
        super(itemId, title, publicationYear, quantity);
        this.publisher = publisher;
    }

    public String getPublisher() {
        return publisher;
    }

    @Override
    public String getDescription() {
        return "Newspaper: " + getTitle() + " by " + publisher;
    }
}


