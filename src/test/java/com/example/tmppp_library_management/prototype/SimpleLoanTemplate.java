package com.example.tmppp_library_management.prototype;

import com.example.tmppp_library_management.user.MemberType;

public class SimpleLoanTemplate extends LoanTemplate {

    public SimpleLoanTemplate(int templateId, String templateName) {
        super(templateId, templateName, 14, MemberType.SIMPLE);
    }

    public SimpleLoanTemplate(SimpleLoanTemplate other) {
        super(other);
    }

    @Override
    public LoanTemplate clone() {
        return new SimpleLoanTemplate(this);
    }
}