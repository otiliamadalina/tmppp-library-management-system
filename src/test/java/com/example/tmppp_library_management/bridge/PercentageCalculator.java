package com.example.tmppp_library_management.bridge;

public class PercentageCalculator implements PenaltyCalculator {
    private double percentage;

    public PercentageCalculator(double percentage) {
        this.percentage = percentage;
    }

    @Override
    public double calculate(int daysLate, double bookPrice) {
        return bookPrice * (percentage / 100) * daysLate;
    }
}
