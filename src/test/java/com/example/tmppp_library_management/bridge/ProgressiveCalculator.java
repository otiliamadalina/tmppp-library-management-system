package com.example.tmppp_library_management.bridge;

public class ProgressiveCalculator implements PenaltyCalculator {
    private double firstWeekRate;
    private double secondWeekRate;
    private double afterTwoWeeksRate;

    public ProgressiveCalculator(double firstWeekRate, double secondWeekRate, double afterTwoWeeksRate) {
        this.firstWeekRate = firstWeekRate;
        this.secondWeekRate = secondWeekRate;
        this.afterTwoWeeksRate = afterTwoWeeksRate;
    }

    @Override
    public double calculate(int daysLate, double bookPrice) {
        double total = 0;

        if (daysLate <= 7) {
            total = daysLate * firstWeekRate;
        } else if (daysLate <= 14) {
            total = 7 * firstWeekRate + (daysLate - 7) * secondWeekRate;
        } else {
            total = 7 * firstWeekRate + 7 * secondWeekRate + (daysLate - 14) * afterTwoWeeksRate;
        }

        return total;
    }
}
