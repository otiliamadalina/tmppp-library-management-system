package com.example.tmppp_library_management.bridge;

public class LostPenalty extends Penalty {
    private double bookPrice;

    public LostPenalty(PenaltyCalculator calculator, double bookPrice) {
        super(calculator, "Penalitate pentru pierdere");
        this.bookPrice = bookPrice;
    }

    @Override
    public double getAmount(int daysLate, double bookPrice) {
        return calculator.calculate(daysLate, this.bookPrice) + this.bookPrice;
    }

    @Override
    public String getType() {
        return "Pierdere";
    }
}
