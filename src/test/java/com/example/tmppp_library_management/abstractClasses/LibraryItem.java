package com.example.tmppp_library_management.abstractClasses;

public abstract class LibraryItem {
    protected int itemId;
    protected int pageCount;
    protected int publicationDate;
    protected String title;

    public LibraryItem(int itemId, String title, int publicationDate, int pageCount) {
        this.itemId = itemId;
        this.title = title;
        this.publicationDate = publicationDate;
        this.pageCount = pageCount;
    }

    public int getItemId() {
        return itemId;
    }

    public int getPageCount() {
        return pageCount;
    }

    public int getPublicationDate() {
        return publicationDate;
    }

    public String getTitle() {
        return title;
    }


    public abstract String getDescription();
}