package com.example.tmppp_library_management.builder;

import com.example.tmppp_library_management.book.Book;

public class PremiumGiftBuilder implements GiftPackageBuilder {
    private PremiumGift result;
    private Book book;
    private String message;
    private String ribbonColor;
    private String tagText;

    @Override
    public void reset() {
        this.result = null;
        this.book = null;
        this.message = null;
        this.ribbonColor = "auriu";
        this.tagText = "La multi ani!";
    }

    @Override
    public void buildBook(Book book) {
        this.book = book;
    }

    @Override
    public void buildGreetingCard(String message) {
        this.message = message;
    }

    @Override
    public void buildPackaging() {
    }

    public void buildRibbon(String color) {
        this.ribbonColor = color;
    }

    public void buildGiftTag(String text) {
        this.tagText = text;
    }

    @Override
    public GiftPackage getResult() {
        GreetingCard card = new GreetingCard(message, 10.0, true);
        Packaging packaging = new Packaging("premium", "burgundy", 5.0);

        Ribbon ribbon = new Ribbon("R001", "Panglica satin", "satin", "ribbon", 3.0, ribbonColor);
        GiftTag tag = new GiftTag("T001", "Eticheta cadou", "hartie", "gift_tag", 2.0, tagText);

        this.result = new PremiumGift(book, card, packaging, ribbon, tag);
        return this.result;
    }
}
