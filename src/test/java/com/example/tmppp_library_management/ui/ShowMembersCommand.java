package com.example.tmppp_library_management.ui;

public class ShowMembersCommand implements Command {
    private LibraryDashboard dashboard;
    private String previousPanel;

    public ShowMembersCommand(LibraryDashboard dashboard) {
        this.dashboard = dashboard;
    }

    @Override
    public void execute() {
        previousPanel = dashboard.getCurrentPanelName();
        dashboard.showPanel("members");
    }

    @Override
    public String getDescription() {
        return "Afisare membri";
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