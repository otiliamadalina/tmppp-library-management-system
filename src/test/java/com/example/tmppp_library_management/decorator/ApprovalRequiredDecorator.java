package com.example.tmppp_library_management.decorator;

import com.example.tmppp_library_management.book.Book;

public class ApprovalRequiredDecorator extends BookDecorator {
    private boolean approved;
    private String approvedBy;

    public ApprovalRequiredDecorator(Book book) {
        super(book);
        this.approved = false;
        this.approvedBy = null;
    }

    @Override
    public void borrowItem() {
        if (!approved) {
            System.out.println("Cartea necesita aprobare inainte de imprumut");
            return;
        }
        super.borrowItem();
    }

    @Override
    public String getDescription() {
        String status = approved ? " [Aprobat de " + approvedBy + "]" : " [Asteapta aprobare]";
        return book.getDescription() + status;
    }

    public void approve(String librarianName) {
        this.approved = true;
        this.approvedBy = librarianName;
        System.out.println("Cartea a fost aprobata de " + librarianName);
    }

    public boolean isApproved() { return approved; }
    public String getApprovedBy() { return approvedBy; }
}