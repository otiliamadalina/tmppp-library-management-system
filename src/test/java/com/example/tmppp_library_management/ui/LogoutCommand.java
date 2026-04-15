package com.example.tmppp_library_management.ui;

public class LogoutCommand implements Command {
    private LibraryDashboard dashboard;

    public LogoutCommand(LibraryDashboard dashboard) {
        this.dashboard = dashboard;
    }

    @Override
    public void execute() {
        dashboard.performLogout();
    }

    @Override
    public String getDescription() {
        return "Delogare";
    }

    @Override
    public boolean canUndo() {
        return false;
    }

    @Override
    public void undo() {
        // Nu se poate face undo la logout
    }
}