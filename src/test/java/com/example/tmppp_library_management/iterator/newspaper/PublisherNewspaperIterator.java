package com.example.tmppp_library_management.iterator.newspaper;

import com.example.tmppp_library_management.newspaper.Newspaper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PublisherNewspaperIterator implements NewspaperIterator {
    private List<Newspaper> newspapers;
    private int position;

    public PublisherNewspaperIterator(List<Newspaper> newspapers) {
        this.newspapers = new ArrayList<>(newspapers);
        this.newspapers.sort(Comparator.comparing(n -> n.getPublisher().getName()));
        this.position = 0;
    }

    @Override
    public boolean hasNext() {
        return position < newspapers.size();
    }

    @Override
    public Newspaper next() {
        if (hasNext()) {
            return newspapers.get(position++);
        }
        return null;
    }
}