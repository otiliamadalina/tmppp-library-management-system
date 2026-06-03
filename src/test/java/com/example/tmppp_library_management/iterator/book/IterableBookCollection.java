package com.example.tmppp_library_management.iterator.book;

public interface IterableBookCollection {
    BookIterator createTitleIterator();
    BookIterator createAuthorIterator();
    BookIterator createYearIterator();
}