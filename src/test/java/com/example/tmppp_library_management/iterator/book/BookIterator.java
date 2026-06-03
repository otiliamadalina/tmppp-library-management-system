package com.example.tmppp_library_management.iterator.book;

import com.example.tmppp_library_management.book.Book;

public interface BookIterator {
    boolean hasNext();
    Book next();
}