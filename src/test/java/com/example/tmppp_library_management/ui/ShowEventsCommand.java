package com.example.tmppp_library_management.ui;


public class ShowEventsCommand implements Command {
    private LibraryDashboard dashboard;
    private String previousPanel;

    public ShowEventsCommand(LibraryDashboard dashboard) {
        this.dashboard = dashboard;
    }

    @Override
    public void execute() {
        previousPanel = dashboard.getCurrentPanelName();
        dashboard.showPanel("events");
    }

    @Override
    public String getDescription() {
        return "Afisare evenimente";
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