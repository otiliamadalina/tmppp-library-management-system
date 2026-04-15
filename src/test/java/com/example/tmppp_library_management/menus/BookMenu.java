package com.example.tmppp_library_management.menus;

import com.example.tmppp_library_management.book.Author;
import com.example.tmppp_library_management.book.Book;
import com.example.tmppp_library_management.book.FantasyBook;
import com.example.tmppp_library_management.book.RomanceBook;
import com.example.tmppp_library_management.services.BookService;
import com.example.tmppp_library_management.services.LibrarianService;
import com.example.tmppp_library_management.services.StockService;
import com.example.tmppp_library_management.interfaces.IBorrowable;
import com.example.tmppp_library_management.decorator.BookDecorator;

import java.util.List;

public class BookMenu {
    private final BookService bookService;
    private final StockService stockService;
    private final LibrarianService librarianService;
    private int nextItemId = 100;

    public BookMenu(BookService bookService, StockService stockService, LibrarianService librarianService) {
        this.bookService = bookService;
        this.stockService = stockService;
        this.librarianService = librarianService;
    }

    public void display(String token) {
        while (true) {
            System.out.println("\n--- MENIU CARTI ---");
            System.out.println("1. Listeaza toate cartile");
            System.out.println("2. Adauga carte Fantasy");
            System.out.println("3. Adauga carte Romance");
            System.out.println("4. Cauta carte dupa ISBN");
            System.out.println("5. Cauta carte dupa titlu");
            System.out.println("6. Adauga restrictii carte");
            System.out.println("0. Inapoi");

            int choice = MenuUtils.readInt("Alege optiunea: ");

            switch (choice) {
                case 1 -> listAllBooks();
                case 2 -> addFantasyBook(token);
                case 3 -> addRomanceBook(token);
                case 4 -> searchByIsbn();
                case 5 -> searchByTitle();
                case 6 -> addRestrictionsMenu(token);
                case 0 -> { return; }
                default -> System.out.println("Optiune invalida!");
            }
        }
    }

    public void listAllBooksSimple() {
        listAllBooks();
    }

    private void listAllBooks() {
        List<Book> books = bookService.findAllBooksLegacy();

        if (books.isEmpty()) {
            System.out.println("Nu exista carti in biblioteca.");
            return;
        }

        System.out.println("\n--- LISTA CARTI ---");
        System.out.println("ID  | Titlu                     | Autor            | ISBN           | Pret");
        System.out.println("----+---------------------------+------------------+----------------+-------");

        for (Book book : books) {
            System.out.printf("%-3d | %-25s | %-16s | %-14s | %.2f lei\n",
                    book.getItemId(),
                    MenuUtils.truncate(book.getTitle(), 25),
                    MenuUtils.truncate(book.getAuthor().getName(), 16),
                    book.getIsbn(),
                    book.getPrice());
        }

        // Afisam si cartile cu decoratori
        List<IBorrowable> allItems = bookService.findAllBooks();
        List<IBorrowable> decoratedBooks = allItems.stream()
                .filter(item -> item instanceof BookDecorator)
                .toList();

        if (!decoratedBooks.isEmpty()) {
            System.out.println("\n--- CARTI CU RESTRICTII ---");
            for (IBorrowable item : decoratedBooks) {
                BookDecorator decorator = (BookDecorator) item;
                Book original = decorator.getOriginalBook();
                System.out.printf("ID: %d | %s | RESTRICTII: %s\n",
                        original.getItemId(),
                        MenuUtils.truncate(original.getTitle(), 30),
                        decorator.getDescription());
            }
        }
    }

    // ============ METODELE CORECTATE CU VALIDARE ============

    private void addFantasyBook(String token) {
        System.out.println("\n--- ADAUGA CARTE FANTASY ---");

        // ✅ Folosim validare pentru titlu
        String title = MenuUtils.readValidatedTitle("Titlu: ");
        if (title == null) {
            System.out.println("Operatiune anulata.");
            return;
        }

        // ✅ Folosim validare pentru an
        String yearStr = MenuUtils.readValidatedYear("An: ");
        if (yearStr == null) {
            System.out.println("Operatiune anulata.");
            return;
        }
        int year = Integer.parseInt(yearStr);

        int pages = MenuUtils.readInt("Numar pagini: ");

        // ✅ Folosim validare pentru nume autor
        String authorName = MenuUtils.readValidatedName("Nume autor: ");
        if (authorName == null) {
            System.out.println("Operatiune anulata.");
            return;
        }
        Author author = new Author(0, authorName);

        String publisher = MenuUtils.readString("Editura: ");

        // ✅ Folosim validare pentru ISBN (cu verificare duplicat)
        String isbn = MenuUtils.readValidatedIsbn("ISBN: ");
        if (isbn == null) {
            System.out.println("Operatiune anulata.");
            return;
        }

        double price = MenuUtils.readDouble("Pret (lei): ");

        FantasyBook book = librarianService.addFantasyBook(
                token, nextItemId++, title, year, pages, author, isbn, publisher, price);
        System.out.println("✓ Carte Fantasy adaugata cu ID: " + book.getItemId());
    }

