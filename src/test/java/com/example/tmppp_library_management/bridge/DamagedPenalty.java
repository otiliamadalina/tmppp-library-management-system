package com.example.tmppp_library_management.bridge;

public class DamagedPenalty extends Penalty {
    private double repairCost;

    public DamagedPenalty(PenaltyCalculator calculator, double repairCost) {
        super(calculator, "Penalitate pentru deteriorare");
        this.repairCost = repairCost;
    }

    @Override
    public double getAmount(int daysLate, double bookPrice) {
        return calculator.calculate(daysLate, bookPrice) + repairCost;
    }

    @Override
    public String getType() {
        return "Deteriorare";
    }
}