package com.example.tmppp_library_management.iterator.book;

import com.example.tmppp_library_management.book.Book;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AuthorBookIterator implements BookIterator {
    private List<Book> books;
    private int position;

    public AuthorBookIterator(List<Book> books) {
        this.books = new ArrayList<>(books);
        this.books.sort(Comparator.comparing(book -> book.getAuthor().getName()));
        this.position = 0;
    }

    @Override
    public boolean hasNext() {
        return position < books.size();
    }

    @Override
    public Book next() {
        if (hasNext()) {
            return books.get(position++);
        }
        return null;
    }
}