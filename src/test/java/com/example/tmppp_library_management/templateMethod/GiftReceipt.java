package com.example.tmppp_library_management.templateMethod;

import com.example.tmppp_library_management.builder.GiftPackage;
import com.example.tmppp_library_management.builder.PremiumGift;

public class GiftReceipt extends ReceiptTemplate {
    private final GiftPackage gift;

    public GiftReceipt(GiftPackage gift) {
        this.gift = gift;
    }

    @Override
    protected String getHeader() {
        return "        CHITANTA PACHET CADOU";
    }

    @Override
    protected String getBody() {
        StringBuilder sb = new StringBuilder();
        sb.append("Carte: ").append(gift.getBook().getTitle()).append("\n");
        sb.append("Autor: ").append(gift.getBook().getAuthor().getName()).append("\n");
        sb.append("Felicitare: ").append(gift.getCard().getMessage()).append("\n");
        sb.append("Ambalaj: ").append(gift.getPackaging().getType()).append("\n");

        if (gift instanceof PremiumGift premium) {
            sb.append("Panglica: ").append(premium.getRibbon().getColor()).append("\n");
            sb.append("Eticheta: ").append(premium.getGiftTag().getText()).append("\n");
        }

        sb.append("\n");
        sb.append("PRET TOTAL: ").append(formatPrice(gift.calculateTotalPrice())).append("\n");
        return sb.toString();
    }

    @Override
    protected String getFooter() {
        return "        Cadou perfect pentru orice ocazie!";
    }
}