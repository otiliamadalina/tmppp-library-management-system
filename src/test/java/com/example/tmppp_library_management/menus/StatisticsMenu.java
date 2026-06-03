package com.example.tmppp_library_management.menus;

import com.example.tmppp_library_management.book.Book;
import com.example.tmppp_library_management.book.FantasyBook;
import com.example.tmppp_library_management.book.RomanceBook;
import com.example.tmppp_library_management.entity.Loan;
import com.example.tmppp_library_management.entity.Stock;
import com.example.tmppp_library_management.newspaper.LocalNewspaper;
import com.example.tmppp_library_management.newspaper.NationalNewspaper;
import com.example.tmppp_library_management.newspaper.Newspaper;
import com.example.tmppp_library_management.services.BookService;
import com.example.tmppp_library_management.services.LoanService;
import com.example.tmppp_library_management.services.MemberService;
import com.example.tmppp_library_management.services.NewspaperService;
import com.example.tmppp_library_management.services.StockService;
import com.example.tmppp_library_management.user.Member;
import com.example.tmppp_library_management.user.MemberType;
import com.example.tmppp_library_management.interfaces.IBorrowable;
import com.example.tmppp_library_management.abstractClasses.LibraryItem;

import java.time.LocalDate;
import java.util.List;

public class StatisticsMenu {
    private final BookService bookService;
    private final NewspaperService newspaperService;
    private final LoanService loanService;
    private final MemberService memberService;
    private final StockService stockService;

    public StatisticsMenu(BookService bookService, NewspaperService newspaperService,
                          LoanService loanService, MemberService memberService) {
        this.bookService = bookService;
        this.newspaperService = newspaperService;
        this.loanService = loanService;
        this.memberService = memberService;
        this.stockService = StockService.getInstance();
    }

    public String getFullStatistics() {
        StringBuilder sb = new StringBuilder();

        List<Book> allBooks = bookService.findAllBooksLegacy();
        int fantasyCount = 0;
        int romanceCount = 0;
        double totalStockValue = 0.0;

        for (Book book : allBooks) {
            if (book instanceof FantasyBook) fantasyCount++;
            else if (book instanceof RomanceBook) romanceCount++;

            Stock stock = stockService.getStock(book.getIsbn());
            if (stock != null) {
                totalStockValue += stock.getQuantity() * book.getPrice();
            }
        }

        sb.append("CARTI\n");
        sb.append("Total carti: ").append(allBooks.size()).append("\n");
        sb.append("Fantasy: ").append(fantasyCount).append("\n");
        sb.append("Romance: ").append(romanceCount).append("\n");
        sb.append("Valoare totala stoc: ").append(String.format("%.2f", totalStockValue)).append(" lei\n\n");

        List<Newspaper> allNewspapers = newspaperService.getAllItems().stream()
                .filter(item -> item instanceof Newspaper)
                .map(item -> (Newspaper) item)
                .toList();
        int localCount = 0;
        int nationalCount = 0;

        for (Newspaper n : allNewspapers) {
            if (n instanceof LocalNewspaper) localCount++;
            else if (n instanceof NationalNewspaper) nationalCount++;
        }

        sb.append("ZIARE\n");
        sb.append("Total ziare: ").append(allNewspapers.size()).append("\n");
        sb.append("Locale: ").append(localCount).append("\n");
        sb.append("Nationale: ").append(nationalCount).append("\n\n");

        List<Loan> allLoans = loanService.getAllLoans();
        List<Loan> activeLoans = loanService.getActiveLoans();
        int lateLoans = 0;
        for (Loan loan : activeLoans) {
            if (LocalDate.now().isAfter(loan.getReturnDate())) {
                lateLoans++;
            }
        }

        sb.append("IMPRUMUTURI\n");
        sb.append("Total imprumuturi: ").append(allLoans.size()).append("\n");
        sb.append("Imprumuturi active: ").append(activeLoans.size()).append("\n");
        sb.append("Imprumuturi intarziate: ").append(lateLoans).append("\n\n");

        List<Member> allMembers = memberService.getAllMembers();
        int simpleCount = 0;
        int studentCount = 0;
        int professorCount = 0;
        int activeMembers = 0;

        for (Member m : allMembers) {
            if (m.getMemberType() == MemberType.SIMPLE) simpleCount++;
            else if (m.getMemberType() == MemberType.STUDENT) studentCount++;
            else if (m.getMemberType() == MemberType.PROFESSOR) professorCount++;

            if (m.getCurrentLoans() > 0) {
                activeMembers++;
            }
        }

        sb.append("MEMBRI\n");
        sb.append("Total membri: ").append(allMembers.size()).append("\n");
        sb.append("Membri activi: ").append(activeMembers).append("\n");
        sb.append("SIMPLE: ").append(simpleCount).append("\n");
        sb.append("STUDENT: ").append(studentCount).append("\n");
        sb.append("PROFESSOR: ").append(professorCount).append("\n");

        return sb.toString();
    }

