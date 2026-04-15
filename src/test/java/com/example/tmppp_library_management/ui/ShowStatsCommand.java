package com.example.tmppp_library_management.ui;

public class ShowStatsCommand implements Command {
    private LibraryDashboard dashboard;
    private String previousPanel;

    public ShowStatsCommand(LibraryDashboard dashboard) {
        this.dashboard = dashboard;
    }

    @Override
    public void execute() {
        previousPanel = dashboard.getCurrentPanelName();
        dashboard.showPanel("stats");
    }

    @Override
    public String getDescription() {
        return "Afisare statistici";
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