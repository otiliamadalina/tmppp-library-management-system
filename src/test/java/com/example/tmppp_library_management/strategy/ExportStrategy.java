package com.example.tmppp_library_management.strategy;

import java.util.List;

public interface ExportStrategy {
    void export(List<String[]> data, String filename);
    String getExtension();
}