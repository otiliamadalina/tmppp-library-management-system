package com.example.tmppp_library_management.iterator.newspaper;

public interface IterableNewspaperCollection {
    NewspaperIterator createTitleIterator();
    NewspaperIterator createDateIterator();
    NewspaperIterator createPublisherIterator();
}