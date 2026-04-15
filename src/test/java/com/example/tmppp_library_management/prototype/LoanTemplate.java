package com.example.tmppp_library_management.prototype;

import com.example.tmppp_library_management.book.Book;
import com.example.tmppp_library_management.entity.Loan;
import com.example.tmppp_library_management.user.Member;
import com.example.tmppp_library_management.user.MemberType;

import java.time.LocalDate;

public abstract class LoanTemplate implements Prototype {
    protected int templateId;
    protected String templateName;
    protected int defaultDuration;
    protected MemberType applicableTo;
    protected String notes;

    public LoanTemplate(int templateId, String templateName,
                        int defaultDuration, MemberType applicableTo) {
        this.templateId = templateId;
        this.templateName = templateName;
        this.defaultDuration = defaultDuration;
        this.applicableTo = applicableTo;
        this.notes = "";
    }

    public LoanTemplate(LoanTemplate other) {
        this.templateId = other.templateId;
        this.templateName = other.templateName;
        this.defaultDuration = other.defaultDuration;
        this.applicableTo = other.applicableTo;
        this.notes = other.notes;
    }

    @Override
    public abstract Prototype clone();

    public Loan createLoan(Member member, Book book) {
        if (member.getMemberType() != this.applicableTo) {
            throw new IllegalArgumentException(
                    "Acest template e pentru " + applicableTo +
                            ", nu pentru " + member.getMemberType());
        }

        if (!member.canBorrow()) {
            throw new IllegalStateException(
                    "Membrul a atins numarul maxim de imprumuturi");
        }

        Loan loan = new Loan();
        loan.setUser(member);
        loan.setItem(book);
        loan.setStartDate(LocalDate.now());
        loan.setReturnDate(LocalDate.now().plusDays(defaultDuration));
        loan.setActive(true);

        member.setCurrentLoans(member.getCurrentLoans() + 1);

        return loan;
    }

    public int getTemplateId() {
        return templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public int getDefaultDuration() {
        return defaultDuration;
    }

    public void setDefaultDuration(int duration) {
        this.defaultDuration = duration;
    }

    public MemberType getApplicableTo() {
        return this.applicableTo;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getDescription() {
        return templateName + " - " + defaultDuration + " zile";
    }
}