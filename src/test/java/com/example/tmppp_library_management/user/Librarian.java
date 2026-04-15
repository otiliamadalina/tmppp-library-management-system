package com.example.tmppp_library_management.user;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Librarian extends User {
    private String username;
    private String password;
    private LocalDateTime lastLogin;
    private List<String> actionLog;

    public Librarian(int userId, String userName, String userEmail,
                     String username, String password) {
        super(userId, userName, userEmail);
        this.username = username;
        this.password = password;
        this.actionLog = new ArrayList<>();
        this.lastLogin = null;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public LocalDateTime getLastLogin() { return lastLogin; }
    public List<String> getActionLog() { return new ArrayList<>(actionLog); }

    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public void addToLog(String action) {
        String logEntry = LocalDateTime.now() + " - " + action;
        actionLog.add(logEntry);
        System.out.println("[LOG] " + logEntry);
    }

    public void printActionLog() {
        System.out.println("\n--- ISTORIC ACTIUNI PENTRU " + username + " ---");
        if (actionLog.isEmpty()) {
            System.out.println("Nu exista actiuni inregistrate");
        } else {
            for (String entry : actionLog) {
                System.out.println(entry);
            }
        }
    }

    @Override
    public String toString() {
        return "Librarian: " + userName + " (username: " + username + ", ID: " + userId + ")";
    }
}