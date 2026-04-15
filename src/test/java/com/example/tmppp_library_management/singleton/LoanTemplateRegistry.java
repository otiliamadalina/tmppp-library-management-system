package com.example.tmppp_library_management.singleton;

import com.example.tmppp_library_management.user.MemberType;
import com.example.tmppp_library_management.prototype.LoanTemplate;
import com.example.tmppp_library_management.prototype.SimpleLoanTemplate;
import com.example.tmppp_library_management.prototype.StudentLoanTemplate;
import com.example.tmppp_library_management.prototype.ProfessorLoanTemplate;
import java.util.HashMap;
import java.util.Map;

public class LoanTemplateRegistry {
    private static LoanTemplateRegistry instance;
    private Map<MemberType, LoanTemplate> templates;

    private LoanTemplateRegistry() {
        templates = new HashMap<>();
        initializeDefaultTemplates();
    }

    public static LoanTemplateRegistry getInstance() {
        if (instance == null) {
            instance = new LoanTemplateRegistry();
        }
        return instance;
    }

    private void initializeDefaultTemplates() {
        templates.put(MemberType.SIMPLE,
                new SimpleLoanTemplate(1, "Imprumut Simplu"));
        templates.put(MemberType.STUDENT,
                new StudentLoanTemplate(2, "Imprumut Student", "Universitate"));
        templates.put(MemberType.PROFESSOR,
                new ProfessorLoanTemplate(3, "Imprumut Profesor", "Departament"));
    }

    public LoanTemplate getTemplateForMember(MemberType type) {
        LoanTemplate original = templates.get(type);
        if (original != null) {
            return (LoanTemplate) original.clone();
        }
        return null;
    }

    public void listTemplates() {
        System.out.println("\nTEMPLATE-URI IMPRUMUT:");
        for (Map.Entry<MemberType, LoanTemplate> entry : templates.entrySet()) {
            LoanTemplate t = entry.getValue();
            System.out.println("  - " + t.getDescription());
        }
    }
}