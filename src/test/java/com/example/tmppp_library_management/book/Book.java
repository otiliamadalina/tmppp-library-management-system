package com.example.tmppp_library_management.book;

import com.example.tmppp_library_management.abstractClasses.BorrowableItem;
import com.example.tmppp_library_management.flyweight.Publisher;

public abstract class Book extends BorrowableItem {
    protected Author author;
    protected String isbn;
    protected Publisher publisher;
    protected double price;

    public Book(int itemId, String title, int publicationDate, int pageCount,
                Author author, String isbn, Publisher publisher, double price) {
        super(itemId, title, publicationDate, pageCount);
        this.author = author;
        this.isbn = isbn;
        this.publisher = publisher;
        this.price = price;
    }

    public Author getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public Publisher getPublisher() { return publisher; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public void setPublisher(Publisher publisher) { this.publisher = publisher; }

    public abstract String getDescription();
}