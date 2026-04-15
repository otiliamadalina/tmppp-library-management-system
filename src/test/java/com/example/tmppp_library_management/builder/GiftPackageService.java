package com.example.tmppp_library_management.builder;

import com.example.tmppp_library_management.book.Book;
import com.example.tmppp_library_management.services.StockService;

import java.util.ArrayList;
import java.util.List;

public class GiftPackageService {
    private GiftPackageDirector director;
    private StockService stockService;
    private List<GiftPackage> createdPackages;

    public GiftPackageService(StockService stockService) {
        this.stockService = stockService;
        this.createdPackages = new ArrayList<>();
    }

    public GiftPackage createGiftPackage(String type, Book book, String message) {
        if (!stockService.checkAvailability(book.getIsbn(), 1)) {
            System.out.println("Carte indisponibila");
            return null;
        }

        GiftPackageBuilder builder;
        if ("premium".equals(type)) {
            builder = new PremiumGiftBuilder();
        } else {
            builder = new StandardGiftBuilder();
        }

        this.director = new GiftPackageDirector(builder);

        // construim pachetul
        director.make(type, book, message);

        GiftPackage gift = builder.getResult();

        stockService.decreaseStock(book.getIsbn(), 1);
        stockService.decreaseStock("greeting_card", 1);

        if ("standard".equals(type)) {
            stockService.decreaseStock("packaging_standard", 1);
        } else {
            stockService.decreaseStock("packaging_premium", 1);
            stockService.decreaseStock("ribbon", 1);
            stockService.decreaseStock("gift_tag", 1);
        }

        if (gift != null) {
            createdPackages.add(gift);
        }

        return gift;
    }

    public List<GiftPackage> getCreatedPackages() {
        return new ArrayList<>(createdPackages);
    }

    public GiftPackage getPackage(int index) {
        if (index >= 0 && index < createdPackages.size()) {
            return createdPackages.get(index);
        }
        return null;
    }

    public double calculatePrice(GiftPackage gift) {
        return gift.calculateTotalPrice();
    }

    public void printGiftReceipt(GiftPackage gift) {
        System.out.println("\n=== CHITANTA PACHET CADOU ===");
        System.out.println("Carte: " + gift.getBook().getTitle());
        System.out.println("Pret total: " + gift.calculateTotalPrice() + " lei");

        if (gift instanceof PremiumGift) {
            System.out.println("Pachet PREMIUM");
        } else {
            System.out.println("Pachet STANDARD");
        }
        System.out.println("----------");
    }

    public boolean validateStockForGift(GiftPackage gift) {
        if (!stockService.checkAvailability(gift.getBook().getIsbn(), 1)) {
            return false;
        }

        if (gift instanceof PremiumGift) {
            return stockService.checkAvailability("ribbon", 1) &&
                    stockService.checkAvailability("gift_tag", 1) &&
                    stockService.checkAvailability("packaging_premium", 1);
        } else {
            return stockService.checkAvailability("packaging_standard", 1);
        }
    }
}