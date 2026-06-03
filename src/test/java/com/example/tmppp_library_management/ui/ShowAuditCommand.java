package com.example.tmppp_library_management.ui;

import com.example.tmppp_library_management.ui.Command;
import com.example.tmppp_library_management.ui.LibraryDashboard;

class ShowAuditCommand implements Command {
    private LibraryDashboard dashboard;
    private String previousPanel;

    public ShowAuditCommand(LibraryDashboard dashboard) {
        this.dashboard = dashboard;
    }

    @Override
    public void execute() {
        this.previousPanel = dashboard.getCurrentPanelName();
        dashboard.showPanel("audit");
    }

    @Override
    public String getDescription() {
        return "Deschide panoul Audit Log";
    }

    @Override
    public boolean canUndo() {
        return previousPanel != null && !previousPanel.equals("audit");
    }

    @Override
    public void undo() {
        if (canUndo()) {
            dashboard.showPanel(previousPanel);
        }
    }
}