package com.example.tmppp_library_management.entity;

import java.time.LocalDate;

public class Payment {
    private int paymentId;
    private double amount;
    private LocalDate date;
    private String method; // "CASH", "CARD"
    private String description;
    private Integer memberId; // null pentru vizitatori

    public Payment(int paymentId, double amount, String method, String description, Integer memberId) {
        this.paymentId = paymentId;
        this.amount = amount;
        this.date = LocalDate.now();
        this.method = method;
        this.description = description;
        this.memberId = memberId;
    }

    public int getPaymentId() { return paymentId; }
    public double getAmount() { return amount; }
    public LocalDate getDate() { return date; }
    public String getMethod() { return method; }
    public String getDescription() { return description; }
    public Integer getMemberId() { return memberId; }

    @Override
    public String toString() {
        return String.format("Payment[id=%d, amount=%.2f, date=%s, method=%s, desc=%s]",
                paymentId, amount, date, method, description);
    }
}
