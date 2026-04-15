package com.example.tmppp_library_management.bridge;

public class FixedCalculator implements PenaltyCalculator {
    private double amountPerDay;

    public FixedCalculator(double amountPerDay) {
        this.amountPerDay = amountPerDay;
    }

    @Override
    public double calculate(int daysLate, double bookPrice) {
        return daysLate * amountPerDay;
    }
}
