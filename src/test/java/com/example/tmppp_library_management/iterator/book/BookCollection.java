package com.example.tmppp_library_management.iterator.book;

import com.example.tmppp_library_management.book.Book;
import java.util.ArrayList;
import java.util.List;

public class BookCollection implements IterableBookCollection {
    private List<Book> books;

    public BookCollection() {
        this.books = new ArrayList<>();
    }

    public void addBook(Book book) {
        this.books.add(book);
    }

    public List<Book> getBooks() {
        return books;
    }

    @Override
    public BookIterator createTitleIterator() {
        return new TitleBookIterator(books);
    }

    @Override
    public BookIterator createAuthorIterator() {
        return new AuthorBookIterator(books);
    }

    @Override
    public BookIterator createYearIterator() {
        return new YearBookIterator(books);
    }
}