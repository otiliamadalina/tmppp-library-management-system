package com.example.tmppp_library_management.newspaper;

import com.example.tmppp_library_management.abstractClasses.StockedItem;
import com.example.tmppp_library_management.flyweight.Publisher;

public class Newspaper extends StockedItem {
    private Publisher publisher;
    private String issn;

    public Newspaper(int itemId, String title, int publicationDate, int pageCount,
                     Publisher publisher, String issn) {
        super(itemId, title, publicationDate, pageCount);
        this.publisher = publisher;
        this.issn = issn;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    public String getIssn() {
        return issn;
    }

    @Override
    public String getDescription() {
        return "Newspaper: " + getTitle() + " - " + publisher;
    }

}


