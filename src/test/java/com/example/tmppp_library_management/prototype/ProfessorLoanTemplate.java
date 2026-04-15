package com.example.tmppp_library_management.prototype;

import com.example.tmppp_library_management.user.MemberType;

public class ProfessorLoanTemplate extends LoanTemplate {
    private String department;

    public ProfessorLoanTemplate(int templateId, String templateName, String department) {
        super(templateId, templateName, 30, MemberType.PROFESSOR);
        this.department = department;
    }

    public ProfessorLoanTemplate(ProfessorLoanTemplate other) {
        super(other);
        this.department = other.department;
    }

    @Override
    public LoanTemplate clone() {
        return new ProfessorLoanTemplate(this);
    }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}