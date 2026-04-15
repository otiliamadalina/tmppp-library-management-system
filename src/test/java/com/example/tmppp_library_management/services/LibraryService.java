package com.example.tmppp_library_management.services;

import com.example.tmppp_library_management.abstractClasses.LibraryItem;

import java.util.ArrayList;
import java.util.List;

public abstract class LibraryService {
    protected List<LibraryItem> items = new ArrayList<>();

    public void addItem(LibraryItem item) {
        items.add(item);
    }

    public List<LibraryItem> getAllItems() {
        return new ArrayList<>(items);
    }

    public LibraryItem findById(int itemId) {
        return items.stream()
                .filter(item -> item.getItemId() == itemId)
                .findFirst()
                .orElse(null);
    }

    public boolean removeItem(int itemId) {
        return items.removeIf(item -> item.getItemId() == itemId);
    }

    public boolean removeItem(LibraryItem item) {
        return items.remove(item);
    }

    /**
     * Factory Method: metoda abstracta prin care subclasele (BookService, NewspaperService)
     * definesc cum se creeaza un item. Fiecare serviciu apeleaza fabrica sa concretă si adauga
     * itemul in lista. Creatorul (LibraryService) nu depinde de clase concrete de iteme.
     */
    public abstract LibraryItem createItem(int itemId, String title, int year, int pageCount);

    public LibraryItem createAndAddItem(int itemId, String title, int year, int pageCount) {
        LibraryItem item = createItem(itemId, title, year, pageCount);
        addItem(item);
        return item;
    }
}