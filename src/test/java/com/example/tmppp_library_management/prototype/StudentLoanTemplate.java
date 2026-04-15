package com.example.tmppp_library_management.prototype;

import com.example.tmppp_library_management.user.MemberType;

public class StudentLoanTemplate extends LoanTemplate {
    private String university;

    public StudentLoanTemplate(int templateId, String templateName, String university) {
        super(templateId, templateName, 21, MemberType.STUDENT);
        this.university = university;
    }

    public StudentLoanTemplate(StudentLoanTemplate other) {
        super(other);
        this.university = other.university;
    }

    @Override
    public LoanTemplate clone() {
        return new StudentLoanTemplate(this);
    }

    public String getUniversity() { return university; }
    public void setUniversity(String university) { this.university = university; }
}