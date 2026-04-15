package com.example.tmppp_library_management.factories;

import com.example.tmppp_library_management.abstractClasses.LibraryItem;
import com.example.tmppp_library_management.book.Book;
import com.example.tmppp_library_management.newspaper.Newspaper;

/**
 * Abstract Factory: interfata comuna pentru fabrici de iteme biblioteca.
 * Fabricile concrete (BookFactory, NewspaperFactory) implementeaza createItem()
 * si creeaza produse de tipul lor (carti, ziare). Clientul lucreaza doar cu
 * LibraryItemFactory, fara a depinde de clase concrete
 */
public abstract class LibraryItemFactory {
    public abstract LibraryItem createItem(int itemId, String title, int year, int pageCount);

}
