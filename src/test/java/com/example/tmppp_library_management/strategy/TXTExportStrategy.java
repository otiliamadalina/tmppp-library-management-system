package com.example.tmppp_library_management.strategy;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

public class TXTExportStrategy implements ExportStrategy {

    @Override
    public void export(List<String[]> data, String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename + ".txt"))) {
            for (String[] row : data) {
                writer.println(String.join(" | ", row));
            }
            System.out.println("TXT exportat: " + filename + ".txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getExtension() {
        return "txt";
    }
}