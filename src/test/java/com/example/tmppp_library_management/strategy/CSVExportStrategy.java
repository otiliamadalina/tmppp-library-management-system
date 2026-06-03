package com.example.tmppp_library_management.strategy;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

public class CSVExportStrategy implements ExportStrategy {

    @Override
    public void export(List<String[]> data, String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename + ".csv"))) {
            for (String[] row : data) {
                writer.println(String.join(",", row));
            }
            System.out.println("CSV exportat: " + filename + ".csv");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getExtension() {
        return "csv";
    }
}