    private void addRomanceBook(String token) {
        System.out.println("\n--- ADAUGA CARTE ROMANCE ---");

        // ✅ Folosim validare pentru titlu
        String title = MenuUtils.readValidatedTitle("Titlu: ");
        if (title == null) {
            System.out.println("Operatiune anulata.");
            return;
        }

        // ✅ Folosim validare pentru an
        String yearStr = MenuUtils.readValidatedYear("An: ");
        if (yearStr == null) {
            System.out.println("Operatiune anulata.");
            return;
        }
        int year = Integer.parseInt(yearStr);

        int pages = MenuUtils.readInt("Numar pagini: ");

        // ✅ Folosim validare pentru nume autor
        String authorName = MenuUtils.readValidatedName("Nume autor: ");
        if (authorName == null) {
            System.out.println("Operatiune anulata.");
            return;
        }
        Author author = new Author(0, authorName);

        String publisher = MenuUtils.readString("Editura: ");

        // ✅ Folosim validare pentru ISBN (cu verificare duplicat)
        String isbn = MenuUtils.readValidatedIsbn("ISBN: ");
        if (isbn == null) {
            System.out.println("Operatiune anulata.");
            return;
        }

        int level = MenuUtils.readInt("Nivel spicy (1-5): ");
        String tropes = MenuUtils.readString("Trope-uri: ");
        double price = MenuUtils.readDouble("Pret (lei): ");

        RomanceBook book = librarianService.addRomanceBook(
                token, nextItemId++, title, year, pages, author, isbn, publisher, level, tropes, price);
        System.out.println("✓ Carte Romance adaugata cu ID: " + book.getItemId());
    }

    private void searchByIsbn() {
        String isbn = MenuUtils.readString("ISBN carte: ");

        List<Book> allBooks = bookService.findAllBooksLegacy();
        Book foundBook = null;

        for (Book book : allBooks) {
            if (book.getIsbn().equals(isbn)) {
                foundBook = book;
                break;
            }
        }

        if (foundBook == null) {
            List<IBorrowable> allItems = bookService.findAllBooks();
            for (IBorrowable item : allItems) {
                if (item instanceof BookDecorator) {
                    BookDecorator decorator = (BookDecorator) item;
                    Book original = decorator.getOriginalBook();
                    if (original.getIsbn().equals(isbn)) {
                        foundBook = original;
                        break;
                    }
                }
            }
        }

        if (foundBook == null) {
            System.out.println("Nu s-a gasit nicio carte cu ISBN-ul: " + isbn);
            return;
        }

        displayBookDetails(foundBook);
    }

    private void searchByTitle() {
        String title = MenuUtils.readString("Titlu carte: ");
        List<Book> books = bookService.findBooksByTitleLegacy(title);

        if (books == null || books.isEmpty()) {
            System.out.println("Nu s-au gasit carti cu titlul: " + title);
            return;
        }

        System.out.println("\n--- REZULTATE CAUTARE ---");
        for (Book book : books) {
            displayBookSummary(book);
        }
    }

    private void addRestrictionsMenu(String token) {
        System.out.println("\n--- ADAUGA RESTRICTII CARTE ---");
        String isbn = MenuUtils.readString("ISBN carte: ");

        List<Book> allBooks = bookService.findAllBooksLegacy();
        Book bookToRestrict = null;

        for (Book book : allBooks) {
            if (book.getIsbn().equals(isbn)) {
                bookToRestrict = book;
                break;
            }
        }

        if (bookToRestrict == null) {
            System.out.println("Carte negasita!");
            return;
        }

        System.out.println("\nCarte: " + bookToRestrict.getTitle());
        System.out.println("1. Doar in sala de lectura");
        System.out.println("2. Acces restrictionat (doar profesori)");
        System.out.println("3. Necesita aprobare speciala");
        System.out.println("4. Toate restrictiile");

        int choice = MenuUtils.readInt("Alege: ");

        switch (choice) {
            case 1 -> {
                String room = MenuUtils.readString("Nume sala: ");
                librarianService.addReadingRoomRestriction(token, bookToRestrict, room);
                System.out.println("✓ Restrictie adaugata: doar in sala " + room);
            }
            case 2 -> {
                librarianService.addRestrictedAccess(token, bookToRestrict);
                System.out.println("✓ Restrictie adaugata: acces doar pentru profesori");
            }
            case 3 -> {
                librarianService.addApprovalRequired(token, bookToRestrict);
                System.out.println("✓ Restrictie adaugata: necesita aprobare");
            }
            case 4 -> {
                String room = MenuUtils.readString("Nume sala: ");
                librarianService.addFullRestrictions(token, bookToRestrict, room);
                System.out.println("✓ Toate restrictiile adaugate");
            }
            default -> System.out.println("Optiune invalida");
        }
    }

    private void displayBookDetails(Book book) {
        System.out.println("\n--- DETALII CARTE ---");
        System.out.println("ID: " + book.getItemId());
        System.out.println("Titlu: " + book.getTitle());
        System.out.println("An: " + book.getPublicationDate());
        System.out.println("Pagini: " + book.getPageCount());
        System.out.println("Autor: " + book.getAuthor().getName());
        System.out.println("Editura: " + book.getPublisher().getName());
        System.out.println("ISBN: " + book.getIsbn());
        System.out.println("Pret: " + book.getPrice() + " lei");

        if (book instanceof FantasyBook) {
            System.out.println("Gen: Fantasy");
        } else if (book instanceof RomanceBook romance) {
            System.out.println("Gen: Romance");
            System.out.println("Nivel spicy: " + romance.getRomanceLevel());
            System.out.println("Trope-uri: " + romance.getTropes());
        }
    }

    private void displayBookSummary(Book book) {
        System.out.printf("ID: %d | %s | %s | %s | %.2f lei\n",
                book.getItemId(),
                MenuUtils.truncate(book.getTitle(), 20),
                book.getAuthor().getName(),
                book.getIsbn(),
                book.getPrice());
    }
}