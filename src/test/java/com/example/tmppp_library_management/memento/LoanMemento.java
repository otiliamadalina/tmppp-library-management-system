package com.example.tmppp_library_management.memento;

import com.example.tmppp_library_management.user.MemberType;
import java.time.LocalDateTime;

public class LoanMemento {
    private final LocalDateTime timestamp;
    private final int memberId;
    private final String memberName;
    private final String memberEmail;
    private final MemberType memberType;
    private final int memberOldLoans;
    private final String isbn;
    private final String bookTitle;
    private final int oldStock;
    private final int loanId;
    private final LoanState loanState;

    public LoanMemento(int memberId, String memberName, String memberEmail, MemberType memberType,
                       int memberOldLoans, String isbn, String bookTitle, int oldStock, int loanId) {
        this.timestamp = LocalDateTime.now();
        this.memberId = memberId;
        this.memberName = memberName;
        this.memberEmail = memberEmail;
        this.memberType = memberType;
        this.memberOldLoans = memberOldLoans;
        this.isbn = isbn;
        this.bookTitle = bookTitle;
        this.oldStock = oldStock;
        this.loanId = loanId;
        this.loanState = LoanState.ACTIVE;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public int getMemberId() { return memberId; }
    public String getMemberName() { return memberName; }
    public String getMemberEmail() { return memberEmail; }
    public MemberType getMemberType() { return memberType; }
    public int getMemberOldLoans() { return memberOldLoans; }
    public String getIsbn() { return isbn; }
    public String getBookTitle() { return bookTitle; }
    public int getOldStock() { return oldStock; }
    public int getLoanId() { return loanId; }
    public LoanState getLoanState() { return loanState; }

    public String getDescription() {
        return String.format("[%s] Loan #%d: %s borrowed '%s' (Member: %s, Stock was: %d)",
                timestamp, loanId, memberName, bookTitle, memberType, oldStock);
    }
}