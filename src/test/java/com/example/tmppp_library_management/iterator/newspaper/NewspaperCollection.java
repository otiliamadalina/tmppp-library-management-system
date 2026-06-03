package com.example.tmppp_library_management.iterator.newspaper;

import com.example.tmppp_library_management.newspaper.Newspaper;
import java.util.ArrayList;
import java.util.List;

public class NewspaperCollection implements IterableNewspaperCollection {
    private List<Newspaper> newspapers;

    public NewspaperCollection() {
        this.newspapers = new ArrayList<>();
    }

    public void addNewspaper(Newspaper newspaper) {
        this.newspapers.add(newspaper);
    }

    public List<Newspaper> getNewspapers() {
        return newspapers;
    }

    @Override
    public NewspaperIterator createTitleIterator() {
        return new TitleNewspaperIterator(newspapers);
    }

    @Override
    public NewspaperIterator createDateIterator() {
        return new DateNewspaperIterator(newspapers);
    }

    @Override
    public NewspaperIterator createPublisherIterator() {
        return new PublisherNewspaperIterator(newspapers);
    }
}