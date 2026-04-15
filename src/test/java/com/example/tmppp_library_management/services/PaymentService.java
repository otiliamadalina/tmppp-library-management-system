package com.example.tmppp_library_management.services;

import com.example.tmppp_library_management.entity.Payment;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PaymentService {
    private static PaymentService instance;
    private List<Payment> payments;
    private int nextPaymentId;

    private PaymentService() {
        this.payments = new ArrayList<>();
        this.nextPaymentId = 1;
    }

    public static PaymentService getInstance() {
        if (instance == null) {
            instance = new PaymentService();
        }
        return instance;
    }

    public Payment processPayment(double amount, String method, String description, Integer memberId) {
        Payment payment = new Payment(nextPaymentId++, amount, method, description, memberId);
        payments.add(payment);
        return payment;
    }

    public List<Payment> getPaymentsForMember(int memberId) {
        List<Payment> result = new ArrayList<>();
        for (Payment p : payments) {
            if (p.getMemberId() != null && p.getMemberId() == memberId) {
                result.add(p);
            }
        }
        return result;
    }

    public double getTotalForToday() {
        LocalDate today = LocalDate.now();
        double total = 0;
        for (Payment p : payments) {
            if (p.getDate().equals(today)) {
                total += p.getAmount();
            }
        }
        return total;
    }

    public void listAllPayments() {
        if (payments.isEmpty()) {
            System.out.println("Nu exista plati inregistrate.");
            return;
        }

        System.out.println("\n=== LISTA PLATI ===");
        for (Payment p : payments) {
            System.out.println(p);
        }
        System.out.println("Total azi: " + getTotalForToday() + " lei");
    }

    public List<Payment> getAllPayments() {
        return new ArrayList<>(payments);
    }
}