package com.example.tmppp_library_management.factories;

import com.example.tmppp_library_management.abstractClasses.LibraryItem;
import com.example.tmppp_library_management.book.Book;
import com.example.tmppp_library_management.newspaper.*;
import com.example.tmppp_library_management.flyweight.Publisher;
import com.example.tmppp_library_management.flyweight.PublisherFactory;

public class NewspaperFactory extends LibraryItemFactory {

    @Override
    public LibraryItem createItem(int itemId, String title, int year, int pageCount) {
        Publisher defaultPublisher = PublisherFactory.getInstance().getPublisher("Unknown Publisher");
        return createLocalNewspaper(itemId, title, year, pageCount, defaultPublisher, "0000-0000", "Unknown", "Unknown");
    }

    public LocalNewspaper createLocalNewspaper(int itemId, String title, int year, int pages,
                                               Publisher publisher, String issn,
                                               String city, String region) {
        return new LocalNewspaper(itemId, title, year, pages, publisher, issn, city, region);
    }

    public NationalNewspaper createNationalNewspaper(int itemId, String title, int year, int pages,
                                                     Publisher publisher, String issn,
                                                     String area, String orientation) {
        return new NationalNewspaper(itemId, title, year, pages, publisher, issn, area, orientation);
    }

}