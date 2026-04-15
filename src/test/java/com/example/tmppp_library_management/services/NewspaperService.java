package com.example.tmppp_library_management.services;

import com.example.tmppp_library_management.abstractClasses.LibraryItem;
import com.example.tmppp_library_management.factories.NewspaperFactory;
import com.example.tmppp_library_management.flyweight.Publisher;
import com.example.tmppp_library_management.flyweight.PublisherFactory;
import com.example.tmppp_library_management.newspaper.LocalNewspaper;
import com.example.tmppp_library_management.newspaper.NationalNewspaper;
import com.example.tmppp_library_management.newspaper.Newspaper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Factory Method: createItem() delegeaza la NewspaperFactory (Abstract Factory) pentru a crea itemul.
 */
public class NewspaperService extends LibraryService {
    private final NewspaperFactory newspaperFactory;

    public NewspaperService(NewspaperFactory newspaperFactory) {
        this.newspaperFactory = newspaperFactory;
    }

    @Override
    public LibraryItem createItem(int itemId, String title, int year, int pageCount) {
        LibraryItem item = newspaperFactory.createItem(itemId, title, year, pageCount);
        addItem(item);
        return item;
    }

    public LocalNewspaper createLocalNewspaper(int itemId, String title, int year, int pages,
                                               String publisherName, String issn,
                                               String city, String region) {
        Publisher publisher = PublisherFactory.getInstance().getPublisher(publisherName);
        LocalNewspaper newspaper = newspaperFactory.createLocalNewspaper(itemId, title, year, pages, publisher, issn, city, region);
        addItem(newspaper);
        return newspaper;
    }

    public NationalNewspaper createNationalNewspaper(int itemId, String title, int year, int pages,
                                                     String publisherName, String issn,
                                                     String area, String orientation) {
        Publisher publisher = PublisherFactory.getInstance().getPublisher(publisherName);
        NationalNewspaper newspaper = newspaperFactory.createNationalNewspaper(itemId, title, year, pages, publisher, issn, area, orientation);
        addItem(newspaper);
        return newspaper;
    }

    public List<LocalNewspaper> findByCity(String city) {
        return items.stream()
                .filter(item -> item instanceof LocalNewspaper)
                .map(item -> (LocalNewspaper) item)
                .filter(local -> local.getCity().equalsIgnoreCase(city))
                .collect(Collectors.toList());
    }

    public List<Newspaper> findByRegion(String region) {
        return items.stream()
                .filter(item -> item instanceof Newspaper)
                .map(item -> (Newspaper) item)
                .filter(newspaper -> {
                    if (newspaper instanceof LocalNewspaper) {
                        return ((LocalNewspaper) newspaper).getRegion().equalsIgnoreCase(region);
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }
}