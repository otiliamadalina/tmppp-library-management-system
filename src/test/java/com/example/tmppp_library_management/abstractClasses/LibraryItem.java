package com.example.tmppp_library_management.abstractClasses;

public abstract class LibraryItem {
    private int itemId;
    private String title;
    private int publicationDate;

    public LibraryItem(int itemId, String title, int publicationDate) {
        this.itemId = itemId;
        this.title = title;
        this.publicationDate = publicationDate;
    }

    public int getItemId() {
        return itemId;
    }

    public String getTitle() {
        return title;
    }

    public int getPublicationDate() {
        return publicationDate;
    }

    public abstract String getDescription();
}
