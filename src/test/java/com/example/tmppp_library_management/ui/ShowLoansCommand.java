package com.example.tmppp_library_management.ui;


public class ShowLoansCommand implements Command {
    private LibraryDashboard dashboard;
    private String previousPanel;

    public ShowLoansCommand(LibraryDashboard dashboard) {
        this.dashboard = dashboard;
    }

    @Override
    public void execute() {
        previousPanel = dashboard.getCurrentPanelName();
        dashboard.showPanel("loans");
    }

    @Override
    public String getDescription() {
        return "Afisare imprumuturi";
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