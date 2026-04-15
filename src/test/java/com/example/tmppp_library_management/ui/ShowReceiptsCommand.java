package com.example.tmppp_library_management.ui;

public class ShowReceiptsCommand implements Command {
    private LibraryDashboard dashboard;
    private String previousPanel;

    public ShowReceiptsCommand(LibraryDashboard dashboard) {
        this.dashboard = dashboard;
    }

    @Override
    public void execute() {
        previousPanel = dashboard.getCurrentPanelName();
        dashboard.showPanel("receipts");
    }

    @Override
    public String getDescription() {
        return "Afisare chitante";
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