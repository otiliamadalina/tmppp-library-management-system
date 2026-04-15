package com.example.tmppp_library_management.facade;

import com.example.tmppp_library_management.book.Book;
import com.example.tmppp_library_management.entity.Loan;
import com.example.tmppp_library_management.user.Member;
import com.example.tmppp_library_management.services.LoanService;
import com.example.tmppp_library_management.services.MemberService;
import com.example.tmppp_library_management.services.PaymentService;
import com.example.tmppp_library_management.services.StockService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ReturnFacade {
    private LoanService loanService;
    private MemberService memberService;
    private StockService stockService;
    private PaymentService paymentService;
    private int nextReceiptId;

    public ReturnFacade() {
        this.loanService = new LoanService();
        this.memberService = MemberService.getInstance();
        this.stockService = StockService.getInstance();
        this.paymentService = PaymentService.getInstance();
        this.nextReceiptId = 1;
    }

    public ReturnReceipt processReturn(int bookId, int memberId) {
        System.out.println("\n--- PROCESARE RETURNARE ---");

        // 1. Gaseste membrul
        Member member = memberService.getMember(memberId);
        if (member == null) {
            System.out.println("Eroare: Membru negasit!");
            return null;
        }

        // 2. Gaseste loan-ul activ pentru aceasta carte si acest membru
        Loan activeLoan = findActiveLoan(bookId, memberId);
        if (activeLoan == null) {
            System.out.println("Eroare: Nu exista imprumut activ pentru aceasta carte!");
            return null;
        }

        Book book = activeLoan.getBook();
        LocalDate dueDate = activeLoan.getReturnDate();
        LocalDate returnDate = LocalDate.now();

        // 3. Calculeaza penalitatea (1 leu/zi pentru SIMPLE, 2 lei/zi pentru altii - exemplu)
        long daysLate = 0;
        double penalty = 0;

        if (returnDate.isAfter(dueDate)) {
            daysLate = ChronoUnit.DAYS.between(dueDate, returnDate);
            penalty = calculatePenalty(member, daysLate);

            // 4. Proceseaza plata penalitatii
            if (penalty > 0) {
                String description = "Penalitate intarziere - " + book.getTitle();
                paymentService.processPayment(penalty, "CASH", description, memberId);
                System.out.println("Penalitate platita: " + penalty + " lei");
            }
        }

        // 5. Actualizeaza stocul (creste numarul de exemplare disponibile)
        stockService.increaseStock(book.getIsbn(), 1);

        // 6. Inchide imprumutul
        loanService.closeLoan(activeLoan);

        // 7. Actualizeaza numarul de imprumuturi ale membrului
        member.setCurrentLoans(member.getCurrentLoans() - 1);

        // 8. Genereaza si returneaza chitanta
        ReturnReceipt receipt = new ReturnReceipt(
                nextReceiptId++,
                member.getUserName(),
                book.getTitle(),
                returnDate,
                dueDate,
                (int) daysLate,
                penalty,
                penalty > 0
        );

        System.out.println("✓ Returnare procesata cu succes!");
        return receipt;
    }


    public ReturnReceipt processReturnByBarcode(String bookBarcode, String memberBarcode) {

        try {
            int bookId = Integer.parseInt(bookBarcode);
            int memberId = Integer.parseInt(memberBarcode);
            return processReturn(bookId, memberId);
        } catch (NumberFormatException e) {
            System.out.println("Eroare: Cod de bare invalid!");
            return null;
        }
    }

    private double calculatePenalty(Member member, long daysLate) {
        double ratePerDay;
        switch (member.getMemberType()) {
            case STUDENT:
            case SIMPLE:
                ratePerDay = 1.0;
                break;
            case PROFESSOR:
                ratePerDay = 0.5; // profesori au reducere
                break;
            default:
                ratePerDay = 1.0;
        }
        return ratePerDay * daysLate;
    }


    private Loan findActiveLoan(int bookId, int memberId) {
        for (Loan loan : loanService.getActiveLoans()) {
            if (loan.getBook().getItemId() == bookId &&
                    loan.getUser().getUserId() == memberId) {
                return loan;
            }
        }
        return null;
    }
}