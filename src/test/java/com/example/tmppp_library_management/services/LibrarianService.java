package com.example.tmppp_library_management.services;

import com.example.tmppp_library_management.builder.GiftPackage;
import com.example.tmppp_library_management.book.Author;
import com.example.tmppp_library_management.book.Book;
import com.example.tmppp_library_management.book.FantasyBook;
import com.example.tmppp_library_management.book.RomanceBook;
import com.example.tmppp_library_management.builder.GiftPackageService;
import com.example.tmppp_library_management.composite.EventService;
import com.example.tmppp_library_management.composite.SingleEvent;
import com.example.tmppp_library_management.entity.Loan;
import com.example.tmppp_library_management.interfaces.IBorrowable;
import com.example.tmppp_library_management.newspaper.LocalNewspaper;
import com.example.tmppp_library_management.newspaper.NationalNewspaper;
import com.example.tmppp_library_management.user.Librarian;
import com.example.tmppp_library_management.user.Member;
import com.example.tmppp_library_management.user.MemberType;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class LibrarianService {
    private static LibrarianService instance;
    private Map<String, Librarian> librarians; // key: username
    private Map<String, String> sessionTokens; // key: token, value: username
    private int nextLibrarianId;

    // Service-uri injectate
    private BookService bookService;
    private NewspaperService newspaperService;
    private MemberService memberService;
    private LoanService loanService;
    private StockService stockService;
    private GiftPackageService giftPackageService;
    private EventService eventService;

    private LibrarianService() {
        this.librarians = new HashMap<>();
        this.sessionTokens = new HashMap<>();
        this.nextLibrarianId = 1;
    }

    public static LibrarianService getInstance() {
        if (instance == null) {
            instance = new LibrarianService();
        }
        return instance;
    }

    // Injectare servicii
    public void injectServices(BookService bookService, NewspaperService newspaperService,
                               MemberService memberService, LoanService loanService,
                               StockService stockService, GiftPackageService giftPackageService,
                               EventService eventService) {
        this.bookService = bookService;
        this.newspaperService = newspaperService;
        this.memberService = memberService;
        this.loanService = loanService;
        this.stockService = stockService;
        this.giftPackageService = giftPackageService;
        this.eventService = eventService;

        registerLibrarian("Administrator", "admin@biblioteca.ro", "admin", "admin123");
        registerLibrarian("Maria Popescu", "maria@biblioteca.ro", "maria", "parola123");
    }

    // ============ AUTENTIFICARE ============

    public String login(String username, String password) {
        System.out.println("DEBUG: Caut librarian cu username: " + username);
        System.out.println("DEBUG: Librarians disponibili: " + librarians.keySet());

        Librarian librarian = librarians.get(username);
        if (librarian == null) {
            System.out.println("DEBUG: Librarian negasit!");
            return null;
        }

        System.out.println("DEBUG: Parola din sistem: " + librarian.getPassword());
        System.out.println("DEBUG: Parola introdusa: " + password);

        if (!librarian.getPassword().equals(password)) {
            System.out.println("DEBUG: Parola gresita!");
            return null;
        }

        String token = generateToken();
        sessionTokens.put(token, username);
        librarian.setLastLogin(LocalDateTime.now());
        librarian.addToLog("S-a autentificat in sistem");

        System.out.println("DEBUG: Autentificare reusita pentru: " + username);
        return token;
    }

    public void logout(String token) {
        String username = sessionTokens.remove(token);
        if (username != null) {
            Librarian librarian = librarians.get(username);
            if (librarian != null) {
                librarian.addToLog("S-a deconectat din sistem");
            }
        }
        System.out.println("✓ Delogare cu succes!");
    }

    public Librarian getCurrentLibrarian(String token) {
        String username = sessionTokens.get(token);
        return username != null ? librarians.get(username) : null;
    }

    public boolean isAuthenticated(String token) {
        return sessionTokens.containsKey(token);
    }

    public void addToLog(String token, String action) {
        Librarian librarian = getCurrentLibrarian(token);
        if (librarian != null) {
            librarian.addToLog(action);
        }
    }

    // ============ GESTIONARE LIBRARIENI ============

    public Librarian registerLibrarian(String name, String email, String username, String password) {
        if (librarians.containsKey(username)) {
            System.out.println("✗ Username-ul exista deja!");
            return null;
        }

        Librarian librarian = new Librarian(nextLibrarianId++, name, email, username, password);
        librarians.put(username, librarian);
        librarian.addToLog("Cont creat");

        System.out.println("✓ Librarian inregistrat: " + username);
        return librarian;
    }

    public void listAllLibrarians() {
        if (librarians.isEmpty()) {
            System.out.println("Nu exista librarieni inregistrati.");
            return;
        }

        System.out.println("\n--- LISTA LIBRARIENI ---");
        for (Librarian lib : librarians.values()) {
            System.out.println("  ID: " + lib.getUserId() +
                    " | Nume: " + lib.getUserName() +
                    " | Username: " + lib.getUsername() +
                    " | Ultimul login: " + (lib.getLastLogin() != null ? lib.getLastLogin() : "niciodata"));
        }
    }

    public void printMyActionLog(String token) {
        Librarian librarian = getCurrentLibrarian(token);
        if (librarian == null) return;
        librarian.printActionLog();
    }

    // ============ OPERATII MEMBRI ============

    public Member registerMember(String token, String name, String email, MemberType type) {
        Librarian librarian = getCurrentLibrarian(token);
        if (librarian == null) return null;

        Member member = memberService.addMember(name, email, type);
        librarian.addToLog("A inregistrat membrul " + member.getUserName() +
                " (ID: " + member.getUserId() + ", Tip: " + type + ")");
        return member;
    }

    public void listAllMembers(String token) {
        Librarian librarian = getCurrentLibrarian(token);
        if (librarian == null) return;

        memberService.listAllMembers();
        librarian.addToLog("A listat toti membrii");
    }

    public Member findMember(String token, int memberId) {
        Librarian librarian = getCurrentLibrarian(token);
        if (librarian == null) return null;

        Member member = memberService.getMember(memberId);
        librarian.addToLog("A cautat membrul cu ID " + memberId +
                (member != null ? " - gasit: " + member.getUserName() : " - negasit"));
        return member;
    }

    public Member findMemberByMembership(String token, String membership) {
        Librarian librarian = getCurrentLibrarian(token);
        if (librarian == null) return null;

        Member member = memberService.getMemberByMembership(membership);
        librarian.addToLog("A cautat membrul cu membership " + membership +
                (member != null ? " - gasit: " + member.getUserName() : " - negasit"));
        return member;
    }

    public boolean updateMemberType(String token, int memberId, MemberType newType) {
        Librarian librarian = getCurrentLibrarian(token);
        if (librarian == null) return false;

        Member member = memberService.getMember(memberId);
        if (member != null) {
            member.setMemberType(newType);
            librarian.addToLog("A actualizat tipul membrului " + member.getUserName() +
                    " (ID: " + memberId + ") la " + newType);
            return true;
        }
        return false;
    }

    public void deleteMember(String token, int memberId) {
        Librarian librarian = getCurrentLibrarian(token);
        if (librarian == null) return;

        Member member = memberService.getMember(memberId);
        if (member != null) {
            memberService.getAllMembers().remove(member);
            librarian.addToLog("A sters membrul " + member.getUserName() + " (ID: " + memberId + ")");
            System.out.println("✓ Membru sters cu succes");
        } else {
            System.out.println("Membru negasit");
        }
    }

    // ============ OPERATII CARTI ============

    public FantasyBook addFantasyBook(String token, int itemId, String title, int year, int pages,
                                      Author author, String isbn, String publisher, double price) {
        Librarian librarian = getCurrentLibrarian(token);
        if (librarian == null) return null;

        FantasyBook book = bookService.createFantasyBook(itemId, title, year, pages, author, isbn, publisher, price);
        librarian.addToLog("A adaugat cartea fantasy: " + title + " (ISBN: " + isbn + ", pret: " + price + " lei)");
        return book;
    }

    public RomanceBook addRomanceBook(String token, int itemId, String title, int year, int pages,
                                      Author author, String isbn, String publisher,
                                      int level, String tropes, double price) {
        Librarian librarian = getCurrentLibrarian(token);
        if (librarian == null) return null;

        RomanceBook book = bookService.createRomanceBook(itemId, title, year, pages, author, isbn, publisher, level, tropes, price);
        librarian.addToLog("A adaugat cartea romance: " + title + " (ISBN: " + isbn + ")");
        return book;
    }

    // ============ OPERATII CARTI - STERGERE ============

    public boolean deleteBook(String token, int itemId) {
        Librarian librarian = getCurrentLibrarian(token);
        if (librarian == null) return false;

        Book book = findBookById(itemId);
        if (book == null) return false;

        boolean deleted = bookService.deleteBook(itemId);
        if (deleted) {
            librarian.addToLog("A sters cartea: " + book.getTitle() + " (ISBN: " + book.getIsbn() + ")");
        }
        return deleted;
    }

    public boolean deleteBookByIsbn(String token, String isbn) {
        Librarian librarian = getCurrentLibrarian(token);
        if (librarian == null) return false;

        Book book = findBookByIsbn(isbn);
        if (book == null) return false;

        boolean deleted = bookService.deleteBookByIsbn(isbn);
        if (deleted) {
            librarian.addToLog("A sters cartea: " + book.getTitle() + " (ISBN: " + isbn + ")");
        }
        return deleted;
    }

    public Book findBookById(int itemId) {
        if (bookService == null) return null;
        java.util.List<Book> allBooks = bookService.findAllBooksLegacy();
        for (Book book : allBooks) {
            if (book.getItemId() == itemId) {
                return book;
            }
        }
        return null;
    }

    public Book findBookByIsbn(String isbn) {
        if (bookService == null) return null;
        java.util.List<Book> allBooks = bookService.findAllBooksLegacy();
        for (Book book : allBooks) {
            if (book.getIsbn().equals(isbn)) {
                return book;
            }
        }
        return null;
    }

    // ============ OPERATII ZIARE ============

    public LocalNewspaper addLocalNewspaper(String token, int itemId, String title, int year, int pages,
                                            String publisher, String issn, String city, String region) {
        Librarian librarian = getCurrentLibrarian(token);
        if (librarian == null) return null;

        LocalNewspaper newspaper = newspaperService.createLocalNewspaper(itemId, title, year, pages,
                publisher, issn, city, region);
        librarian.addToLog("A adaugat ziarul local: " + title + " (ISSN: " + issn + ")");
        return newspaper;
    }

    public NationalNewspaper addNationalNewspaper(String token, int itemId, String title, int year, int pages,
                                                  String publisher, String issn, String area, String orientation) {
        Librarian librarian = getCurrentLibrarian(token);
        if (librarian == null) return null;

        NationalNewspaper newspaper = newspaperService.createNationalNewspaper(itemId, title, year, pages,
                publisher, issn, area, orientation);
        librarian.addToLog("A adaugat ziarul national: " + title + " (ISSN: " + issn + ")");
        return newspaper;
    }

    // ============ OPERATII IMPRUMUTURI ============

    public Loan createLoan(String token, Member member, IBorrowable item) {
        Librarian librarian = getCurrentLibrarian(token);
        if (librarian == null) return null;

        Loan loan = loanService.createLoan(member, item);
        librarian.addToLog("A creat imprumut pentru " + member.getUserName() +
                " - item: " + (item instanceof Book ? ((Book)item).getTitle() : "Decorated Book"));
        return loan;
    }

    public void closeLoan(String token, Loan loan) {
        Librarian librarian = getCurrentLibrarian(token);
        if (librarian == null) return;

        loanService.closeLoan(loan);
        librarian.addToLog("A inchis imprumutul ID " + loan.getLoanId() +
                " pentru cartea: " + loan.getBookTitle());
    }

    public void showAllTemplates(String token) {
        Librarian librarian = getCurrentLibrarian(token);
        if (librarian == null) return;

        loanService.showTemplates();
        librarian.addToLog("A afisat template-urile de imprumut");
    }

    // ============ OPERATII PACHETE CADOU ============

    public GiftPackage createGiftPackage(String token, String type, Book book, String message) {
        Librarian librarian = getCurrentLibrarian(token);
        if (librarian == null) return null;

        GiftPackage gift = giftPackageService.createGiftPackage(type, book, message);
        librarian.addToLog("A creat pachet cadou " + type + " - carte: " + book.getTitle());
        return gift;
    }

    // ============ OPERATII EVENIMENTE ============

    public SingleEvent createEvent(String token, String name, String date, String location, String type) {
        Librarian librarian = getCurrentLibrarian(token);
        if (librarian == null) return null;

        SingleEvent event = eventService.createEvent(name, date, location, type);
        librarian.addToLog("A creat evenimentul: " + name);
        return event;
    }

    // ============ UTILITARE ============

    private String generateToken() {
        return java.util.UUID.randomUUID().toString();
    }

    public IBorrowable addReadingRoomRestriction(String token, Book book, String room) {
        Librarian librarian = getCurrentLibrarian(token);
        if (librarian == null) return null;

        IBorrowable restricted = bookService.addReadingRoomRestriction(book, room);
        librarian.addToLog("A adaugat restrictie 'doar in sala " + room + "' pentru cartea " + book.getTitle());
        return restricted;
    }

    public IBorrowable addRestrictedAccess(String token, Book book) {
        Librarian librarian = getCurrentLibrarian(token);
        if (librarian == null) return null;

        IBorrowable restricted = bookService.addRestrictedAccess(book, MemberType.PROFESSOR);
        librarian.addToLog("A adaugat restrictie 'acces restrictionat' pentru cartea " + book.getTitle());
        return restricted;
    }

    public IBorrowable addApprovalRequired(String token, Book book) {
        Librarian librarian = getCurrentLibrarian(token);
        if (librarian == null) return null;

        IBorrowable restricted = bookService.addApprovalRequired(book);
        librarian.addToLog("A adaugat restrictie 'necesita aprobare' pentru cartea " + book.getTitle());
        return restricted;
    }

    public IBorrowable addFullRestrictions(String token, Book book, String room) {
        Librarian librarian = getCurrentLibrarian(token);
        if (librarian == null) return null;

        IBorrowable restricted = bookService.addFullRestrictions(book, room);
        librarian.addToLog("A adaugat toate restrictiile pentru cartea " + book.getTitle());
        return restricted;
    }

}