    public String getBookStatistics() {
        List<Book> allBooks = bookService.findAllBooksLegacy();
        int fantasyCount = 0;
        int romanceCount = 0;
        double totalStockValue = 0.0;

        for (Book book : allBooks) {
            if (book instanceof FantasyBook) fantasyCount++;
            else if (book instanceof RomanceBook) romanceCount++;

            Stock stock = stockService.getStock(book.getIsbn());
            if (stock != null) {
                totalStockValue += stock.getQuantity() * book.getPrice();
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("CARTI\n");
        sb.append("Total carti: ").append(allBooks.size()).append("\n");
        sb.append("Fantasy: ").append(fantasyCount).append("\n");
        sb.append("Romance: ").append(romanceCount).append("\n");
        sb.append("Valoare totala stoc: ").append(String.format("%.2f", totalStockValue)).append(" lei\n");

        return sb.toString();
    }

    public String getLoanStatistics() {
        List<Loan> allLoans = loanService.getAllLoans();
        List<Loan> activeLoans = loanService.getActiveLoans();
        int lateLoans = 0;
        for (Loan loan : activeLoans) {
            if (LocalDate.now().isAfter(loan.getReturnDate())) {
                lateLoans++;
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("IMPRUMUTURI\n");
        sb.append("Total imprumuturi: ").append(allLoans.size()).append("\n");
        sb.append("Imprumuturi active: ").append(activeLoans.size()).append("\n");
        sb.append("Imprumuturi intarziate: ").append(lateLoans).append("\n");

        return sb.toString();
    }

    public String getMemberStatistics() {
        List<Member> allMembers = memberService.getAllMembers();
        int simpleCount = 0;
        int studentCount = 0;
        int professorCount = 0;
        int activeMembers = 0;

        for (Member m : allMembers) {
            if (m.getMemberType() == MemberType.SIMPLE) simpleCount++;
            else if (m.getMemberType() == MemberType.STUDENT) studentCount++;
            else if (m.getMemberType() == MemberType.PROFESSOR) professorCount++;

            if (m.getCurrentLoans() > 0) {
                activeMembers++;
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("MEMBRI\n");
        sb.append("Total membri: ").append(allMembers.size()).append("\n");
        sb.append("Membri activi: ").append(activeMembers).append("\n");
        sb.append("SIMPLE: ").append(simpleCount).append("\n");
        sb.append("STUDENT: ").append(studentCount).append("\n");
        sb.append("PROFESSOR: ").append(professorCount).append("\n");

        return sb.toString();
    }

    public String getNewspaperStatistics() {
        List<Newspaper> allNewspapers = newspaperService.getAllItems().stream()
                .filter(item -> item instanceof Newspaper)
                .map(item -> (Newspaper) item)
                .toList();
        int localCount = 0;
        int nationalCount = 0;

        for (Newspaper n : allNewspapers) {
            if (n instanceof LocalNewspaper) localCount++;
            else if (n instanceof NationalNewspaper) nationalCount++;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("ZIARE\n");
        sb.append("Total ziare: ").append(allNewspapers.size()).append("\n");
        sb.append("Locale: ").append(localCount).append("\n");
        sb.append("Nationale: ").append(nationalCount).append("\n");

        return sb.toString();
    }
}