package com.example.tmppp_library_management.menus;

import com.example.tmppp_library_management.book.Author;
import com.example.tmppp_library_management.book.Book;
import com.example.tmppp_library_management.book.FantasyBook;
import com.example.tmppp_library_management.book.RomanceBook;
import com.example.tmppp_library_management.builder.*;
import com.example.tmppp_library_management.composite.EventGroup;
import com.example.tmppp_library_management.composite.EventService;
import com.example.tmppp_library_management.composite.SingleEvent;
import com.example.tmppp_library_management.decorator.BookDecorator;
import com.example.tmppp_library_management.entity.Loan;
import com.example.tmppp_library_management.entity.Payment;
import com.example.tmppp_library_management.entity.Stock;
import com.example.tmppp_library_management.interfaces.IBorrowable;
import com.example.tmppp_library_management.ui.LibraryDashboard;
import com.example.tmppp_library_management.user.Member;
import com.example.tmppp_library_management.user.MemberType;
import com.example.tmppp_library_management.services.*;
import com.example.tmppp_library_management.flyweight.PublisherFactory;
import com.example.tmppp_library_management.templateMethod.*;

import javax.swing.*;
import java.awt.*;
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
    private final GiftPackageService giftPackageService;

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
        this.giftPackageService = new GiftPackageService(stockService);

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
        initializeGiftPackages();
    }

    private void initializeTestData() {
        Author author1 = new Author(nextAuthorId++, "Frank Herbert");
        Author author2 = new Author(nextAuthorId++, "Mihai Eminescu");
        Author author3 = new Author(nextAuthorId++, "J.R.R. Tolkien");
        Author author4 = new Author(nextAuthorId++, "George R.R. Martin");
        Author author5 = new Author(nextAuthorId++, "Jane Austen");
        Author author6 = new Author(nextAuthorId++, "Carlos Ruiz Zafon");
        Author author7 = new Author(nextAuthorId++, "Gabriel Garcia Marquez");
        Author author8 = new Author(nextAuthorId++, "Haruki Murakami");
        Author author9 = new Author(nextAuthorId++, "Agatha Christie");
        Author author10 = new Author(nextAuthorId++, "Stephen King");
        Author author11 = new Author(nextAuthorId++, "Dan Brown");
        Author author12 = new Author(nextAuthorId++, "J.K. Rowling");
        Author author13 = new Author(nextAuthorId++, "Leo Tolstoy");
        Author author14 = new Author(nextAuthorId++, "Fyodor Dostoevsky");
        Author author15 = new Author(nextAuthorId++, "Ernest Hemingway");

        bookService.createFantasyBook(nextItemId++, "Dune", 1965, 500, author1, "123-456-789-012", "Chilton Books", 45.0);
        bookService.createRomanceBook(nextItemId++, "Iubire in Paris", 2024, 300, author2, "234-567-890-123", "Romance Pub", 4, "enemies-to-lovers", 35.0);
        bookService.createFantasyBook(nextItemId++, "Stapanul Inelelor", 1954, 1000, author3, "345-678-901-234", "Allen & Unwin", 60.0);
        bookService.createFantasyBook(nextItemId++, "Jocul Tronurilor", 1996, 800, author4, "978-0-55-357340-0", "Bantam Books", 124.0);
        bookService.createRomanceBook(nextItemId++, "Mandrie si Prejudecata", 1813, 400, author5, "978-0-14-143951-8", "T. Egerton", 67, "slow-burn", 5);
        bookService.createFantasyBook(nextItemId++, "Umbra Vantului", 2001, 500, author6, "978-3-16-148410-0", "Planeta", 89.0);
        bookService.createFantasyBook(nextItemId++, "Un veac de singuratate", 1967, 450, author7, "978-0-06-088328-7", "Harper & Row", 78.0);
        bookService.createFantasyBook(nextItemId++, "Kafka pe malul marii", 2002, 600, author8, "978-1-40-004366-5", "Shinchosha", 95.0);
        bookService.createFantasyBook(nextItemId++, "Crima din Orient Express", 1934, 250, author9, "978-0-00-711931-8", "Collins Crime Club", 55.0);
        bookService.createFantasyBook(nextItemId++, "Strălucirea", 1977, 650, author10, "978-0-38-512167-5", "Doubleday", 110.0);
        bookService.createFantasyBook(nextItemId++, "Codul lui Da Vinci", 2003, 450, author11, "978-0-38-550420-5", "Doubleday", 85.0);
        bookService.createFantasyBook(nextItemId++, "Harry Potter si Piatra Filozofala", 1997, 350, author12, "978-0-74-753269-9", "Bloomsbury", 75.0);
        bookService.createFantasyBook(nextItemId++, "Harry Potter si Camera Secretelor", 1998, 380, author12, "978-0-74-753284-2", "Bloomsbury", 75.0);
        bookService.createFantasyBook(nextItemId++, "Razboi si pace", 1869, 1200, author13, "978-0-14-044417-9", "The Russian Messenger", 150.0);
        bookService.createFantasyBook(nextItemId++, "Crima si pedeapsa", 1866, 550, author14, "978-0-14-044913-6", "The Russian Messenger", 120.0);
        bookService.createFantasyBook(nextItemId++, "Batranul si marea", 1952, 150, author15, "978-0-68-480122-3", "Charles Scribner's Sons", 65.0);

        newspaperService.createLocalNewspaper(nextItemId++, "Gazeta de Cluj", 2024, 20, "Editura Cluj", "1234-5678", "Cluj-Napoca", "Transilvania");
        newspaperService.createNationalNewspaper(nextItemId++, "Adevarul", 2024, 30, "Adevarul Holding", "8765-4321", "national", "centru-dreapta");
        newspaperService.createLocalNewspaper(nextItemId++, "Monitorul de Iasi", 2024, 16, "Mediafax", "9876-5432", "Iasi", "Moldova");
        newspaperService.createLocalNewspaper(nextItemId++, "Monitorul de Cluj", 2024, 16, "Mediafax", "9876-5433", "Cluj-Napoca", "Transilvania");
        newspaperService.createLocalNewspaper(nextItemId++, "Monitorul de Timisoara", 2024, 16, "Mediafax", "9876-5434", "Timisoara", "Banat");
        newspaperService.createNationalNewspaper(nextItemId++, "Libertatea", 2024, 28, "Libertatea Media", "1111-2222", "national", "independent");
        newspaperService.createNationalNewspaper(nextItemId++, "Romania Libera", 2024, 32, "Romania Libera SRL", "3333-4444", "national", "centru-stanga");
        newspaperService.createLocalNewspaper(nextItemId++, "Ziarul de Duminica", 2024, 24, "Ziarul Media", "5555-6666", "Bucuresti", "Muntenia");
        newspaperService.createLocalNewspaper(nextItemId++, "Observatorul", 2024, 18, "Observator Media", "7777-8888", "Sibiu", "Transilvania");
        newspaperService.createNationalNewspaper(nextItemId++, "Evenimentul Zilei", 2024, 30, "Evenimentul Media", "9999-0000", "national", "centru");
        newspaperService.createLocalNewspaper(nextItemId++, "Cronica Romana", 2024, 22, "Cronica Media", "1111-3333", "Brasov", "Transilvania");
        newspaperService.createNationalNewspaper(nextItemId++, "Jurnalul National", 2024, 26, "Jurnalul Media", "4444-5555", "national", "centru-dreapta");
        newspaperService.createLocalNewspaper(nextItemId++, "Curierul National", 2024, 20, "Curierul Media", "6666-7777", "Constanta", "Dobrogea");
        newspaperService.createLocalNewspaper(nextItemId++, "Mesagerul", 2024, 14, "Mesagerul Media", "8888-9999", "Oradea", "Crisana");
    }

    private void initializeFlyweightTest() {
        Author testAuthor = new Author(nextAuthorId++, "Ion Creanga");
        Author testAuthor2 = new Author(nextAuthorId++, "Vasile Alecsandri");
        Author testAuthor3 = new Author(nextAuthorId++, "George Cosbuc");
        PublisherFactory factory = PublisherFactory.getInstance();

        bookService.createFantasyBook(nextItemId++, "Amintiri din copilarie", 1892, 200,
                testAuthor, "456-789-012-345", "Humanitas", 25.0);
        bookService.createFantasyBook(nextItemId++, "Povestea lui Harap-Alb", 1894, 150,
                testAuthor, "567-890-123-456", "Humanitas", 25.0);
        bookService.createRomanceBook(nextItemId++, "Frumoasa din padurea adormita", 1890, 180,
                testAuthor, "678-901-234-567", "Humanitas", 3, "basme", 28.0);
        bookService.createRomanceBook(nextItemId++, "Sarea in bucate", 1880, 120,
                testAuthor2, "789-012-345-678", "Humanitas", 4, "comedie", 22.0);
        bookService.createFantasyBook(nextItemId++, "Nunta Zamfirei", 1889, 140,
                testAuthor2, "890-123-456-789", "Humanitas", 30.0);
        bookService.createFantasyBook(nextItemId++, "Balade si idile", 1893, 100,
                testAuthor3, "901-234-567-890", "Humanitas", 28.0);

        Author modernAuthor = new Author(nextAuthorId++, "Mircea Cartarescu");
        Author modernAuthor2 = new Author(nextAuthorId++, "Ana Blandiana");
        Author modernAuthor3 = new Author(nextAuthorId++, "Nichita Stanescu");

        bookService.createFantasyBook(nextItemId++, "Solenoid", 2015, 800,
                modernAuthor, "012-345-678-901", "Polirom", 55.0);
        bookService.createFantasyBook(nextItemId++, "Nostalgia", 1993, 400,
                modernAuthor, "123-456-789-012", "Polirom", 40.0);
        bookService.createRomanceBook(nextItemId++, "Arhitectura valurilor", 2018, 350,
                modernAuthor, "234-567-890-123", "Polirom", 5, "poetic", 48.0);
        bookService.createRomanceBook(nextItemId++, "Patria mea A4", 2010, 250,
                modernAuthor2, "345-678-901-234", "Humanitas", 4, "lyrical", 35.0);
        bookService.createFantasyBook(nextItemId++, "Opere incomplete", 2013, 300,
                modernAuthor3, "456-789-012-345", "Humanitas", 38.0);
    }

    private void initializeStock() {
        stockService.addStock("123-456-789-012", 8);
        stockService.addStock("234-567-890-123", 5);
        stockService.addStock("345-678-901-234", 6);
        stockService.addStock("978-0-55-357340-0", 3);
        stockService.addStock("978-0-14-143951-8", 2);
        stockService.addStock("978-3-16-148410-0", 4);
        stockService.addStock("978-0-06-088328-7", 5);
        stockService.addStock("978-1-40-004366-5", 3);
        stockService.addStock("978-0-00-711931-8", 7);
        stockService.addStock("978-0-38-512167-5", 4);
        stockService.addStock("978-0-38-550420-5", 6);
        stockService.addStock("978-0-74-753269-9", 10);
        stockService.addStock("978-0-74-753284-2", 8);
        stockService.addStock("978-0-14-044417-9", 2);
        stockService.addStock("978-0-14-044913-6", 3);
        stockService.addStock("978-0-68-480122-3", 5);

        stockService.addStock("456-789-012-345", 4);
        stockService.addStock("567-890-123-456", 5);
        stockService.addStock("678-901-234-567", 6);
        stockService.addStock("789-012-345-678", 3);
        stockService.addStock("890-123-456-789", 4);
        stockService.addStock("901-234-567-890", 5);
        stockService.addStock("012-345-678-901", 3);
        stockService.addStock("123-456-789-012", 4);
        stockService.addStock("234-567-890-123", 5);
        stockService.addStock("345-678-901-234", 3);
        stockService.addStock("456-789-012-345", 4);

        stockService.addStock("greeting_card", 30);
        stockService.addStock("packaging_standard", 25);
        stockService.addStock("packaging_premium", 15);
        stockService.addStock("ribbon", 20);
        stockService.addStock("gift_tag", 18);

        stockService.addStock("1234-5678", 15);
        stockService.addStock("8765-4321", 20);
        stockService.addStock("9876-5432", 12);
        stockService.addStock("9876-5433", 14);
        stockService.addStock("9876-5434", 10);
        stockService.addStock("1111-2222", 18);
        stockService.addStock("3333-4444", 16);
        stockService.addStock("5555-6666", 11);
        stockService.addStock("7777-8888", 9);
        stockService.addStock("9999-0000", 22);
        stockService.addStock("1111-3333", 13);
        stockService.addStock("4444-5555", 17);
        stockService.addStock("6666-7777", 8);
        stockService.addStock("8888-9999", 10);
    }

    private void initializeMembers() {
        memberService.addMember("Ion Popescu", "ion.popescu@email.com", MemberType.SIMPLE);
        memberService.addMember("Maria Ionescu", "maria.ionescu@email.com", MemberType.STUDENT);
        memberService.addMember("Prof. George Vasile", "george.vasile@univ.ro", MemberType.PROFESSOR);
        memberService.addMember("Ana Marinescu", "ana.marinescu@email.com", MemberType.SIMPLE);
        memberService.addMember("Andrei Popa", "andrei.popa@student.ro", MemberType.STUDENT);
        memberService.addMember("Elena Dumitrescu", "elena.dumitrescu@email.com", MemberType.SIMPLE);
        memberService.addMember("Prof. Mihai Stanescu", "mihai.stanescu@univ.ro", MemberType.PROFESSOR);
        memberService.addMember("Cristina Munteanu", "cristina.munteanu@student.ro", MemberType.STUDENT);
        memberService.addMember("Daniela Chereji", "daniela.chereji@email.com", MemberType.SIMPLE);
        memberService.addMember("Razvan Ionescu", "razvan.ionescu@student.ro", MemberType.STUDENT);
        memberService.addMember("Prof. Andreea Popescu", "andreea.popescu@univ.ro", MemberType.PROFESSOR);
        memberService.addMember("Florin Georgescu", "florin.georgescu@email.com", MemberType.SIMPLE);
        memberService.addMember("Ioana Vasilescu", "ioana.vasilescu@student.ro", MemberType.STUDENT);
        memberService.addMember("Prof. Radu Constantinescu", "radu.constantinescu@univ.ro", MemberType.PROFESSOR);
        memberService.addMember("Carmen Ionescu", "carmen.ionescu@email.com", MemberType.SIMPLE);
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

        if (members.size() > 0 && books.size() > 0) {
            try {
                Loan loan1 = loanService.createLoan(members.get(0), books.get(0));
                if (loan1 != null) {
                    loan1.setStartDate(LocalDate.now().minusDays(5));
                    loan1.setReturnDate(LocalDate.now().plusDays(9));
                    loan1.setActive(true);
                }
            } catch (Exception e) {}
        }

        if (members.size() > 1 && books.size() > 1) {
            try {
                Loan loan2 = loanService.createLoan(members.get(1), books.get(1));
                if (loan2 != null) {
                    loan2.setStartDate(LocalDate.now().minusDays(3));
                    loan2.setReturnDate(LocalDate.now().plusDays(18));
                    loan2.setActive(true);
                }
            } catch (Exception e) {}
        }

        if (members.size() > 2 && books.size() > 2) {
            try {
                Loan loan3 = loanService.createLoan(members.get(2), books.get(2));
                if (loan3 != null) {
                    loan3.setStartDate(LocalDate.now().minusDays(10));
                    loan3.setReturnDate(LocalDate.now().plusDays(20));
                    loan3.setActive(true);
                }
            } catch (Exception e) {}
        }

        if (members.size() > 3 && books.size() > 3) {
            try {
                Loan loan4 = loanService.createLoan(members.get(3), books.get(3));
                if (loan4 != null) {
                    loan4.setStartDate(LocalDate.now().minusDays(2));
                    loan4.setReturnDate(LocalDate.now().plusDays(12));
                    loan4.setActive(true);
                }
            } catch (Exception e) {}
        }

        if (members.size() > 4 && books.size() > 4) {
            try {
                Loan loan5 = loanService.createLoan(members.get(4), books.get(4));
                if (loan5 != null) {
                    loan5.setStartDate(LocalDate.now().minusDays(7));
                    loan5.setReturnDate(LocalDate.now().plusDays(7));
                    loan5.setActive(true);
                }
            } catch (Exception e) {}
        }

        if (members.size() > 5 && books.size() > 5) {
            try {
                Loan loan6 = loanService.createLoan(members.get(5), books.get(5));
                if (loan6 != null) {
                    loan6.setStartDate(LocalDate.now().minusDays(1));
                    loan6.setReturnDate(LocalDate.now().plusDays(13));
                    loan6.setActive(true);
                }
            } catch (Exception e) {}
        }

        if (members.size() > 6 && books.size() > 6) {
            try {
                Loan closedLoan = loanService.createLoan(members.get(6), books.get(6));
                if (closedLoan != null) {
                    closedLoan.setStartDate(LocalDate.now().minusDays(20));
                    closedLoan.setReturnDate(LocalDate.now().minusDays(5));
                    closedLoan.setActive(false);
                }
            } catch (Exception e) {}
        }

        if (members.size() > 7 && books.size() > 7) {
            try {
                Loan closedLoan2 = loanService.createLoan(members.get(7), books.get(7));
                if (closedLoan2 != null) {
                    closedLoan2.setStartDate(LocalDate.now().minusDays(15));
                    closedLoan2.setReturnDate(LocalDate.now().minusDays(2));
                    closedLoan2.setActive(false);
                }
            } catch (Exception e) {}
        }
    }

    private void initializePayments() {
        paymentService.processPayment(5.0, "CASH", "Penalitate intarziere - Dune", 1);
        paymentService.processPayment(3.5, "CARD", "Penalitate intarziere - Iubire in Paris", 2);
        paymentService.processPayment(12.0, "CASH", "Penalitate intarziere - Stapanul Inelelor", 3);
        paymentService.processPayment(8.0, "CARD", "Penalitate intarziere - Jocul Tronurilor", 4);
        paymentService.processPayment(6.5, "CASH", "Penalitate intarziere - Mandrie si Prejudecata", 5);
        paymentService.processPayment(10.0, "CARD", "Penalitate intarziere - Umbra Vantului", 6);
        paymentService.processPayment(4.0, "CASH", "Penalitate intarziere - Un veac de singuratate", 7);
        paymentService.processPayment(7.0, "CARD", "Penalitate intarziere - Kafka pe malul marii", 8);
        paymentService.processPayment(25.0, "CARD", "Pachet cadou standard", null);
        paymentService.processPayment(45.0, "CASH", "Pachet cadou premium", null);
        paymentService.processPayment(30.0, "CARD", "Pachet cadou standard", null);
        paymentService.processPayment(55.0, "CASH", "Pachet cadou premium", null);
        paymentService.processPayment(15.0, "CARD", "Abonament anual biblioteca", 9);
        paymentService.processPayment(20.0, "CASH", "Abonament semestrial", 10);
        paymentService.processPayment(8.0, "CARD", "Taxa inscriere", 11);
    }

    private void initializeEvents() {
        EventService eventService = EventService.getInstance();

        SingleEvent e1 = eventService.createEvent("Lansarea cartii 'Ion' - Editie aniversara", "15.05.2024", "Sala Mare, Biblioteca Centrala", "lansare");
        SingleEvent e2 = eventService.createEvent("Club de lectura - Eminescu", "16.05.2024", "Sala Mica, Biblioteca Centrala", "club");
        SingleEvent e3 = eventService.createEvent("Club de lectura - Rebreanu", "23.05.2024", "Sala Mica, Biblioteca Centrala", "club");
        SingleEvent e4 = eventService.createEvent("Atelier de creatie pentru copii", "17.05.2024", "Atelierul de creatie", "atelier");
        SingleEvent e5 = eventService.createEvent("Expozitie 'Cartea veche'", "20.05.2024 - 30.05.2024", "Sala Expozitii", "expozitie");
        SingleEvent e6 = eventService.createEvent("Conferinta 'Literatura contemporana'", "25.06.2024", "Auditoriul Central", "conferinta");
        SingleEvent e7 = eventService.createEvent("Targ de carte de ocazie", "01.07.2024 - 07.07.2024", "Piata Centrala", "targ");
        SingleEvent e8 = eventService.createEvent("Lansarea romanului 'Nostalgia'", "10.07.2024", "Libraria Carturesti", "lansare");
        SingleEvent e9 = eventService.createEvent("Atelier de scriere creativa", "15.07.2024", "Sala Mica", "atelier");
        SingleEvent e10 = eventService.createEvent("Club de lectura - Tolkien", "20.07.2024", "Sala Mare", "club");
        SingleEvent e11 = eventService.createEvent("Conferinta 'Biblioteca digitala'", "25.07.2024", "Auditoriul Central", "conferinta");
        SingleEvent e12 = eventService.createEvent("Expozitie de ilustratii", "01.08.2024 - 15.08.2024", "Sala Expozitii", "expozitie");
        SingleEvent e13 = eventService.createEvent("Lansarea antologiei 'Poezia romana'", "10.08.2024", "Teatrul National", "lansare");
        SingleEvent e14 = eventService.createEvent("Atelier de restaurat carti", "20.08.2024", "Atelierul de restaurat", "atelier");
        SingleEvent e15 = eventService.createEvent("Club de lectura - Cartarescu", "25.08.2024", "Sala Mica", "club");

        EventGroup clubSeries = eventService.createGroup("Clubul de lectura - Seria Primavara 2024");
        clubSeries.add(e2);
        clubSeries.add(e3);
        clubSeries.add(e10);
        clubSeries.add(e15);

        EventGroup festival = eventService.createGroup("Festivalul Cartii 2024");
        festival.add(e1);
        festival.add(clubSeries);
        festival.add(e4);
        festival.add(e6);
        festival.add(e7);
        festival.add(e8);
        festival.add(e11);

        EventGroup autumnEvents = eventService.createGroup("Toamna literara 2024");
        autumnEvents.add(e9);
        autumnEvents.add(e12);
        autumnEvents.add(e13);
        autumnEvents.add(e14);
    }

    private void initializeReceipts() {
        // CHITANTE - MULTIPLE
        List<Loan> activeLoans = loanService.getActiveLoans();
        if (!activeLoans.isEmpty()) {
            for (int i = 0; i < Math.min(activeLoans.size(), 5); i++) {
                Loan loan = activeLoans.get(i);
                LoanReceipt loanReceipt = new LoanReceipt(loan);
                receiptMenu.saveReceiptToHistory(loanReceipt.generateReceipt());
            }
        }

        List<Loan> allLoans = loanService.getAllLoans();
        for (Loan loan : allLoans) {
            if (!loan.isActive()) {
                int daysLate = 0;
                double penalty = 0;
                if (LocalDate.now().isAfter(loan.getReturnDate())) {
                    daysLate = (int) java.time.temporal.ChronoUnit.DAYS.between(
                            loan.getReturnDate(), LocalDate.now());
                    penalty = daysLate * 1.0;
                }
                ReturnReceiptTemplate returnReceipt = new ReturnReceiptTemplate(loan, LocalDate.now(), daysLate, penalty, penalty > 0);
                receiptMenu.saveReceiptToHistory(returnReceipt.generateReceipt());
            }
        }

        List<Payment> allPayments = paymentService.getAllPayments();
        for (int i = 0; i < Math.min(allPayments.size(), 8); i++) {
            Payment payment = allPayments.get(i);
            String memberName = "Membru ID: " + (payment.getMemberId() != null ? payment.getMemberId() : "Vizitator");
            PaymentReceipt paymentReceipt = new PaymentReceipt(payment, memberName);
            receiptMenu.saveReceiptToHistory(paymentReceipt.generateReceipt());
        }
    }

    private void initializeGiftPackages() {
        List<IBorrowable> items = bookService.findAllBooks();
        List<Book> books = new ArrayList<>();

        for (IBorrowable item : items) {
            Book book = getBookFromItem(item);
            if (book != null) {
                books.add(book);
            }
        }

        if (books.size() >= 10) {
            // Pachete standard
            giftPackageService.createGiftPackage("standard", books.get(0), "La multi ani, iubirea mea!");
            giftPackageService.createGiftPackage("standard", books.get(1), "Pentru cel mai bun prieten!");
            giftPackageService.createGiftPackage("standard", books.get(2), "Sarbatori fericite!");
            giftPackageService.createGiftPackage("standard", books.get(3), "La multi ani, taticule!");
            giftPackageService.createGiftPackage("standard", books.get(4), "Felicitari pentru absolvire!");
            giftPackageService.createGiftPackage("standard", books.get(5), "La multi ani, mamica!");

            // Pachete premium
            giftPackageService.createGiftPackage("premium", books.get(6), "Cu dragoste, de ziua ta!");
            giftPackageService.createGiftPackage("premium", books.get(7), "Pentru tine, cu toata iubirea!");
            giftPackageService.createGiftPackage("premium", books.get(8), "La multi ani, sotia mea draga!");
            giftPackageService.createGiftPackage("premium", books.get(9), "Felicitari pentru noua casa!");
            giftPackageService.createGiftPackage("premium", books.get(10), "Cu respect si admiratie!");
        } else if (!books.isEmpty()) {
            giftPackageService.createGiftPackage("standard", books.get(0), "Cadou surpriza!");
            if (books.size() > 1) {
                giftPackageService.createGiftPackage("premium", books.get(1), "Cadou premium special!");
            }
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