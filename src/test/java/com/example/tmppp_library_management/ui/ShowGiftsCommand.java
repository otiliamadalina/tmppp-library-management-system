package com.example.tmppp_library_management.ui;


public class ShowGiftsCommand implements Command {
    private LibraryDashboard dashboard;
    private String previousPanel;

    public ShowGiftsCommand(LibraryDashboard dashboard) {
        this.dashboard = dashboard;
    }

    @Override
    public void execute() {
        previousPanel = dashboard.getCurrentPanelName();
        dashboard.showPanel("gifts");
    }

    @Override
    public String getDescription() {
        return "Afisare pachete cadou";
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