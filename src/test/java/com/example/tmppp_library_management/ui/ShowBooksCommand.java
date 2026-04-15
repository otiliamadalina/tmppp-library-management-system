package com.example.tmppp_library_management.ui;

public class ShowBooksCommand implements Command {
    private LibraryDashboard dashboard;
    private String previousPanel;

    public ShowBooksCommand(LibraryDashboard dashboard) {
        this.dashboard = dashboard;
    }

    @Override
    public void execute() {
        previousPanel = dashboard.getCurrentPanelName();
        dashboard.showPanel("books");
    }

    @Override
    public String getDescription() {
        return "Afisare carti";
    }

    @Override
    public boolean canUndo() {
        return previousPanel != null;
    }

    @Override
    public void undo() {
        if (previousPanel != null) {
            dashboard.showPanel(previousPanel);
        }
    }
}