package com.example.tmppp_library_management.menus;

import com.example.tmppp_library_management.book.Book;
import com.example.tmppp_library_management.decorator.BookDecorator;
import com.example.tmppp_library_management.entity.Loan;
import com.example.tmppp_library_management.interfaces.IBorrowable;
import com.example.tmppp_library_management.bridge.*;
import com.example.tmppp_library_management.user.Member;
import com.example.tmppp_library_management.services.BookService;
import com.example.tmppp_library_management.services.LibrarianService;
import com.example.tmppp_library_management.services.LoanService;
import com.example.tmppp_library_management.services.MemberService;
import com.example.tmppp_library_management.services.StockService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LoanMenu {
    private final LoanService loanService;
    private final BookService bookService;
    private final MemberService memberService;
    private final StockService stockService;
    private final LibrarianService librarianService;

    public LoanMenu(LoanService loanService, BookService bookService,
                    MemberService memberService, StockService stockService,
                    LibrarianService librarianService) {
        this.loanService = loanService;
        this.bookService = bookService;
        this.memberService = memberService;
        this.stockService = stockService;
        this.librarianService = librarianService;
    }

    public void display(String token) {
        while (true) {
            System.out.println("\n--- IMPRUMUTURI ---");
            System.out.println("1. Lista imprumuturi active");
            System.out.println("2. Creaza imprumut nou");
            System.out.println("3. Returneaza carte");
            System.out.println("4. Prelungeste imprumut");
            System.out.println("5. Istoric imprumuturi membru");
            System.out.println("6. Afiseaza template-uri imprumut");
            System.out.println("7. Configureaza penalitati");
            System.out.println("0. Inapoi");

            int choice = MenuUtils.readInt("Alege: ");
            switch (choice) {
                case 1 -> listActiveLoans();
                case 2 -> createLoan(token);
                case 3 -> returnBook(token);
                case 4 -> renewLoan();
                case 5 -> memberLoanHistory(token);
                case 6 -> librarianService.showAllTemplates(token);
                case 7 -> configurePenalties();
                case 0 -> { return; }
                default -> System.out.println("Optiune invalida");
            }
        }
    }

    private void listActiveLoans() {
        List<Loan> activeLoans = loanService.getActiveLoans();

        if (activeLoans.isEmpty()) {
            System.out.println("Nu exista imprumuturi active");
            return;
        }

        System.out.println("\nIMPRUMUTURI ACTIVE:");
        System.out.println("ID   | Membru                | Carte                 | Data imprumut | Returnare   | Penalitate");
        System.out.println("-----+-----------------------+-----------------------+---------------+-------------+------------");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        for (Loan loan : activeLoans) {
            double penalty = loan.calculatePenalty();
            String penaltyStr = penalty > 0 ? String.format("%.2f lei", penalty) : "0 lei";

            System.out.printf("%-4d | %-21s | %-21s | %s | %s | %s\n",
                    loan.getLoanId(),
                    MenuUtils.truncate(loan.getUser().getUserName(), 21),
                    MenuUtils.truncate(loan.getBookTitle(), 21),
                    loan.getStartDate().format(formatter),
                    loan.getReturnDate().format(formatter),
                    penaltyStr);
        }
    }

    private void createLoan(String token) {
        System.out.println("\n--- CREAZA IMPRUMUT NOU ---");

        Member member = findMemberForLoan(token);
        if (member == null) return;

        IBorrowable item = findItemForLoan(token);
        if (item == null) return;

        Book book = getBookFromItem(item);
        if (book == null) {
            System.out.println("Itemul nu este o carte valida");
            return;
        }

        if (!stockService.checkAvailability(book.getIsbn(), 1)) {
            System.out.println("✗ Cartea nu este disponibila in stoc");
            return;
        }

        if (!member.canBorrow()) {
            System.out.println("✗ Membrul a atins numarul maxim de imprumuturi (" +
                    member.getMaxBooks() + ")");
            return;
        }

        Loan loan = librarianService.createLoan(token, member, item);

        // Adauga penalitate default (late penalty cu fixed calculator)
        if (loan != null) {
            PenaltyCalculator defaultCalc = new FixedCalculator(1.0);
            Penalty defaultPenalty = new LatePenalty(defaultCalc);
            loan.setPenalty(defaultPenalty);

            System.out.println("\n✓ IMPRUMUT CREAT CU SUCCES!");
            System.out.println("   Membru: " + member.getUserName());
            System.out.println("   Carte: " + book.getTitle());
            if (item instanceof BookDecorator) {
                System.out.println("   Restrictii: " + ((BookDecorator) item).getDescription());
            }
            System.out.println("   Data imprumut: " + loan.getStartDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            System.out.println("   Data returnare: " + loan.getReturnDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            System.out.println("   Durata: " + member.getMemberType().getDefaultDuration() + " zile");
            System.out.println("   Penalitate intarziere: 1 leu/zi");
        }
    }

    private void returnBook(String token) {
        System.out.println("\n--- RETURNARE CARTE ---");

        List<Loan> activeLoans = loanService.getActiveLoans();
        if (activeLoans.isEmpty()) {
            System.out.println("Nu exista imprumuturi active");
            return;
        }

        System.out.println("\nImprumuturi active:");
        for (int i = 0; i < activeLoans.size(); i++) {
            Loan loan = activeLoans.get(i);
            System.out.printf(" %d. ID %d | %s | %s | Returnare: %s\n",
                    i+1,
                    loan.getLoanId(),
                    loan.getUser().getUserName(),
                    loan.getBookTitle(),
                    loan.getReturnDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        }

        int loanChoice = MenuUtils.readInt("Alege numarul imprumutului de returnat: ") - 1;
        if (loanChoice < 0 || loanChoice >= activeLoans.size()) {
            System.out.println("Optiune invalida");
            return;
        }

        Loan loan = activeLoans.get(loanChoice);
        Book book = loan.getBook();
        if (book == null) {
            System.out.println("Eroare: Cartea nu a fost gasita!");
            return;
        }

        // ============ INSPECTIA CARTII ============
        System.out.println("\n--- INSPECTIA CARTII ---");
        System.out.println("Carte: " + book.getTitle());
        System.out.println("Pret: " + book.getPrice() + " lei");
        System.out.println("\nStarea cartii:");
        System.out.println("1. Stare buna (returnare normala)");
        System.out.println("2. Intarziere");
        System.out.println("3. Deteriorata");
        System.out.println("4. Pierduta");

        int condition = MenuUtils.readInt("Alege starea cartii (1-4): ");

        Penalty penalty = null;
        String conditionDesc = "";

        switch (condition) {
            case 1 -> {
                conditionDesc = "returnare normala";
                penalty = null;
            }
            case 2 -> {
                conditionDesc = "intarziere";

                System.out.println("\nMetoda de calcul penalitate intarziere:");
                System.out.println("1. Fix (x lei/zi)");
                System.out.println("2. Procentual (% din pret/zi)");
                System.out.println("3. Progresiv (1/2/3 lei/zi)");

                int calcMethod = MenuUtils.readInt("Alege metoda (1-3): ");
                PenaltyCalculator calculator = null;

                switch (calcMethod) {
                    case 1 -> {
                        double rate = MenuUtils.readDouble("Rata pe zi (lei): ");
                        calculator = new FixedCalculator(rate);
                    }
                    case 2 -> {
                        double percent = MenuUtils.readDouble("Procent pe zi (%): ");
                        calculator = new PercentageCalculator(percent);
                    }
                    case 3 -> {
                        calculator = new ProgressiveCalculator(1.0, 2.0, 3.0);
                    }
                    default -> {
                        System.out.println("Optiune invalida, se foloseste default (1 leu/zi)");
                        calculator = new FixedCalculator(1.0);
                    }
                }
                penalty = new LatePenalty(calculator);
            }
            case 3 -> {
                conditionDesc = "deteriorare";
                double repairCost = MenuUtils.readDouble("Cost estimat reparatie (lei): ");

                System.out.println("\nMetoda de calcul penalizare suplimentara:");
                System.out.println("1. Fix (x lei/zi)");
                System.out.println("2. Procentual (% din pret/zi)");
                System.out.println("3. Fara penalizare suplimentara");

                int calcMethod = MenuUtils.readInt("Alege metoda (1-3): ");
                PenaltyCalculator calculator = null;

                switch (calcMethod) {
                    case 1 -> {
                        double rate = MenuUtils.readDouble("Rata pe zi (lei): ");
                        calculator = new FixedCalculator(rate);
                    }
                    case 2 -> {
                        double percent = MenuUtils.readDouble("Procent pe zi (%): ");
                        calculator = new PercentageCalculator(percent);
                    }
                    default -> {
                        calculator = new FixedCalculator(0);
                    }
                }
                penalty = new DamagedPenalty(calculator, repairCost);
            }
            case 4 -> {
                conditionDesc = "pierdere";
                double bookPrice = book.getPrice();
                System.out.println("Pretul cartii: " + bookPrice + " lei");

                System.out.println("\nMetoda de calcul penalizare suplimentara:");
                System.out.println("1. Fix (x lei/zi)");
                System.out.println("2. Procentual (% din pret/zi)");
                System.out.println("3. Fara penalizare suplimentara");

                int calcMethod = MenuUtils.readInt("Alege metoda (1-3): ");
                PenaltyCalculator calculator = null;

                switch (calcMethod) {
                    case 1 -> {
                        double rate = MenuUtils.readDouble("Rata pe zi (lei): ");
                        calculator = new FixedCalculator(rate);
                    }
                    case 2 -> {
                        double percent = MenuUtils.readDouble("Procent pe zi (%): ");
                        calculator = new PercentageCalculator(percent);
                    }
                    default -> {
                        calculator = new FixedCalculator(0);
                    }
                }
                penalty = new LostPenalty(calculator, bookPrice);
            }
            default -> {
                System.out.println("Optiune invalida");
                return;
            }
        }

        // Seteaza penalitatea
        if (penalty != null) {
            loan.setPenalty(penalty);
        }

        // Calculeaza penalitatea
        double penaltyAmount = loan.calculatePenalty();

        // ============ AFISARE REZUMAT ============
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        System.out.println("\n--- REZUMAT RETURNARE ---");
        System.out.println("Membru: " + loan.getUser().getUserName());
        System.out.println("Carte: " + book.getTitle());
        System.out.println("Data imprumut: " + loan.getStartDate().format(formatter));
        System.out.println("Data scadenta: " + loan.getReturnDate().format(formatter));
        System.out.println("Data returnare: " + LocalDate.now().format(formatter));
        System.out.println("Stare carte: " + conditionDesc);

        if (penaltyAmount > 0) {
            System.out.println("\n⚠️ PENALITATE: " + String.format("%.2f lei", penaltyAmount));
            System.out.println("   Tip: " + loan.getPenaltyType());
            System.out.println("   Descriere: " + loan.getPenaltyDescription());
        } else {
            System.out.println("\n✓ Carte returnata la timp, fara penalitati.");
        }

        // ============ CONFIRMARE ============
        String confirm = MenuUtils.readString("\nConfirmati returnarea? (da/nu): ");
        if (!confirm.equalsIgnoreCase("da")) {
            System.out.println("Operatie anulata.");
            return;
        }

        // ============ PROCESARE PLATA ============
        if (penaltyAmount > 0) {
            String pay = MenuUtils.readString("Doriti sa platiti penalitatea acum? (da/nu): ");
            if (pay.equalsIgnoreCase("da")) {
                librarianService.addToLog(token, "A platit penalitate " + penaltyAmount + " lei pentru imprumutul ID " + loan.getLoanId());
                System.out.println("✓ Penalitate platita cu succes!");
            } else {
                System.out.println("Penalitatea va fi inregistrata si platita ulterior.");
            }
        }

        // Actualizeaza stocul si inchide imprumutul
        stockService.releaseItem(book.getIsbn(), 1);
        loanService.closeLoan(loan);

        System.out.println("\n✓ Carte returnata cu succes!");
    }

    private void configurePenalties() {
        while (true) {
            System.out.println("\n--- CONFIGURARE PENALITATI ---");
            System.out.println("1. Tip penalitate (Intarziere / Pierdere / Deteriorare)");
            System.out.println("2. Metoda de calcul (Fix / Procentual / Progresiv)");
            System.out.println("3. Aplica penalitate la un imprumut existent");
            System.out.println("4. Afiseaza penalitatile unui imprumut");
            System.out.println("0. Inapoi");

            int choice = MenuUtils.readInt("Alege: ");
            switch (choice) {
                case 1 -> selectPenaltyType();
                case 2 -> selectCalculationMethod();
                case 3 -> applyPenaltyToLoan();
                case 4 -> showLoanPenalty();
                case 0 -> { return; }
                default -> System.out.println("Optiune invalida");
            }
        }
    }

    private void selectPenaltyType() {
        System.out.println("\n--- TIPURI PENALITATE ---");
        System.out.println("1. Intarziere");
        System.out.println("2. Pierdere");
        System.out.println("3. Deteriorare");

        int type = MenuUtils.readInt("Alege tipul: ");
        System.out.println("✓ Tip penalitate selectat");
    }

    private void selectCalculationMethod() {
        System.out.println("\n--- METODE DE CALCUL ---");
        System.out.println("1. Fix (x lei/zi)");
        System.out.println("2. Procentual (% din pret/zi)");
        System.out.println("3. Progresiv (1 leu/zi prima saptamana, 2 lei/zi a doua, 3 lei/zi dupa)");

        int method = MenuUtils.readInt("Alege metoda: ");

        PenaltyCalculator calculator = null;

        switch (method) {
            case 1 -> {
                double rate = MenuUtils.readDouble("Rata pe zi (lei): ");
                calculator = new FixedCalculator(rate);
                System.out.println("✓ Calculator fix selectat: " + rate + " lei/zi");
            }
            case 2 -> {
                double percent = MenuUtils.readDouble("Procent pe zi (%): ");
                calculator = new PercentageCalculator(percent);
                System.out.println("✓ Calculator procentual selectat: " + percent + "%/zi");
            }
            case 3 -> {
                System.out.println("Calculator progresiv: 1 leu/zi (1-7 zile), 2 lei/zi (8-14 zile), 3 lei/zi (>14 zile)");
                calculator = new ProgressiveCalculator(1.0, 2.0, 3.0);
                System.out.println("✓ Calculator progresiv selectat");
            }
            default -> {
                System.out.println("Optiune invalida");
                return;
            }
        }

        loanService.setDefaultPenaltyCalculator(calculator);
    }

    private void applyPenaltyToLoan() {
        List<Loan> activeLoans = loanService.getActiveLoans();
        if (activeLoans.isEmpty()) {
            System.out.println("Nu exista imprumuturi active");
            return;
        }

        System.out.println("\nImprumuturi active:");
        for (int i = 0; i < activeLoans.size(); i++) {
            Loan loan = activeLoans.get(i);
            System.out.printf(" %d. ID %d | %s | %s | Returnare: %s\n",
                    i+1,
                    loan.getLoanId(),
                    loan.getUser().getUserName(),
                    loan.getBookTitle(),
                    loan.getReturnDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        }

        int loanChoice = MenuUtils.readInt("Alege numarul imprumutului: ") - 1;
        if (loanChoice < 0 || loanChoice >= activeLoans.size()) {
            System.out.println("Optiune invalida");
            return;
        }

        Loan loan = activeLoans.get(loanChoice);

        System.out.println("\nTip penalitate:");
        System.out.println("1. Intarziere");
        System.out.println("2. Pierdere");
        System.out.println("3. Deteriorare");
        int type = MenuUtils.readInt("Alege: ");

        Penalty penalty = null;

        switch (type) {
            case 1 -> {
                PenaltyCalculator calc = getCalculatorFromUser();
                if (calc != null) {
                    penalty = new LatePenalty(calc);
                }
            }
            case 2 -> {
                double bookPrice = MenuUtils.readDouble("Pretul cartii (lei): ");
                PenaltyCalculator calc = getCalculatorFromUser();
                if (calc != null) {
                    penalty = new LostPenalty(calc, bookPrice);
                }
            }
            case 3 -> {
                double repairCost = MenuUtils.readDouble("Cost reparatie (lei): ");
                PenaltyCalculator calc = getCalculatorFromUser();
                if (calc != null) {
                    penalty = new DamagedPenalty(calc, repairCost);
                }
            }
            default -> {
                System.out.println("Tip invalid");
                return;
            }
        }

        if (penalty != null) {
            loan.setPenalty(penalty);
            System.out.println("✓ Penalitate aplicata cu succes!");
            System.out.println("   Tip: " + penalty.getType());
            System.out.println("   Descriere: " + penalty.getDescription());
        }
    }

    private PenaltyCalculator getCalculatorFromUser() {
        System.out.println("\nMetoda de calcul:");
        System.out.println("1. Fix (x lei/zi)");
        System.out.println("2. Procentual (% din pret/zi)");
        System.out.println("3. Progresiv (1/2/3 lei/zi)");

        int method = MenuUtils.readInt("Alege: ");

        switch (method) {
            case 1 -> {
                double rate = MenuUtils.readDouble("Rata pe zi (lei): ");
                return new FixedCalculator(rate);
            }
            case 2 -> {
                double percent = MenuUtils.readDouble("Procent pe zi (%): ");
                return new PercentageCalculator(percent);
            }
            case 3 -> {
                return new ProgressiveCalculator(1.0, 2.0, 3.0);
            }
            default -> {
                System.out.println("Optiune invalida");
                return null;
            }
        }
    }

    private void showLoanPenalty() {
        List<Loan> activeLoans = loanService.getActiveLoans();
        if (activeLoans.isEmpty()) {
            System.out.println("Nu exista imprumuturi active");
            return;
        }

        System.out.println("\nImprumuturi active:");
        for (int i = 0; i < activeLoans.size(); i++) {
            Loan loan = activeLoans.get(i);
            System.out.printf(" %d. ID %d | %s | %s\n",
                    i+1,
                    loan.getLoanId(),
                    loan.getUser().getUserName(),
                    loan.getBookTitle());
        }

        int loanChoice = MenuUtils.readInt("Alege numarul imprumutului: ") - 1;
        if (loanChoice < 0 || loanChoice >= activeLoans.size()) {
            System.out.println("Optiune invalida");
            return;
        }

        Loan loan = activeLoans.get(loanChoice);

        System.out.println("\n--- DETALII PENALITATE ---");
        System.out.println("Tip: " + loan.getPenaltyType());
        System.out.println("Descriere: " + loan.getPenaltyDescription());
        System.out.println("Penalitate curenta: " + loan.calculatePenalty() + " lei");
    }

    private Member findMemberForLoan(String token) {
        System.out.println("\nCauta membru:");
        System.out.println("1. Dupa ID");
        System.out.println("2. Dupa numar membership");
        System.out.println("3. Dupa nume");
        System.out.println("4. Dupa email");

        int opt = MenuUtils.readInt("Alege: ");
        Member member = null;

        switch (opt) {
            case 1 -> {
                int id = MenuUtils.readIntWithCommands("ID membru (sau /show members): ", null, this::listAllMembersSimple, this::listActiveLoansSimple, null);
                member = librarianService.findMember(token, id);
            }
            case 2 -> {
                String membership = MenuUtils.readStringWithCommands("Numar membership (sau /show members): ", null, this::listAllMembersSimple, this::listActiveLoansSimple, null);
                member = librarianService.findMemberByMembership(token, membership);
            }
            case 3 -> {
                String name = MenuUtils.readStringWithCommands("Nume (sau /show members): ", null, this::listAllMembersSimple, this::listActiveLoansSimple, null);
                List<Member> results = new ArrayList<>();
                for (Member m : memberService.getAllMembers()) {
                    if (m.getUserName().toLowerCase().contains(name.toLowerCase())) {
                        results.add(m);
                    }
                }
                member = selectMemberFromList(results);
            }
            case 4 -> {
                String email = MenuUtils.readStringWithCommands("Email (sau /show members): ", null, this::listAllMembersSimple, this::listActiveLoansSimple, null);
                List<Member> results = new ArrayList<>();
                for (Member m : memberService.getAllMembers()) {
                    if (m.getUserEmail().toLowerCase().contains(email.toLowerCase())) {
                        results.add(m);
                    }
                }
                member = selectMemberFromList(results);
            }
            default -> {
                System.out.println("Optiune invalida");
                return null;
            }
        }

        if (member == null) {
            System.out.println("Membru negasit");
            return null;
        }

        System.out.println("Membru gasit: " + member.getUserName() + " (" + member.getMemberType() + ")");
        return member;
    }

    private void listAllMembersSimple() {
        memberService.listAllMembers();
    }

    private void listActiveLoansSimple() {
        listActiveLoans();
    }

    private Member selectMemberFromList(List<Member> members) {
        if (members.isEmpty()) return null;
        if (members.size() == 1) return members.get(0);

        System.out.println("\nMembri gasiti:");
        for (int i = 0; i < members.size(); i++) {
            Member m = members.get(i);
            System.out.printf(" %d. %s (ID: %d, Membership: %s, Tip: %s)\n",
                    i+1, m.getUserName(), m.getUserId(), m.getMembershipNumber(), m.getMemberType());
        }
        int choice = MenuUtils.readInt("Alege numarul: ") - 1;
        return (choice >= 0 && choice < members.size()) ? members.get(choice) : null;
    }

    private Book getBookFromItem(IBorrowable item) {
        if (item instanceof Book) {
            return (Book) item;
        } else if (item instanceof BookDecorator) {
            return ((BookDecorator) item).getOriginalBook();
        }
        return null;
    }

    private IBorrowable findItemForLoan(String token) {
        System.out.println("\nCauta carte:");
        System.out.println("1. Dupa ID");
        System.out.println("2. Dupa ISBN");
        System.out.println("3. Dupa titlu");

        int opt = MenuUtils.readInt("Alege: ");
        IBorrowable item = null;

        switch (opt) {
            case 1 -> {
                int id = MenuUtils.readIntWithCommands("ID carte (sau /show books): ", this::listAllItemsSimple, null, this::listActiveLoansSimple, null);
                for (IBorrowable i : bookService.findAllBooks()) {
                    Book b = getBookFromItem(i);
                    if (b != null && b.getItemId() == id) {
                        item = i;
                        break;
                    }
                }
            }
            case 2 -> {
                String isbn = MenuUtils.readStringWithCommands("ISBN (sau /show books): ", this::listAllItemsSimple, null, this::listActiveLoansSimple, null);
                for (IBorrowable i : bookService.findAllBooks()) {
                    Book b = getBookFromItem(i);
                    if (b != null && b.getIsbn().equals(isbn)) {
                        item = i;
                        break;
                    }
                }
            }
            case 3 -> {
                String title = MenuUtils.readStringWithCommands("Titlu (sau /show books): ", this::listAllItemsSimple, null, this::listActiveLoansSimple, null);
                List<IBorrowable> results = bookService.findBooksByTitle(title);
                if (results.isEmpty()) {
                    System.out.println("Nu s-au gasit carti");
                    return null;
                }
                if (results.size() == 1) {
                    item = results.get(0);
                } else {
                    System.out.println("\nCarti gasite:");
                    for (int i = 0; i < results.size(); i++) {
                        IBorrowable ib = results.get(i);
                        Book b = getBookFromItem(ib);
                        if (b != null) {
                            int stock = stockService.getStock(b.getIsbn()) != null ?
                                    stockService.getStock(b.getIsbn()).getAvailableQuantity() : 0;
                            System.out.printf(" %d. %s de %s (Stoc: %d)%s\n",
                                    i+1, b.getTitle(), b.getAuthor().getName(), stock,
                                    ib instanceof BookDecorator ? " [RESTRICTIONAT]" : "");
                        }
                    }
                    int choice = MenuUtils.readInt("Alege numarul: ") - 1;
                    if (choice >= 0 && choice < results.size()) {
                        item = results.get(choice);
                    }
                }
            }
            default -> {
                System.out.println("Optiune invalida");
                return null;
            }
        }

        if (item != null) {
            Book book = getBookFromItem(item);
            int stock = stockService.getStock(book.getIsbn()) != null ?
                    stockService.getStock(book.getIsbn()).getAvailableQuantity() : 0;
            System.out.println("Carte gasita: " + book.getTitle() + " de " + book.getAuthor().getName() +
                    " (stoc: " + stock + ")");
            if (item instanceof BookDecorator) {
                System.out.println("   Restrictii: " + ((BookDecorator) item).getDescription());
            }
        }
        return item;
    }

    private void listAllItemsSimple() {
        List<IBorrowable> items = bookService.findAllBooks();
        if (items.isEmpty()) {
            System.out.println("Nu exista carti");
            return;
        }

        System.out.println("\nLISTA CARTI:");
        System.out.println("ID  | Titlu                   | Autor                  | Stoc | ISBN | Restrictii");
        System.out.println("----+-------------------------+------------------------+------+------+-----------");

        for (IBorrowable item : items) {
            Book book = getBookFromItem(item);
            if (book == null) continue;

            int stock = stockService.getStock(book.getIsbn()) != null ?
                    stockService.getStock(book.getIsbn()).getAvailableQuantity() : 0;

            String restrictii = "";
            if (item instanceof BookDecorator) {
                restrictii = ((BookDecorator) item).getDescription();
                if (restrictii.contains("[")) {
                    restrictii = restrictii.substring(restrictii.indexOf("["));
                }
            }

            System.out.printf("%-3d | %-23s | %-22s | %-4d | %s | %s\n",
                    book.getItemId(),
                    MenuUtils.truncate(book.getTitle(), 23),
                    MenuUtils.truncate(book.getAuthor().getName(), 22),
                    stock,
                    book.getIsbn(),
                    restrictii);
        }
    }

    private void renewLoan() {
        System.out.println("\n--- PRELUNGIRE IMPRUMUT ---");

        List<Loan> activeLoans = loanService.getActiveLoans();
        if (activeLoans.isEmpty()) {
            System.out.println("Nu exista imprumuturi active");
            return;
        }

        System.out.println("\nImprumuturi active:");
        for (int i = 0; i < activeLoans.size(); i++) {
            Loan loan = activeLoans.get(i);
            System.out.printf(" %d. %s - %s (returnare: %s)\n",
                    i+1,
                    loan.getUser().getUserName(),
                    loan.getBookTitle(),
                    loan.getReturnDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        }

        int loanChoice = MenuUtils.readInt("Alege numarul imprumutului de prelungit: ") - 1;
        if (loanChoice < 0 || loanChoice >= activeLoans.size()) {
            System.out.println("Optiune invalida");
            return;
        }

        Loan loan = activeLoans.get(loanChoice);
        int extraDays = MenuUtils.readInt("Numar zile prelungire: ");

        loanService.renewLoan(loan, extraDays);
        System.out.println("✓ Imprumut prelungit pana la " +
                loan.getReturnDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
    }

    private void memberLoanHistory(String token) {
        System.out.println("\n--- ISTORIC IMPRUMUTURI MEMBRU ---");

        Member member = findMemberForLoan(token);
        if (member == null) return;

        List<Loan> allLoans = loanService.getAllLoans();
        List<Loan> memberLoans = new ArrayList<>();

        for (Loan loan : allLoans) {
            if (loan.getUser().getUserId() == member.getUserId()) {
                memberLoans.add(loan);
            }
        }

        if (memberLoans.isEmpty()) {
            System.out.println("Membrul " + member.getUserName() + " nu are imprumuturi.");
            return;
        }

        System.out.println("\nISTORIC IMPRUMUTURI PENTRU " + member.getUserName() + ":");
        System.out.println("ID   | Carte                 | Data imprumut | Returnare   | Status  | Penalitate");
        System.out.println("-----+-----------------------+---------------+-------------+---------+-----------");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        for (Loan loan : memberLoans) {
            String status = loan.isActive() ? "ACTIV" : "INCHIS";
            double penalty = loan.calculatePenalty();
            String penaltyStr = penalty > 0 ? String.format("%.2f lei", penalty) : "-";
            System.out.printf("%-4d | %-21s | %s | %s | %-7s | %s\n",
                    loan.getLoanId(),
                    MenuUtils.truncate(loan.getBookTitle(), 21),
                    loan.getStartDate().format(formatter),
                    loan.getReturnDate().format(formatter),
                    status,
                    penaltyStr);
        }
    }
}