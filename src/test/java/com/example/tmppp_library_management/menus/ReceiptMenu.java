package com.example.tmppp_library_management.menus;

import com.example.tmppp_library_management.services.PaymentService;
import com.example.tmppp_library_management.facade.ReturnFacade;
import com.example.tmppp_library_management.facade.ReturnReceipt;
import com.example.tmppp_library_management.templateMethod.ReturnReceiptTemplate;
import com.example.tmppp_library_management.templateMethod.PaymentReceipt;
import com.example.tmppp_library_management.entity.Payment;
import com.example.tmppp_library_management.entity.Loan;
import com.example.tmppp_library_management.services.LoanService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReceiptMenu {
    private final ReturnFacade returnFacade;
    private final PaymentService paymentService;
    private final LoanService loanService;
    private List<String> receiptHistory;

    public ReceiptMenu() {
        this.returnFacade = new ReturnFacade();
        this.paymentService = PaymentService.getInstance();
        this.loanService = new LoanService();
        this.receiptHistory = new ArrayList<>();
    }

    public void display() {
        while (true) {
            System.out.println("\n--- CHITANTE SI PLATI ---");
            System.out.println("1. Procesare returnare carte");
            System.out.println("2. Procesare returnare cu cod de bare");
            System.out.println("3. Lista toate platile");
            System.out.println("4. Plati pentru un membru");
            System.out.println("5. Total incasari azi");
            System.out.println("6. Afiseaza istoric chitante");
            System.out.println("7. Goleste istoric chitante");
            System.out.println("0. Inapoi");

            int choice = MenuUtils.readInt("Alege optiunea: ");
            switch (choice) {
                case 1 -> processReturn();
                case 2 -> processReturnByBarcode();
                case 3 -> paymentService.listAllPayments();
                case 4 -> showPaymentsForMember();
                case 5 -> showDailyTotal();
                case 6 -> showReceiptHistory();
                case 7 -> clearReceiptHistory();
                case 0 -> { return; }
                default -> System.out.println("Optiune invalida!");
            }
        }
    }

    private void processReturn() {
        System.out.println("\n--- RETURNARE CARTE ---");

        int bookId = MenuUtils.readInt("ID carte: ");
        int memberId = MenuUtils.readInt("ID membru: ");

        ReturnReceipt receipt = returnFacade.processReturn(bookId, memberId);

        if (receipt != null) {
            // Folosește chitanța existentă din facade
            receipt.print();

            // Salvează în istoric folosind Template Method
            saveReturnReceiptToHistory(bookId, memberId);
        }
    }

    private void processReturnByBarcode() {
        System.out.println("\n--- RETURNARE CU COD DE BARE ---");
        System.out.println("(Simulare - introduceti ID-urile ca si coduri)");

        String bookBarcode = MenuUtils.readString("Cod bare carte: ");
        String memberBarcode = MenuUtils.readString("Cod bare membru: ");

        ReturnReceipt receipt = returnFacade.processReturnByBarcode(bookBarcode, memberBarcode);

        if (receipt != null) {
            receipt.print();

            // Salvează în istoric folosind Template Method
            try {
                int bookId = Integer.parseInt(bookBarcode);
                int memberId = Integer.parseInt(memberBarcode);
                saveReturnReceiptToHistory(bookId, memberId);
            } catch (NumberFormatException e) {
                System.out.println("Nu s-a putut salva in istoric.");
            }
        }
    }

    private void saveReturnReceiptToHistory(int bookId, int memberId) {
        for (Loan loan : loanService.getActiveLoans()) {
            if (loan.getBook() != null && loan.getBook().getItemId() == bookId &&
                    loan.getUser().getUserId() == memberId) {

                double penalty = loan.calculatePenalty();
                int daysLate = 0;
                if (java.time.LocalDate.now().isAfter(loan.getReturnDate())) {
                    daysLate = (int) java.time.temporal.ChronoUnit.DAYS.between(
                            loan.getReturnDate(), java.time.LocalDate.now());
                }

                ReturnReceiptTemplate templateReceipt = new ReturnReceiptTemplate(
                        loan,
                        java.time.LocalDate.now(),
                        daysLate,
                        penalty,
                        penalty > 0
                );
                saveReceipt(templateReceipt.generateReceipt());
                break;
            }
        }
    }

    public void printReturnReceipt(Loan loan, LocalDate returnDate, int daysLate, double penalty, boolean penaltyPaid) {
        ReturnReceiptTemplate receipt = new ReturnReceiptTemplate(loan, returnDate, daysLate, penalty, penaltyPaid);
        saveReceipt(receipt.generateReceipt());
    }

    public void saveReceiptToHistory(String receipt) {
        receiptHistory.add(receipt);
    }

    public List<String> getReceiptHistory() {
        return new ArrayList<>(receiptHistory);
    }

    public void clearHistory() {
        receiptHistory.clear();
    }


    public void printLoanReceipt(Loan loan) {
        com.example.tmppp_library_management.templateMethod.LoanReceipt receipt =
                new com.example.tmppp_library_management.templateMethod.LoanReceipt(loan);
        saveReceipt(receipt.generateReceipt());
    }

    public void printPaymentReceipt(Payment payment, String memberName) {
        PaymentReceipt receipt = new PaymentReceipt(payment, memberName);
        saveReceipt(receipt.generateReceipt());
    }

    public void printGiftReceipt(com.example.tmppp_library_management.builder.GiftPackage gift) {
        com.example.tmppp_library_management.templateMethod.GiftReceipt receipt =
                new com.example.tmppp_library_management.templateMethod.GiftReceipt(gift);
        saveReceipt(receipt.generateReceipt());
    }

    private void saveReceipt(String receipt) {
        receiptHistory.add(receipt);
    }

    private void showReceiptHistory() {
        if (receiptHistory.isEmpty()) {
            System.out.println("\nNu exista chitante in istoric.");
            return;
        }

        System.out.println("\n=== ISTORIC CHITANTE ===");
        for (int i = 0; i < receiptHistory.size(); i++) {
            System.out.println("\n--- Chitanta #" + (i + 1) + " ---");
            System.out.println(receiptHistory.get(i));
        }
    }

    private void clearReceiptHistory() {
        receiptHistory.clear();
        System.out.println("Istoric chitante golit.");
    }

    private void showPaymentsForMember() {
        int memberId = MenuUtils.readInt("ID membru: ");
        var payments = paymentService.getPaymentsForMember(memberId);

        if (payments.isEmpty()) {
            System.out.println("Nu exista plati pentru acest membru.");
            return;
        }

        System.out.println("\n=== PLATI PENTRU MEMBRU ID " + memberId + " ===");
        double total = 0;
        for (var p : payments) {
            System.out.printf("  %d | %.2f lei | %s | %s\n",
                    p.getPaymentId(), p.getAmount(), p.getDate(), p.getDescription());
            total += p.getAmount();

            printPaymentReceipt(p, "Membru ID: " + memberId);
        }
        System.out.println("Total: " + total + " lei");
    }

    private void showDailyTotal() {
        double total = paymentService.getTotalForToday();
        System.out.println("\n=== TOTAL INCASARI AZI ===");
        System.out.println(total + " lei");
    }
}