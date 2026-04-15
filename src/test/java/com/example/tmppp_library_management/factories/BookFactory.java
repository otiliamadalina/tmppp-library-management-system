package com.example.tmppp_library_management.factories;

import com.example.tmppp_library_management.abstractClasses.LibraryItem;
import com.example.tmppp_library_management.book.Author;
import com.example.tmppp_library_management.book.Book;
import com.example.tmppp_library_management.book.FantasyBook;
import com.example.tmppp_library_management.book.RomanceBook;
import com.example.tmppp_library_management.flyweight.Publisher;
import com.example.tmppp_library_management.flyweight.PublisherFactory;

public class BookFactory extends LibraryItemFactory {

    @Override
    public LibraryItem createItem(int itemId, String title, int year, int pageCount) {
        Author defaultAuthor = new Author(0, "Unknown Author");
        Publisher defaultPublisher = PublisherFactory.getInstance().getPublisher("Unknown Publisher");
        return createFantasyBook(itemId, title, year, pageCount, defaultAuthor, "0000-0000", defaultPublisher, 0);
    }

    public Book createBook(int itemId, String title, int year, int pageCount,
                           Author author, String isbn, Publisher publisher, double price) {
        return new FantasyBook(itemId, title, year, pageCount, author, isbn, publisher, price);
    }

    public FantasyBook createFantasyBook(int itemId, String title, int year, int pageCount,
                                         Author author, String isbn, Publisher publisher, double price) {
        return new FantasyBook(itemId, title, year, pageCount, author, isbn, publisher, price);
    }

    public RomanceBook createRomanceBook(int itemId, String title, int year, int pageCount,
                                         Author author, String isbn, Publisher publisher,
                                         int romanceLevel, String tropes, double price) {
        return new RomanceBook(itemId, title, year, pageCount, author, isbn, publisher,
                romanceLevel, tropes, price);
    }
}