package com.example.tmppp_library_management.memento;

import com.example.tmppp_library_management.services.LoanService;
import com.example.tmppp_library_management.interfaces.IBorrowable;
import com.example.tmppp_library_management.user.Member;
import com.example.tmppp_library_management.entity.Loan;

public class LoanCaretaker {
    private final LoanService loanService;

    public LoanCaretaker(LoanService loanService) {
        this.loanService = loanService;
    }

    public Loan performLoan(Member member, IBorrowable item) {
        return loanService.createLoan(member, item);
    }

    public boolean undo() {
        return loanService.undoLastLoan();
    }

    public boolean canUndo() {
        return loanService.canUndo();
    }

    public String getUndoDescription() {
        return loanService.getLastUndoDescription();
    }

    public int getUndoHistorySize() {
        return loanService.getUndoHistorySize();
    }

    public void clearHistory() {
        loanService.clearHistory();
    }
}