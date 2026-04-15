package com.example.tmppp_library_management.book;

public class Author {
    private int authorId;
    private String name;

    public Author(int authorId, String name) {
        this.authorId = authorId;
        this.name = name;
    }

    public int getAuthorId() {
        return authorId;
    }

    public String getName() {
        return name;
    }
}