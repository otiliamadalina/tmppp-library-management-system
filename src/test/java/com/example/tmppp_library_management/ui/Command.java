package com.example.tmppp_library_management.ui;

public interface Command {
    void execute();
    String getDescription();
    boolean canUndo();
    void undo();
}