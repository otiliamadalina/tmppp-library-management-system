package com.example.tmppp_library_management.facade;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ReturnReceipt {
    private int receiptId;
    private String memberName;
    private String bookTitle;
    private LocalDate returnDate;
    private LocalDate dueDate;
    private int daysLate;
    private double penalty;
    private boolean penaltyPaid;

    public ReturnReceipt(int receiptId, String memberName, String bookTitle,
                         LocalDate returnDate, LocalDate dueDate,
                         int daysLate, double penalty, boolean penaltyPaid) {
        this.receiptId = receiptId;
        this.memberName = memberName;
        this.bookTitle = bookTitle;
        this.returnDate = returnDate;
        this.dueDate = dueDate;
        this.daysLate = daysLate;
        this.penalty = penalty;
        this.penaltyPaid = penaltyPaid;
    }

    public void print() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        System.out.println("\n=== CHITANTA RETURNARE ===");
        System.out.println("ID chitanta: " + receiptId);
        System.out.println("Membru: " + memberName);
        System.out.println("Carte: " + bookTitle);
        System.out.println("Data returnare: " + returnDate.format(formatter));
        System.out.println("Data scadenta: " + dueDate.format(formatter));

        if (daysLate > 0) {
            System.out.println("Zile intarziere: " + daysLate);
            System.out.println("Penalitate: " + penalty + " lei");
            System.out.println("Status plata: " + (penaltyPaid ? "ACHITAT" : "NECHITAT"));
        } else {
            System.out.println("Returnare la timp. Fara penalitati.");
        }
        System.out.println("===========================");
    }
}