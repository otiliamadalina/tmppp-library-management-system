package com.example.tmppp_library_management.menus;

import com.example.tmppp_library_management.builder.GiftPackage;
import com.example.tmppp_library_management.book.Book;
import com.example.tmppp_library_management.builder.GiftPackageService;
import com.example.tmppp_library_management.builder.PremiumGift;
import com.example.tmppp_library_management.decorator.BookDecorator;
import com.example.tmppp_library_management.interfaces.IBorrowable;
import com.example.tmppp_library_management.observer.BookEvent;
import com.example.tmppp_library_management.observer.BookObserver;
import com.example.tmppp_library_management.services.*;

import java.util.List;

public class GiftPackageMenu implements BookObserver {
    private final GiftPackageService giftPackageService;
    private final BookService bookService;
    private final StockService stockService;
    private final LibrarianService librarianService;
    private List<IBorrowable> cachedBooks;

    public GiftPackageMenu(GiftPackageService giftPackageService, BookService bookService,
                           StockService stockService, MemberService memberService,
                           LibrarianService librarianService) {
        this.giftPackageService = giftPackageService;
        this.bookService = bookService;
        this.stockService = stockService;
        this.librarianService = librarianService;
        this.cachedBooks = bookService.findAllBooks();
        this.bookService.attach(this);
    }

    @Override
    public void update(BookEvent event) {
        this.cachedBooks = bookService.findAllBooks();
        System.out.println("[GiftPackageMenu] Lista carti actualizata: " + event.getMessage());
    }

    public void display(String token) {
        while (true) {
            System.out.println("\n--- PACHETE CADOU ---");
            System.out.println("1. Pachet Standard");
            System.out.println("2. Pachet Premium");
            System.out.println("3. Afiseaza stoc accesorii");
            System.out.println("4. Vezi toate pachetele create");
            System.out.println("5. Detalii pachet");
            System.out.println("0. Inapoi");

            int choice = MenuUtils.readInt("Alege: ");
            switch (choice) {
                case 1 -> createStandardGift(token);
                case 2 -> createPremiumGift(token);
                case 3 -> showAccessoryStock();
                case 4 -> showAllCreatedPackages();
                case 5 -> showPackageDetails();
                case 0 -> { return; }
                default -> System.out.println("Optiune invalida");
            }
        }
    }

    private Book getBookFromItem(IBorrowable item) {
        if (item instanceof Book) {
            return (Book) item;
        } else if (item instanceof BookDecorator) {
            return ((BookDecorator) item).getOriginalBook();
        }
        return null;
    }

    private void refreshCachedBooks() {
        this.cachedBooks = bookService.findAllBooks();
    }

    private void createStandardGift(String token) {
        System.out.println("\nPACHET CADOU STANDARD");

        IBorrowable item = chooseItem();
        if (item == null) return;

        Book book = getBookFromItem(item);
        if (book == null) {
            System.out.println("Eroare: Itemul nu este o carte valida");
            return;
        }

        String message = MenuUtils.readString("Mesaj pe felicitare: ");

        GiftPackage gift = giftPackageService.createGiftPackage("standard", book, message);

        if (gift != null) {
            librarianService.addToLog(token, "A creat pachet cadou STANDARD - carte: " + book.getTitle());

            System.out.println("\nPACHET CADOU CREAT CU SUCCES!");
            System.out.println("   Carte: " + book.getTitle());
            if (item instanceof BookDecorator) {
                System.out.println("   Restrictii: " + ((BookDecorator) item).getDescription());
            }
            System.out.println("   Felicitare: " + message);
            System.out.println("   Pret total: " + gift.calculateTotalPrice() + " lei");
            giftPackageService.printGiftReceipt(gift);
        }
    }

    private void createPremiumGift(String token) {
        System.out.println("\nPACHET CADOU PREMIUM");

        IBorrowable item = chooseItem();
        if (item == null) return;

        Book book = getBookFromItem(item);
        if (book == null) {
            System.out.println("Eroare: Itemul nu este o carte valida");
            return;
        }

        String message = MenuUtils.readString("Mesaj personalizat: ");

        GiftPackage gift = giftPackageService.createGiftPackage("premium", book, message);

        if (gift != null) {
            librarianService.addToLog(token, "A creat pachet cadou PREMIUM - carte: " + book.getTitle());

            System.out.println("\nPACHET PREMIUM CREAT CU SUCCES!");
            System.out.println("   Carte: " + book.getTitle());
            if (item instanceof BookDecorator) {
                System.out.println("   Restrictii: " + ((BookDecorator) item).getDescription());
            }
            System.out.println("   Felicitare personalizata: " + message);
            System.out.println("   + panglica, eticheta, ambalaj premium");
            System.out.println("   Pret total: " + gift.calculateTotalPrice() + " lei");
            giftPackageService.printGiftReceipt(gift);
        }
    }

