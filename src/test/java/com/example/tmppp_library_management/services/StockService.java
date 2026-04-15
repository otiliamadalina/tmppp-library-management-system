package com.example.tmppp_library_management.services;

import com.example.tmppp_library_management.entity.Stock;

import java.util.HashMap;
import java.util.Map;

// SINGLETON
public class StockService {
    // instanta unica
    private static StockService instance;

    private Map<String, Stock> stocks;
    private int nextStockId;

    private StockService() { // nimeni nu poate face 'new' din afaara
        this.stocks = new HashMap<>();
        this.nextStockId = 1;
    }

    public static StockService getInstance() {
        if (instance == null) {
            instance = new StockService(); // se creeaza 1 singura data
        }
        return instance; // returneaza aceeasi instanta
    }

    public Stock addStock(String itemId, int initialQuantity) {
        if (stocks.containsKey(itemId)) {
            Stock existingStock = stocks.get(itemId);
            existingStock.setQuantity(existingStock.getQuantity() + initialQuantity);
            existingStock.setAvailableQuantity(existingStock.getAvailableQuantity() + initialQuantity);
            return existingStock;
        } else {
            Stock stock = new Stock(nextStockId++, itemId, initialQuantity);
            stocks.put(itemId, stock);
            return stock;
        }
    }

    public Stock getStock(String itemId) {
        return stocks.get(itemId);
    }

    public boolean checkAvailability(String itemId, int requestedQuantity) {
        System.out.println("Verific stoc pentru itemId: " + itemId);

        Stock stock = stocks.get(itemId);
        if (stock == null) {
            System.out.println("STOC NULL pentru: " + itemId);
            System.out.println("Stocuri disponibile: " + stocks.keySet());
            return false;
        }
        System.out.println("Stoc gasit: " + stock.getAvailableQuantity() + " bucati");
        return stock.getAvailableQuantity() >= requestedQuantity;
    }

    public boolean checkAvailability(String itemId) {
        return checkAvailability(itemId, 1);
    }

    public boolean reserveItem(String itemId, int quantity) {
        Stock stock = stocks.get(itemId);
        if (stock == null) return false;

        int available = stock.getAvailableQuantity();
        if (available >= quantity) {
            stock.setAvailableQuantity(available - quantity);
            stock.setReservedQuantity(stock.getReservedQuantity() + quantity);
            return true;
        }
        return false;
    }

    public boolean releaseItem(String itemId, int quantity) {
        Stock stock = stocks.get(itemId);
        if (stock == null) return false;

        int reserved = stock.getReservedQuantity();
        if (reserved >= quantity) {
            stock.setAvailableQuantity(stock.getAvailableQuantity() + quantity);
            stock.setReservedQuantity(reserved - quantity);
            return true;
        }
        return false;
    }

    public void decreaseStock(String itemId, int quantity) {
        Stock stock = stocks.get(itemId);
        if (stock != null) {
            int newQuantity = stock.getQuantity() - quantity;
            int newAvailable = stock.getAvailableQuantity() - quantity;

            if (newQuantity < 0) newQuantity = 0;
            if (newAvailable < 0) newAvailable = 0;

            stock.setQuantity(newQuantity);
            stock.setAvailableQuantity(newAvailable);
        }
    }

    public void increaseStock(String itemId, int quantity) {
        Stock stock = stocks.get(itemId);
        if (stock != null) {
            stock.setQuantity(stock.getQuantity() + quantity);
            stock.setAvailableQuantity(stock.getAvailableQuantity() + quantity);
        }
    }

    public void updateStock(String itemId, int quantityChange) {
        Stock stock = stocks.get(itemId);
        if (stock != null) {
            int newQuantity = stock.getQuantity() + quantityChange;
            int newAvailable = stock.getAvailableQuantity() + quantityChange;

            if (newQuantity < 0) newQuantity = 0;
            if (newAvailable < 0) newAvailable = 0;

            stock.setQuantity(newQuantity);
            stock.setAvailableQuantity(newAvailable);
        }
    }

    public Map<String, Stock> getAllStocks() {
        return new HashMap<>(stocks);
    }

    public boolean removeStock(String itemId) {
        if (stocks.containsKey(itemId)) {
            stocks.remove(itemId);
            return true;
        }
        return false;
    }
}