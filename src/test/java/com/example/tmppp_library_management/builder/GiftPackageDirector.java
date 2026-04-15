package com.example.tmppp_library_management.builder;

import com.example.tmppp_library_management.book.Book;

public class GiftPackageDirector {
    private GiftPackageBuilder builder;

    public GiftPackageDirector(GiftPackageBuilder builder) {
        this.builder = builder;
    }

    public void changeBuilder(GiftPackageBuilder builder) {
        this.builder = builder;
    }

    public void make(String type, Book book, String message) {
        builder.reset();
        builder.buildBook(book);
        builder.buildGreetingCard(message);
        builder.buildPackaging();

        if ("premium".equals(type) && builder instanceof PremiumGiftBuilder) {
            ((PremiumGiftBuilder) builder).buildRibbon("auriu");
            ((PremiumGiftBuilder) builder).buildGiftTag("La multi ani!");
        }
    }
}