    private IBorrowable chooseItem() {
        refreshCachedBooks();
        if (cachedBooks.isEmpty()) {
            System.out.println("Nu exista carti in sistem");
            return null;
        }

        System.out.println("\nCarti disponibile:");
        for (int i = 0; i < cachedBooks.size(); i++) {
            IBorrowable item = cachedBooks.get(i);
            Book book = getBookFromItem(item);
            if (book != null) {
                String restrictii = "";
                if (item instanceof BookDecorator) {
                    restrictii = " [RESTRICTIONAT: " + ((BookDecorator) item).getDescription() + "]";
                }
                System.out.println(" " + (i+1) + ". " + book.getTitle() + " de " + book.getAuthor().getName() + restrictii);
            }
        }

        int choice = MenuUtils.readInt("Alege numarul cartii: ") - 1;
        if (choice < 0 || choice >= cachedBooks.size()) {
            System.out.println("Optiune invalida");
            return null;
        }

        return cachedBooks.get(choice);
    }

    private void showAccessoryStock() {
        System.out.println("\nSTOC ACCESORII:");
        System.out.println("  - Felicitari: " +
                (stockService.getStock("greeting_card") != null ?
                        stockService.getStock("greeting_card").getAvailableQuantity() : 0) + " buc");
        System.out.println("  - Ambalaj standard: " +
                (stockService.getStock("packaging_standard") != null ?
                        stockService.getStock("packaging_standard").getAvailableQuantity() : 0) + " buc");
        System.out.println("  - Ambalaj premium: " +
                (stockService.getStock("packaging_premium") != null ?
                        stockService.getStock("packaging_premium").getAvailableQuantity() : 0) + " buc");
        System.out.println("  - Panglici: " +
                (stockService.getStock("ribbon") != null ?
                        stockService.getStock("ribbon").getAvailableQuantity() : 0) + " buc");
        System.out.println("  - Etichete cadou: " +
                (stockService.getStock("gift_tag") != null ?
                        stockService.getStock("gift_tag").getAvailableQuantity() : 0) + " buc");
    }

    private void showAllCreatedPackages() {
        List<GiftPackage> packages = giftPackageService.getCreatedPackages();

        if (packages.isEmpty()) {
            System.out.println("\nNu s-au creat pachete cadou");
            return;
        }

        System.out.println("\nTOATE PACHETELE CREATE:");
        System.out.println("Nr | Carte                  | Tip       | Pret");
        System.out.println("---+------------------------+-----------+--------");

        for (int i = 0; i < packages.size(); i++) {
            GiftPackage p = packages.get(i);
            String tip = (p instanceof PremiumGift) ? "PREMIUM" : "STANDARD";

            System.out.printf("%-2d | %-22s | %-9s | %.2f lei\n",
                    i + 1,
                    MenuUtils.truncate(p.getBook().getTitle(), 22),
                    tip,
                    p.calculateTotalPrice());
        }
    }

    private void showPackageDetails() {
        List<GiftPackage> packages = giftPackageService.getCreatedPackages();

        if (packages.isEmpty()) {
            System.out.println("\nNu exista pachete create.");
            return;
        }

        int nr = MenuUtils.readInt("Alege numarul pachetului: ") - 1;
        if (nr < 0 || nr >= packages.size()) {
            System.out.println("Numar invalid");
            return;
        }

        GiftPackage p = packages.get(nr);

        System.out.println("\nDETALII PACHET:");
        System.out.println("   Carte: " + p.getBook().getTitle());
        System.out.println("   Autor: " + p.getBook().getAuthor().getName());
        System.out.println("   Felicitare: " + p.getCard().getMessage());
        System.out.println("   Ambalaj: " + p.getPackaging().getType() + " (" + p.getPackaging().getColor() + ")");

        if (p instanceof PremiumGift premium) {
            System.out.println("   Panglica: " + premium.getRibbon().getColor());
            System.out.println("   Eticheta: " + premium.getGiftTag().getText());
        }

        System.out.println("   PRET TOTAL: " + p.calculateTotalPrice() + " lei");
        giftPackageService.printGiftReceipt(p);
    }
}