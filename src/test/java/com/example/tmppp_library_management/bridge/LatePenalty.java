package com.example.tmppp_library_management.bridge;

public class LatePenalty extends Penalty {

    public LatePenalty(PenaltyCalculator calculator) {
        super(calculator, "Penalitate pentru intarziere");
    }

    @Override
    public String getType() {
        return "Intarziere";
    }
}
