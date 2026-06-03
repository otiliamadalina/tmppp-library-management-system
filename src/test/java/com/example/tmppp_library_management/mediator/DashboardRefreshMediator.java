package com.example.tmppp_library_management.mediator;

import javax.swing.JTextArea;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

public class DashboardRefreshMediator {
    private static DashboardRefreshMediator instance;

    private AbstractTableModel componentA;  // BooksTableModel
    private AbstractTableModel componentB;  // ActiveLoansTableModel
    private AbstractTableModel componentC;  // MembersTableModel
    private AbstractTableModel componentD;  // ClosedLoansTableModel
    private JTextArea componentE;           // StatsArea
    private JTable componentF;              // LoansTable (pentru refresh vizual)

    private DashboardRefreshMediator() {}

    public static DashboardRefreshMediator getInstance() {
        if (instance == null) {
            instance = new DashboardRefreshMediator();
        }
        return instance;
    }

    public void setComponentA(AbstractTableModel componentA) { this.componentA = componentA; }
    public void setComponentB(AbstractTableModel componentB) { this.componentB = componentB; }
    public void setComponentC(AbstractTableModel componentC) { this.componentC = componentC; }
    public void setComponentD(AbstractTableModel componentD) { this.componentD = componentD; }
    public void setComponentE(JTextArea componentE) { this.componentE = componentE; }
    public void setComponentF(JTable componentF) { this.componentF = componentF; }

    public void notify(Object sender) {
        if (sender == componentA) {
            reactOnA();
        } else if (sender == componentB) {
            reactOnB();
        } else if (sender == componentC) {
            reactOnC();
        } else if (sender == componentD) {
            reactOnD();
        } else if (sender == componentE) {
            reactOnE();
        }
    }


    private void reactOnA() {
        System.out.println("[Mediator] BooksTableModel changed - refreshing all tables");
        refreshAll();
    }

    private void reactOnB() {
        System.out.println("[Mediator] ActiveLoansTableModel changed - refreshing loans and stats");
        refreshLoans();
        refreshStats();
    }

    private void reactOnC() {
        System.out.println("[Mediator] MembersTableModel changed - refreshing members");
        refreshMembers();
    }

    private void reactOnD() {
        System.out.println("[Mediator] ClosedLoansTableModel changed - refreshing loans");
        refreshLoans();
    }

    private void reactOnE() {
        System.out.println("[Mediator] StatsArea changed - refreshing stats only");
        refreshStats();
    }

    public void refreshAll() {
        refreshBooks();
        refreshLoans();
        refreshMembers();
        refreshStats();
        System.out.println("[Mediator] All components refreshed");
    }

    public void refreshBooks() {
        if (componentA != null) {
            componentA.fireTableDataChanged();
            System.out.println("[Mediator] Books table refreshed");
        }
    }

    public void refreshLoans() {
        if (componentB != null) {
            componentB.fireTableDataChanged();
        }
        if (componentD != null) {
            componentD.fireTableDataChanged();
        }
        if (componentF != null) {
            componentF.repaint();
        }
        System.out.println("[Mediator] Loans tables refreshed");
    }

    public void refreshMembers() {
        if (componentC != null) {
            componentC.fireTableDataChanged();
            System.out.println("[Mediator] Members table refreshed");
        }
    }

    public void refreshStats() {
        // Stats area is updated by the caller with new text
        System.out.println("[Mediator] Stats area refresh requested");
    }

    public void updateStatsText(String newText) {
        if (componentE != null) {
            componentE.setText(newText);
            System.out.println("[Mediator] Stats text updated");
        }
    }
}