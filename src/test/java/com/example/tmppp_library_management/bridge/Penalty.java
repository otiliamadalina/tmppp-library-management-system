package com.example.tmppp_library_management.bridge;

public abstract class Penalty {
    protected PenaltyCalculator calculator;
    protected String description;

    public Penalty(PenaltyCalculator calculator, String description) {
        this.calculator = calculator;
        this.description = description;
    }

    public double getAmount(int daysLate, double bookPrice) {
        return calculator.calculate(daysLate, bookPrice);
    }

    public String getDescription() {
        return description;
    }

    public abstract String getType();
}
