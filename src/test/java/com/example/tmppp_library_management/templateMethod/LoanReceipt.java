package com.example.tmppp_library_management.templateMethod;

import com.example.tmppp_library_management.entity.Loan;
import java.time.format.DateTimeFormatter;

public class LoanReceipt extends ReceiptTemplate {
    private final Loan loan;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public LoanReceipt(Loan loan) {
        this.loan = loan;
    }

    @Override
    protected String getHeader() {
        return "        CHITANTA IMPRUMUT";
    }

    @Override
    protected String getBody() {
        StringBuilder sb = new StringBuilder();
        sb.append("ID Imprumut: #").append(loan.getLoanId()).append("\n");
        sb.append("Membru: ").append(loan.getUser().getUserName()).append("\n");
        sb.append("Carte: ").append(loan.getBookTitle()).append("\n");
        sb.append("Data imprumut: ").append(loan.getStartDate().format(dateFormatter)).append("\n");
        sb.append("Data returnare: ").append(loan.getReturnDate().format(dateFormatter)).append("\n");
        sb.append("\n");
        sb.append("Termen returnare: ").append(loan.getReturnDate().format(dateFormatter)).append("\n");
        sb.append("Penalitate intarziere: 1 leu/zi\n");
        return sb.toString();
    }

    @Override
    protected String getFooter() {
        return "     Va rugam sa returnati la timp!\n" +
                "     Multumim pentru colaborare!";
    }
}