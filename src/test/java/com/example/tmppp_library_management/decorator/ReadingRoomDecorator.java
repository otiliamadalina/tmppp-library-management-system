package com.example.tmppp_library_management.decorator;

import com.example.tmppp_library_management.book.Book;

public class ReadingRoomDecorator extends BookDecorator {
    private String room;
    private boolean isInReadingRoom;

    public ReadingRoomDecorator(Book book, String room) {
        super(book);
        this.room = room;
        this.isInReadingRoom = false;
    }

    @Override
    public void borrowItem() {
        if (!isInReadingRoom) {
            System.out.println("Cartea poate fi citita doar in sala de lectura " + room);
            return;
        }
        super.borrowItem();
    }

    @Override
    public String getDescription() {
        return book.getDescription() + " [Doar in sala " + room + "]";
    }

    public void enterReadingRoom() {
        this.isInReadingRoom = true;
        System.out.println("Intrat in sala de lectura " + room);
    }

    public void leaveReadingRoom() {
        this.isInReadingRoom = false;
        System.out.println("Parasit sala de lectura " + room);
    }

    public String getRoom() { return room; }
}