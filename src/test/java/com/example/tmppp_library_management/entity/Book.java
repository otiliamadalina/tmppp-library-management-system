package com.example.tmppp_library_management.entity;

import com.example.tmppp_library_management.abstractClasses.BorrowableItem;

public class Book extends BorrowableItem {
    private Author author;
    private String publisher;
    private String isbn;

    public Book(int itemId, String title, int publicationYear, int quantity,
                String authorName, String publisher, String isbn) {
        super(itemId, title, publicationYear, quantity);
        this.author = new Author(authorName);
        this.publisher = publisher;
        this.isbn = isbn;
    }

    public Author getAuthor() {
        return author;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getIsbn() {
        return isbn;
    }

    @Override
    public String getDescription() {
        return "Book: " + getTitle() + " by " + author.getAuthorName();
    }
}


