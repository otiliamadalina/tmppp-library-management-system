package com.example.tmppp_library_management.user;

public enum MemberType {
    SIMPLE(3, 14),
    STUDENT(5, 21),
    PROFESSOR(10, 30);

    private final int maxBooks;
    private final int defaultDuration;

    MemberType(int maxBooks, int defaultDuration) {
        this.maxBooks = maxBooks;
        this.defaultDuration = defaultDuration;
    }

    public int getMaxBooks() { return maxBooks; }
    public int getDefaultDuration() { return defaultDuration; }

    @Override
    public String toString() {
        return name();
    }
}