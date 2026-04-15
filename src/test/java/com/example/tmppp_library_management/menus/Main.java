package com.example.tmppp_library_management.menus;

import com.example.tmppp_library_management.abstractClasses.LibraryItem;
import com.example.tmppp_library_management.builder.GiftPackageService;
import com.example.tmppp_library_management.composite.EventService;
import com.example.tmppp_library_management.factories.BookFactory;
import com.example.tmppp_library_management.factories.NewspaperFactory;
import com.example.tmppp_library_management.services.*;
import com.example.tmppp_library_management.singleton.LoanTemplateRegistry;

import java.util.ArrayList;
import java.util.List;

public class Main {
    // Factory-uri
    private static final BookFactory bookFactory = new BookFactory();
    private static final NewspaperFactory newspaperFactory = new NewspaperFactory();

    // Service-uri
    private static final BookService bookService = new BookService(bookFactory);
    private static final NewspaperService newspaperService = new NewspaperService(newspaperFactory);
    private static final StockService stockService = StockService.getInstance();
    private static final MemberService memberService = MemberService.getInstance();
    private static final LoanService loanService = new LoanService();
    private static final GiftPackageService giftPackageService = new GiftPackageService(stockService);
    private static final EventService eventService = EventService.getInstance();
    private static final LibrarianService librarianService = LibrarianService.getInstance();

    // Session curenta
    private static String currentToken = null;

    // Meniuri
    private static BookMenu bookMenu;
    private static NewspaperMenu newspaperMenu;
    private static MemberMenu memberMenu;
    private static LoanMenu loanMenu;
    private static GiftPackageMenu giftPackageMenu;
    private static EventMenu eventMenu;
    private static ReceiptMenu receiptMenu;

    public static void main(String[] args) {
        initializeSystem();
        initializeMenus();

        // INITIALIZARE CHAIN OF RESPONSIBILITY
        MenuUtils.initValidation(bookService, memberService, newspaperService);

        // Autentificare
        if (!login()) {
            System.out.println("Autentificare esuata. Programul se inchide.");
            return;
        }

        // Meniul principal
        while (true) {
            printMainMenu();
            int choice = MenuUtils.readInt("Alege o optiune: ");

            switch (choice) {
                case 1 -> bookMenu.display(currentToken);
                case 2 -> newspaperMenu.display(currentToken);
                case 3 -> memberMenu.display(currentToken);
                case 4 -> loanMenu.display(currentToken);
                case 5 -> giftPackageMenu.display(currentToken);
                case 6 -> listAllItems();
                case 7 -> eventMenu.display(currentToken);
                case 8 -> receiptMenu.display();
                case 9 -> showMyLog();
                case 0 -> {
                    logout();
                    System.out.println("La revedere");
                    System.exit(0);
                }
                default -> System.out.println("Optiune invalida");
            }
        }
    }

    private static boolean login() {
        System.out.println("\n=== AUTENTIFICARE LIBRARIAN ===");
        System.out.println("(Pentru test: username: admin, parola: admin123)");
        System.out.println("(Pentru test: username: maria, parola: parola123)");

        String username = MenuUtils.readString("Username: ");
        String password = MenuUtils.readString("Parola: ");

        currentToken = librarianService.login(username, password);
        return currentToken != null;
    }

    private static void logout() {
        if (currentToken != null) {
            librarianService.logout(currentToken);
            currentToken = null;
        }
    }

    private static void showMyLog() {
        librarianService.printMyActionLog(currentToken);
    }

    private static void initializeSystem() {
        // Injectam service-urile in LibrarianService
        librarianService.injectServices(bookService, newspaperService, memberService,
                loanService, stockService, giftPackageService, eventService);

        // Cream conturi pentru test
        librarianService.registerLibrarian("Administrator", "admin@biblioteca.ro", "admin", "admin123");
        librarianService.registerLibrarian("Maria Popescu", "maria@biblioteca.ro", "maria", "parola123");

        LoanTemplateRegistry.getInstance();

        InitializeData initializer = new InitializeData(bookService, newspaperService,
                memberService, stockService, loanService, receiptMenu);
        initializer.initializeAll();

        System.out.println("Sistem initializat cu succes");
    }

    private static void initializeMenus() {
        bookMenu = new BookMenu(bookService, stockService, librarianService);
        newspaperMenu = new NewspaperMenu(newspaperService, librarianService);
        memberMenu = new MemberMenu(librarianService);
        loanMenu = new LoanMenu(loanService, bookService, memberService, stockService, librarianService);
        giftPackageMenu = new GiftPackageMenu(giftPackageService, bookService, stockService,
                memberService, librarianService);
        eventMenu = new EventMenu(librarianService);
        receiptMenu = new ReceiptMenu();
    }

    private static void printMainMenu() {
        System.out.println("\n====================================");
        System.out.println("   LIBRARY MANAGEMENT SYSTEM");
        System.out.println("====================================");
        System.out.println(" 1. Carti");
        System.out.println(" 2. Ziare");
        System.out.println(" 3. Membri");
        System.out.println(" 4. Imprumuturi");
        System.out.println(" 5. Pachete Cadou");
        System.out.println(" 6. Toate itemele");
        System.out.println(" 7. Evenimente");
        System.out.println(" 8. Chitante si Plati");
        System.out.println(" 9. Istoric actiuni personale");
        System.out.println(" 0. Iesire");
        System.out.println("====================================");
    }

    private static void listAllItems() {
        List<LibraryItem> allItems = new ArrayList<>();
        allItems.addAll(bookService.getAllItems());
        allItems.addAll(newspaperService.getAllItems());

        if (allItems.isEmpty()) {
            System.out.println("Nu exista iteme");
            return;
        }

        System.out.println("\nTOATE ITEMELE DIN BIBLIOTECA:");
        System.out.println("ID  | Tip                   | Titlu");
        System.out.println("----+-----------------------+----------------------");

        for (LibraryItem item : allItems) {
            String tip = item.getClass().getSimpleName();
            System.out.printf("%-3d | %-21s | %s\n",
                    item.getItemId(),
                    MenuUtils.truncate(tip, 21),
                    MenuUtils.truncate(item.getTitle(), 30));
        }
    }
}