package com.example.tmppp_library_management.visitor;

import com.example.tmppp_library_management.book.Book;
import com.example.tmppp_library_management.entity.Loan;
import com.example.tmppp_library_management.newspaper.Newspaper;
import com.example.tmppp_library_management.user.Member;

public interface Visitor {
    void visit(Book book);
    void visit(Loan loan);
    void visit(Newspaper newspaper);
    void visit(Member member);
}
