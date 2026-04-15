package com.example.tmppp_library_management.templateMethod;

import com.example.tmppp_library_management.entity.Loan;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ReturnReceiptTemplate extends ReceiptTemplate {
    private final Loan loan;
    private final LocalDate returnDate;
    private final int daysLate;
    private final double penalty;
    private final boolean penaltyPaid;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public ReturnReceiptTemplate(Loan loan, LocalDate returnDate, int daysLate, double penalty, boolean penaltyPaid) {
        this.loan = loan;
        this.returnDate = returnDate;
        this.daysLate = daysLate;
        this.penalty = penalty;
        this.penaltyPaid = penaltyPaid;
    }

    @Override
    protected String getHeader() {
        return "        CHITANTA RETURNARE";
    }

    @Override
    protected String getBody() {
        StringBuilder sb = new StringBuilder();
        sb.append("ID Imprumut: #").append(loan.getLoanId()).append("\n");
        sb.append("Membru: ").append(loan.getUser().getUserName()).append("\n");
        sb.append("Carte: ").append(loan.getBookTitle()).append("\n");
        sb.append("Data returnare: ").append(returnDate.format(dateFormatter)).append("\n");
        sb.append("Data scadenta: ").append(loan.getReturnDate().format(dateFormatter)).append("\n");
        sb.append("\n");

        if (daysLate > 0) {
            sb.append("Zile intarziere: ").append(daysLate).append("\n");
            sb.append("Penalitate: ").append(formatPrice(penalty)).append("\n");
            sb.append("Status plata: ").append(penaltyPaid ? "ACHITAT" : "NECHITAT").append("\n");
        } else {
            sb.append("Returnare la timp. Fara penalitati.\n");
        }

        return sb.toString();
    }

    @Override
    protected String getFooter() {
        return "        Va multumim pentru returnare!";
    }
}