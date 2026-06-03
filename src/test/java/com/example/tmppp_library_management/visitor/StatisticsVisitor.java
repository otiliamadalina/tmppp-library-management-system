package com.example.tmppp_library_management.visitor;

import com.example.tmppp_library_management.book.Book;
import com.example.tmppp_library_management.book.FantasyBook;
import com.example.tmppp_library_management.book.RomanceBook;
import com.example.tmppp_library_management.newspaper.LocalNewspaper;
import com.example.tmppp_library_management.newspaper.NationalNewspaper;
import com.example.tmppp_library_management.newspaper.Newspaper;
import com.example.tmppp_library_management.entity.Loan;
import com.example.tmppp_library_management.user.Member;
import com.example.tmppp_library_management.user.MemberType;

import java.time.LocalDate;

public class StatisticsVisitor implements Visitor {
    private int bookCount = 0;
    private int fantasyCount = 0;
    private int romanceCount = 0;
    private double totalBookPrice = 0;

    private int newspaperCount = 0;
    private int localNewspaperCount = 0;
    private int nationalNewspaperCount = 0;

    private int totalLoans = 0;
    private int activeLoans = 0;
    private int overdueLoans = 0;


    private int totalMembers = 0;
    private int simpleMembers = 0;
    private int studentMembers = 0;
    private int professorMembers = 0;
    private int activeMembers = 0;

    @Override
    public void visit(Book book) {
        bookCount++;
        totalBookPrice += book.getPrice();

        if (book instanceof FantasyBook) {
            fantasyCount++;
        } else if (book instanceof RomanceBook) {
            romanceCount++;
        }
    }

    @Override
    public void visit(Newspaper newspaper) {
        newspaperCount++;

        if (newspaper instanceof LocalNewspaper) {
            localNewspaperCount++;
        } else if (newspaper instanceof NationalNewspaper) {
            nationalNewspaperCount++;
        }
    }

    @Override
    public void visit(Loan loan) {
        totalLoans++;

        if (loan.isActive()) {
            activeLoans++;
        }

        if (loan.isActive() && LocalDate.now().isAfter(loan.getReturnDate())) {
            overdueLoans++;
        }
    }

    @Override
    public void visit(Member member) {
        totalMembers++;

        MemberType type = member.getMemberType();
        if (type == MemberType.SIMPLE) {
            simpleMembers++;
        } else if (type == MemberType.STUDENT) {
            studentMembers++;
        } else if (type == MemberType.PROFESSOR) {
            professorMembers++;
        }

        if (member.getCurrentLoans() > 0) {
            activeMembers++;
        }
    }

    public StatisticsResult getResults() {
        StatisticsResult result = new StatisticsResult();

        result.setTotalBooks(bookCount);
        result.setFantasyBooks(fantasyCount);
        result.setRomanceBooks(romanceCount);
        result.setTotalBookRevenue(totalBookPrice);

        result.setTotalNewspapers(newspaperCount);
        result.setLocalNewspapers(localNewspaperCount);
        result.setNationalNewspapers(nationalNewspaperCount);

        result.setTotalLoans(totalLoans);
        result.setActiveLoans(activeLoans);
        result.setOverdueLoans(overdueLoans);

        result.setTotalMembers(totalMembers);
        result.setActiveMembers(activeMembers);
        result.setSimpleMembers(simpleMembers);
        result.setStudentMembers(studentMembers);
        result.setProfessorMembers(professorMembers);

        return result;
    }
}