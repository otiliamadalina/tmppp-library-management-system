package com.example.tmppp_library_management.visitor;

public class StatisticsResult {
    private int totalBooks;
    private int fantasyBooks;
    private int romanceBooks;
    private double totalBookRevenue;

    private int totalNewspapers;
    private int localNewspapers;
    private int nationalNewspapers;

    private int totalLoans;
    private int activeLoans;
    private int overdueLoans;

    private int totalMembers;
    private int activeMembers;
    private int simpleMembers;
    private int studentMembers;
    private int professorMembers;

    public StatisticsResult() {}

    public int getTotalBooks() { return totalBooks; }
    public int getFantasyBooks() { return fantasyBooks; }
    public int getRomanceBooks() { return romanceBooks; }
    public double getTotalBookRevenue() { return totalBookRevenue; }
    public int getTotalNewspapers() { return totalNewspapers; }
    public int getLocalNewspapers() { return localNewspapers; }
    public int getNationalNewspapers() { return nationalNewspapers; }
    public int getTotalLoans() { return totalLoans; }
    public int getActiveLoans() { return activeLoans; }
    public int getOverdueLoans() { return overdueLoans; }
    public int getTotalMembers() { return totalMembers; }
    public int getActiveMembers() { return activeMembers; }
    public int getSimpleMembers() { return simpleMembers; }
    public int getStudentMembers() { return studentMembers; }
    public int getProfessorMembers() { return professorMembers; }

    void setTotalBooks(int totalBooks) { this.totalBooks = totalBooks; }
    void setFantasyBooks(int fantasyBooks) { this.fantasyBooks = fantasyBooks; }
    void setRomanceBooks(int romanceBooks) { this.romanceBooks = romanceBooks; }
    void setTotalBookRevenue(double totalBookRevenue) { this.totalBookRevenue = totalBookRevenue; }
    void setTotalNewspapers(int totalNewspapers) { this.totalNewspapers = totalNewspapers; }
    void setLocalNewspapers(int localNewspapers) { this.localNewspapers = localNewspapers; }
    void setNationalNewspapers(int nationalNewspapers) { this.nationalNewspapers = nationalNewspapers; }
    void setTotalLoans(int totalLoans) { this.totalLoans = totalLoans; }
    void setActiveLoans(int activeLoans) { this.activeLoans = activeLoans; }
    void setOverdueLoans(int overdueLoans) { this.overdueLoans = overdueLoans; }
    void setTotalMembers(int totalMembers) { this.totalMembers = totalMembers; }
    void setActiveMembers(int activeMembers) { this.activeMembers = activeMembers; }
    void setSimpleMembers(int simpleMembers) { this.simpleMembers = simpleMembers; }
    void setStudentMembers(int studentMembers) { this.studentMembers = studentMembers; }
    void setProfessorMembers(int professorMembers) { this.professorMembers = professorMembers; }

    public void printStatistics() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                        STATISTICI BIBLIOTECA                       ║");
        System.out.println("╠════════════════════════════════════════════════════════════════════╣");

        System.out.println("║                                                                     ║");
        System.out.println("║     CĂRȚI                                                           ║");
        System.out.println("║     Total cărți:                    " + String.format("%-30d", totalBooks) + "║");
        System.out.println("║     Fantasy:                        " + String.format("%-30d", fantasyBooks) + "║");
        System.out.println("║     Romance:                        " + String.format("%-30d", romanceBooks) + "║");
        System.out.println("║     Valoare totală stoc:            " + String.format("%-30.2f lei", totalBookRevenue) + "║");

        System.out.println("║                                                                     ║");
        System.out.println("║     ZIARE                                                           ║");
        System.out.println("║     Total ziare:                    " + String.format("%-30d", totalNewspapers) + "║");
        System.out.println("║     Locale:                         " + String.format("%-30d", localNewspapers) + "║");
        System.out.println("║     Naționale:                      " + String.format("%-30d", nationalNewspapers) + "║");

        System.out.println("║                                                                     ║");
        System.out.println("║     ÎMPRUMUTURI                                                     ║");
        System.out.println("║     Total împrumuturi:              " + String.format("%-30d", totalLoans) + "║");
        System.out.println("║     Împrumuturi active:             " + String.format("%-30d", activeLoans) + "║");
        System.out.println("║     Împrumuturi întârziate:         " + String.format("%-30d", overdueLoans) + "║");

        System.out.println("║                                                                     ║");
        System.out.println("║     MEMBRI                                                          ║");
        System.out.println("║     Total membri:                   " + String.format("%-30d", totalMembers) + "║");
        System.out.println("║     Membri activi (cu împrumuturi): " + String.format("%-30d", activeMembers) + "║");
        System.out.println("║     SIMPLE:                         " + String.format("%-30d", simpleMembers) + "║");
        System.out.println("║     STUDENT:                        " + String.format("%-30d", studentMembers) + "║");
        System.out.println("║     PROFESSOR:                      " + String.format("%-30d", professorMembers) + "║");

        System.out.println("╚════════════════════════════════════════════════════════════════════╝");
    }
}