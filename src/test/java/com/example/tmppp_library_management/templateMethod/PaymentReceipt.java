package com.example.tmppp_library_management.templateMethod;

import com.example.tmppp_library_management.entity.Payment;
import java.time.format.DateTimeFormatter;

public class PaymentReceipt extends ReceiptTemplate {
    private final Payment payment;
    private final String memberName;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public PaymentReceipt(Payment payment, String memberName) {
        this.payment = payment;
        this.memberName = memberName;
    }

    @Override
    protected String getHeader() {
        return "        CHITANTA PLATA";
    }

    @Override
    protected String getBody() {
        StringBuilder sb = new StringBuilder();
        sb.append("ID Plata: #").append(payment.getPaymentId()).append("\n");
        sb.append("Data: ").append(payment.getDate().format(dateFormatter)).append("\n");
        sb.append("Membru: ").append(memberName).append("\n");
        sb.append("Descriere: ").append(payment.getDescription()).append("\n");
        sb.append("Metoda plata: ").append(payment.getMethod()).append("\n");
        sb.append("Suma: ").append(formatPrice(payment.getAmount())).append("\n");
        return sb.toString();
    }

    @Override
    protected String getFooter() {
        return "        Va multumim!";
    }
}