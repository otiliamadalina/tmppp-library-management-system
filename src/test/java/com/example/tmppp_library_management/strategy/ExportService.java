package com.example.tmppp_library_management.strategy;

import java.util.ArrayList;
import java.util.List;

public class ExportService {
    private ExportStrategy strategy;
    private List<String[]> data;

    public ExportService() {
        this.data = new ArrayList<>();
    }

    public void setStrategy(ExportStrategy strategy) {
        this.strategy = strategy;
    }

    public void addRow(String[] row) {
        data.add(row);
    }

    public void clearData() {
        data.clear();
    }

    public void exportData(String filename) {
        if (strategy != null && !data.isEmpty()) {
            strategy.export(data, filename);
        }
    }
}