package com.example.tmppp_library_management.newspaper;

import com.example.tmppp_library_management.flyweight.Publisher;

public class LocalNewspaper extends Newspaper {
    private String city;
    private String region;

    public LocalNewspaper(int itemId, String title, int publicationDate, int pageCount,
                          Publisher publisher, String issn, String city, String region) {
        super(itemId, title, publicationDate, pageCount, publisher, issn);
        this.city = city;
        this.region = region;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @Override
    public String getDescription() {
        return "LocalNewspaper: " + getTitle() + " - " + city + ", " + region;
    }
}
