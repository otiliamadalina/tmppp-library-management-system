package com.example.tmppp_library_management.builder;

import com.example.tmppp_library_management.book.Book;

public class PremiumGift extends GiftPackage {
    private Ribbon ribbon;
    private GiftTag giftTag;

    public PremiumGift(Book book, GreetingCard card, Packaging packaging,
                       Ribbon ribbon, GiftTag giftTag) {
        super(book, card, packaging);
        this.ribbon = ribbon;
        this.giftTag = giftTag;
    }

    @Override
    public double calculateTotalPrice() {
        double total = card.getPrice() + packaging.getPrice();

        if (ribbon != null) {
            total += ribbon.getPrice();
        }

        if (giftTag != null) {
            total += giftTag.getPrice();
        }

        return total;
    }

    public Ribbon getRibbon() { return ribbon; }
    public GiftTag getGiftTag() { return giftTag; }
}