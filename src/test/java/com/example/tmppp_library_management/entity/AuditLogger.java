 package com.example.tmppp_library_management.entity;

 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.time.LocalDateTime;
 import java.time.format.DateTimeFormatter;

 public class AuditLogger {
     private static AuditLogger instance;
     private static final String LOG_FILE = "audit_log.txt";
     private StringBuilder recentLogs;
     private String currentUser = "Necunoscut";
     private static final int MAX_LOG_SIZE = 50000;

     private AuditLogger() {
         recentLogs = new StringBuilder();
     }

     public static AuditLogger getInstance() {
         if (instance == null) {
             instance = new AuditLogger();
         }
         return instance;
     }

     public void setCurrentUser(String username) {
         this.currentUser = username;
         log("AUTENTIFICARE", "Utilizatorul '" + username + "' s-a conectat");
     }

     public void log(String action, String details) {
         String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
         String logEntry = String.format("[%s] [%s] %s: %s%n", timestamp, currentUser, action, details);

         recentLogs.insert(0, logEntry);
         if (recentLogs.length() > MAX_LOG_SIZE) {
             recentLogs.setLength(MAX_LOG_SIZE);
         }

         System.out.println("[LOG] " + timestamp + " | " + currentUser + " | " + action + ": " + details);

         try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
             writer.print(logEntry);
         } catch (IOException e) {
             System.err.println("Eroare la scrierea logului: " + e.getMessage());
         }
     }

     public String getRecentLogs() {
         return recentLogs.toString();
     }

     public void clearRecentLogs() {
         recentLogs.setLength(0);
         log("SISTEM", "Istoricul logurilor a fost sters");
     }
 }