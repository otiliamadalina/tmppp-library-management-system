package com.example.tmppp_library_management.decorator;

import com.example.tmppp_library_management.book.Book;
import com.example.tmppp_library_management.user.MemberType;

public class RestrictedAccessDecorator extends BookDecorator {
    private MemberType requiredLevel;

    public RestrictedAccessDecorator(Book book, MemberType requiredLevel) {
        super(book);
        this.requiredLevel = requiredLevel;
    }

    @Override
    public void borrowItem() {
        super.borrowItem();
    }

    public boolean canBorrow(MemberType memberType) {
        if (memberType.ordinal() >= requiredLevel.ordinal()) {
            return true;
        }
        System.out.println("Acces restrictionat. Nivel necesar: " + requiredLevel);
        return false;
    }

    @Override
    public String getDescription() {
        return book.getDescription() + " [Acces: " + requiredLevel + "]";
    }

    public MemberType getRequiredLevel() { return requiredLevel; }
}