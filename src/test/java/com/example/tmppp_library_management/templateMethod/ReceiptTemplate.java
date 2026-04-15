package com.example.tmppp_library_management.templateMethod;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class ReceiptTemplate {

    protected static final String SEPARATOR = "========================================";
    protected static final String HEADER_TITLE = "          BIBLIOTECA CENTRALA";

    public final String generateReceipt() {
        StringBuilder sb = new StringBuilder();
        sb.append(SEPARATOR).append("\n");
        sb.append(HEADER_TITLE).append("\n");
        sb.append(SEPARATOR).append("\n");
        sb.append(getTimestamp()).append("\n");
        sb.append(SEPARATOR).append("\n");
        sb.append(getHeader()).append("\n");
        sb.append(getBody()).append("\n");
        sb.append(getFooter()).append("\n");
        sb.append(SEPARATOR).append("\n");
        return sb.toString();
    }

    public final void printReceipt() {
        System.out.println(generateReceipt());
    }

    protected String getSeparator() {
        return SEPARATOR;
    }

    protected String getHeaderTitle() {
        return HEADER_TITLE;
    }

    protected String getTimestamp() {
        return "Data: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
    }

    protected String formatPrice(double price) {
        return String.format("%.2f lei", price);
    }

    protected abstract String getHeader();
    protected abstract String getBody();
    protected abstract String getFooter();
}