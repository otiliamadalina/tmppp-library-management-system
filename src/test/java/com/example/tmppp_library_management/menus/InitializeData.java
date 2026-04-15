package com.example.tmppp_library_management.menus;

import com.example.tmppp_library_management.book.Author;
import com.example.tmppp_library_management.book.Book;
import com.example.tmppp_library_management.composite.EventGroup;
import com.example.tmppp_library_management.composite.EventService;
import com.example.tmppp_library_management.composite.SingleEvent;
import com.example.tmppp_library_management.decorator.BookDecorator;
import com.example.tmppp_library_management.entity.Loan;
import com.example.tmppp_library_management.entity.Payment;
import com.example.tmppp_library_management.interfaces.IBorrowable;
import com.example.tmppp_library_management.user.Member;
import com.example.tmppp_library_management.user.MemberType;
import com.example.tmppp_library_management.services.*;
import com.example.tmppp_library_management.flyweight.PublisherFactory;
import com.example.tmppp_library_management.templateMethod.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InitializeData {

    private final BookService bookService;
    private final NewspaperService newspaperService;
    private final MemberService memberService;
    private final StockService stockService;
    private final LoanService loanService;
    private final PaymentService paymentService;
    private final ReceiptMenu receiptMenu;

    private int nextItemId;
    private int nextAuthorId;
    private final int nextMemberId;

    public InitializeData(BookService bookService, NewspaperService newspaperService,
                          MemberService memberService, StockService stockService,
                          LoanService loanService, ReceiptMenu receiptMenu) {
        this.bookService = bookService;
        this.newspaperService = newspaperService;
        this.memberService = memberService;
        this.stockService = stockService;
        this.loanService = loanService;
        this.paymentService = PaymentService.getInstance();
        this.receiptMenu = receiptMenu;

        this.nextItemId = 100;
        this.nextAuthorId = 1;
        this.nextMemberId = 1;
    }

    public void initializeAll() {
        initializeTestData();
        initializeStock();
        initializeMembers();
        initializeLoans();
        initializePayments();
        initializeEvents();
        initializeFlyweightTest();
        initializeReceipts();
    }

    private void initializeTestData() {
        Author author1 = new Author(nextAuthorId++, "Frank Herbert");
        Author author2 = new Author(nextAuthorId++, "Mihai Eminescu");
        Author author3 = new Author(nextAuthorId++, "J.R.R. Tolkien");

        bookService.createFantasyBook(nextItemId++, "Dune", 1965, 500, author1, "123-456-789-012", "Chilton Books", 45.0);
        bookService.createRomanceBook(nextItemId++, "Iubire in Paris", 2024, 300, author2, "234-567-890-123", "Romance Pub", 4, "enemies-to-lovers", 35.0);
        bookService.createFantasyBook(nextItemId++, "Stapanul Inelelor", 1954, 1000, author3, "345-678-901-234", "Allen & Unwin", 60.0);

        newspaperService.createLocalNewspaper(nextItemId++, "Gazeta de Cluj", 2024, 20, "Editura Cluj", "1234-5678", "Cluj-Napoca", "Transilvania");
        newspaperService.createNationalNewspaper(nextItemId++, "Adevarul", 2024, 30, "Adevarul Holding", "8765-4321", "national", "centru-dreapta");
    }

    private void initializeFlyweightTest() {
        Author testAuthor = new Author(nextAuthorId++, "Ion Creanga");
        PublisherFactory factory = PublisherFactory.getInstance();

        bookService.createFantasyBook(nextItemId++, "Amintiri din copilarie", 1892, 200,
                testAuthor, "456-789-012-345", "Humanitas", 25.0);
        bookService.createFantasyBook(nextItemId++, "Povestea lui Harap-Alb", 1894, 150,
                testAuthor, "567-890-123-456", "Humanitas", 25.0);
        bookService.createRomanceBook(nextItemId++, "Frumoasa din padurea adormita", 1890, 180,
                testAuthor, "678-901-234-567", "Humanitas", 3, "basme", 28.0);

        Author modernAuthor = new Author(nextAuthorId++, "Mircea Cartarescu");
        bookService.createFantasyBook(nextItemId++, "Solenoid", 2015, 800,
                modernAuthor, "789-012-345-678", "Polirom", 55.0);
        bookService.createFantasyBook(nextItemId++, "Nostalgia", 1993, 400,
                modernAuthor, "890-123-456-789", "Polirom", 40.0);

        newspaperService.createLocalNewspaper(nextItemId++, "Monitorul de Iasi", 2024, 16,
                "Mediafax", "9876-5432", "Iasi", "Moldova");
        newspaperService.createLocalNewspaper(nextItemId++, "Monitorul de Cluj", 2024, 16,
                "Mediafax", "9876-5433", "Cluj-Napoca", "Transilvania");
        newspaperService.createLocalNewspaper(nextItemId++, "Monitorul de Timisoara", 2024, 16,
                "Mediafax", "9876-5434", "Timisoara", "Banat");
    }

    private void initializeStock() {
        stockService.addStock("123-456-789-012", 5);
        stockService.addStock("234-567-890-123", 3);
        stockService.addStock("345-678-901-234", 4);

        stockService.addStock("456-789-012-345", 3);
        stockService.addStock("567-890-123-456", 3);
        stockService.addStock("678-901-234-567", 3);
        stockService.addStock("789-012-345-678", 2);
        stockService.addStock("890-123-456-789", 2);

        stockService.addStock("greeting_card", 20);
        stockService.addStock("packaging_standard", 15);
        stockService.addStock("packaging_premium", 8);
        stockService.addStock("ribbon", 12);
        stockService.addStock("gift_tag", 10);

        stockService.addStock("9876-5432", 10);
        stockService.addStock("9876-5433", 10);
        stockService.addStock("9876-5434", 10);
    }

    private void initializeMembers() {
        memberService.addMember("Ion Popescu", "ion.popescu@email.com", MemberType.SIMPLE);
        memberService.addMember("Maria Ionescu", "maria.ionescu@email.com", MemberType.STUDENT);
        memberService.addMember("Prof. George Vasile", "george.vasile@univ.ro", MemberType.PROFESSOR);
        memberService.addMember("Ana Marinescu", "ana.marinescu@email.com", MemberType.SIMPLE);
        memberService.addMember("Andrei Popa", "andrei.popa@student.ro", MemberType.STUDENT);
    }

    private Book getBookFromItem(IBorrowable item) {
        if (item instanceof Book) {
            return (Book) item;
        } else if (item instanceof BookDecorator) {
            return ((BookDecorator) item).getOriginalBook();
        }
        return null;
    }

    private void initializeLoans() {
        List<IBorrowable> items = bookService.findAllBooks();
        List<Member> members = memberService.getAllMembers();

        if (items.isEmpty() || members.isEmpty()) {
            return;
        }

        List<Book> books = new ArrayList<>();
        for (IBorrowable item : items) {
            if (item instanceof Book && !(item instanceof BookDecorator)) {
                books.add((Book) item);
            }
        }

        if (books.isEmpty()) {
            return;
        }

        // Imprumut activ #1
        if (!members.isEmpty() && !books.isEmpty()) {
            try {
                Loan loan1 = loanService.createLoan(members.get(0), books.get(0));
                loan1.setStartDate(LocalDate.now().minusDays(5));
                loan1.setReturnDate(LocalDate.now().plusDays(9));
                loan1.setActive(true);
            } catch (Exception ignored) {}
        }

        // Imprumut activ #2
        if (members.size() > 1 && books.size() > 1) {
            try {
                Loan loan2 = loanService.createLoan(members.get(1), books.get(1));
                loan2.setStartDate(LocalDate.now().minusDays(3));
                loan2.setReturnDate(LocalDate.now().plusDays(18));
                loan2.setActive(true);
            } catch (Exception ignored) {}
        }

        // Imprumut activ #3
        if (members.size() > 2 && books.size() > 2) {
            try {
                Loan loan3 = loanService.createLoan(members.get(2), books.get(2));
                loan3.setStartDate(LocalDate.now().minusDays(10));
                loan3.setReturnDate(LocalDate.now().plusDays(20));
                loan3.setActive(true);
            } catch (Exception ignored) {}
        }

        // Imprumut inchis #4
        if (members.size() > 3 && !books.isEmpty()) {
            try {
                Loan loan4 = loanService.createLoan(members.get(3), books.get(0));
                loan4.setStartDate(LocalDate.now().minusDays(30));
                loan4.setReturnDate(LocalDate.now().minusDays(16));
                loan4.setActive(false);
                loanService.closeLoan(loan4);
            } catch (Exception ignored) {}
        }

        // Imprumut inchis #5
        if (members.size() > 4 && books.size() > 1) {
            try {
                Loan loan5 = loanService.createLoan(members.get(4), books.get(1));
                loan5.setStartDate(LocalDate.now().minusDays(25));
                loan5.setReturnDate(LocalDate.now().minusDays(4));
                loan5.setActive(false);
                loanService.closeLoan(loan5);
            } catch (Exception ignored) {}
        }

        // Imprumut inchis #6
        if (members.size() > 1 && books.size() > 2) {
            try {
                Loan loan6 = loanService.createLoan(members.get(1), books.get(2));
                loan6.setStartDate(LocalDate.now().minusDays(40));
                loan6.setReturnDate(LocalDate.now().minusDays(19));
                loan6.setActive(false);
                loanService.closeLoan(loan6);
            } catch (Exception ignored) {}
        }
    }

    private void initializePayments() {
        paymentService.processPayment(5.0, "CASH", "Penalitate intarziere - Dune", 1);
        paymentService.processPayment(3.5, "CARD", "Penalitate intarziere - Iubire in Paris", 2);
        paymentService.processPayment(12.0, "CASH", "Penalitate intarziere - Stapanul Inelelor", 3);
        paymentService.processPayment(25.0, "CARD", "Pachet cadou standard", null);
        paymentService.processPayment(45.0, "CASH", "Pachet cadou premium", null);
    }

    private void initializeEvents() {
        EventService eventService = EventService.getInstance();

        SingleEvent e1 = eventService.createEvent("Lansarea cartii 'Ion' - Editie aniversara", "15.05.2024", "Sala Mare, Biblioteca Centrala", "lansare");
        SingleEvent e2 = eventService.createEvent("Club de lectura - Eminescu", "16.05.2024", "Sala Mica, Biblioteca Centrala", "club");
        SingleEvent e3 = eventService.createEvent("Club de lectura - Rebreanu", "23.05.2024", "Sala Mica, Biblioteca Centrala", "club");
        SingleEvent e4 = eventService.createEvent("Atelier de creatie pentru copii", "17.05.2024", "Atelierul de creatie", "atelier");
        SingleEvent e5 = eventService.createEvent("Expozitie 'Cartea veche'", "20.05.2024 - 30.05.2024", "Sala Expozitii", "expozitie");

        EventGroup clubSeries = eventService.createGroup("Clubul de lectura - Seria Primavara 2024");
        clubSeries.add(e2);
        clubSeries.add(e3);

        EventGroup festival = eventService.createGroup("Festivalul Cartii 2024");
        festival.add(e1);
        festival.add(clubSeries);
        festival.add(e4);
    }

    private void initializeReceipts() {

        List<Loan> activeLoans = loanService.getActiveLoans();
        if (!activeLoans.isEmpty()) {
            Loan firstLoan = activeLoans.get(0);
            LoanReceipt loanReceipt = new LoanReceipt(firstLoan);
            receiptMenu.saveReceiptToHistory(loanReceipt.generateReceipt());
        }

        List<Loan> allLoans = loanService.getAllLoans();
        Loan closedLoan = null;
        for (Loan loan : allLoans) {
            if (!loan.isActive()) {
                closedLoan = loan;
                break;
            }
        }

        if (closedLoan != null) {
            int daysLate = 0;
            double penalty = 0;
            if (LocalDate.now().isAfter(closedLoan.getReturnDate())) {
                daysLate = (int) java.time.temporal.ChronoUnit.DAYS.between(
                        closedLoan.getReturnDate(), LocalDate.now());
                penalty = daysLate * 1.0;
            }
            ReturnReceiptTemplate returnReceipt = new ReturnReceiptTemplate(closedLoan, LocalDate.now(), daysLate, penalty, penalty > 0);
            receiptMenu.saveReceiptToHistory(returnReceipt.generateReceipt());
        }

        List<Payment> allPayments = paymentService.getAllPayments();
        if (!allPayments.isEmpty()) {
            Payment firstPayment = allPayments.get(0);
            String memberName = "Membru ID: " + (firstPayment.getMemberId() != null ? firstPayment.getMemberId() : "Vizitator");
            PaymentReceipt paymentReceipt = new PaymentReceipt(firstPayment, memberName);
            receiptMenu.saveReceiptToHistory(paymentReceipt.generateReceipt());
        }
    }

    public int getNextItemId() {
        return nextItemId;
    }

    public int getNextAuthorId() {
        return nextAuthorId;
    }

    public int getNextMemberId() {
        return nextMemberId;
    }
}