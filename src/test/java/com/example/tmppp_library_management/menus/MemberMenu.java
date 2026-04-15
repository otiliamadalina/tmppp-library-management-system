package com.example.tmppp_library_management.menus;

import com.example.tmppp_library_management.adapter.LibraryMemberAdapter;
import com.example.tmppp_library_management.services.LibrarianService;
import com.example.tmppp_library_management.user.Member;
import com.example.tmppp_library_management.user.MemberType;
import com.example.tmppp_library_management.services.MemberService;

public class MemberMenu {
    private final LibraryMemberAdapter memberAdapter;
    private final MemberService memberService;
    private final LibrarianService librarianService;

    public MemberMenu(LibrarianService librarianService) {
        this.librarianService = librarianService;
        this.memberService = MemberService.getInstance();
        this.memberAdapter = new LibraryMemberAdapter();
    }

    public void display(String token) {
        while (true) {
            System.out.println("\n--- MENIU MEMBRI ---");
            System.out.println("1. Adauga membru nou");
            System.out.println("2. Listeaza toti membrii");
            System.out.println("3. Cauta membru");
            System.out.println("4. Actualizeaza tip membru");
            System.out.println("5. Sterge membru");
            System.out.println("0. Inapoi");

            int choice = MenuUtils.readInt("Alege optiunea: ");

            switch (choice) {
                case 1 -> addMember(token);
                case 2 -> librarianService.listAllMembers(token);
                case 3 -> searchMemberSubMenu(token);
                case 4 -> updateMemberType(token);
                case 5 -> deleteMember(token);
                case 0 -> { return; }
                default -> System.out.println("Optiune invalida!");
            }
        }
    }

    public void listAllMembersSimple() {
        memberService.listAllMembers();
    }

    private void searchMemberSubMenu(String token) {
        while (true) {
            System.out.println("\n--- CAUTARE MEMBRU ---");
            System.out.println("1. Dupa ID");
            System.out.println("2. Dupa numar membership");
            System.out.println("3. Dupa nume");
            System.out.println("4. Dupa email");
            System.out.println("0. Inapoi");

            int choice = MenuUtils.readInt("Alege metoda de cautare: ");

            switch (choice) {
                case 1 -> findMemberById(token);
                case 2 -> findMemberByMembership(token);
                case 3 -> findMemberByName(token);
                case 4 -> findMemberByEmail(token);
                case 0 -> { return; }
                default -> System.out.println("Optiune invalida!");
            }
        }
    }

    private void addMember(String token) {
        System.out.println("\n--- ADAUGARE MEMBRU NOU ---");
        System.out.println("Tipuri disponibile: " + memberAdapter.getAvailableTypes());

        String type = MenuUtils.readString("Tip membru: ");

        // Folosim Chain of Responsibility pentru validare nume
        String name = MenuUtils.readValidatedName("Nume: ");
        if (name == null) {
            System.out.println("Operatiune anulata.");
            return;
        }

        // Folosim Chain of Responsibility pentru validare email (cu verificare duplicat)
        String email = MenuUtils.readValidatedEmail("Email: ");
        if (email == null) {
            System.out.println("Operatiune anulata.");
            return;
        }

        MemberType memberType = switch (type.toLowerCase()) {
            case "student" -> MemberType.STUDENT;
            case "professor" -> MemberType.PROFESSOR;
            default -> MemberType.SIMPLE;
        };

        try {
            Member member = librarianService.registerMember(token, name, email, memberType);
            System.out.println("✓ Membru adaugat cu succes");
            System.out.println("   ID: " + member.getUserId());
            System.out.println("   Nume: " + member.getUserName());
            System.out.println("   Email: " + member.getUserEmail());
            System.out.println("   Membership: " + member.getMembershipNumber());
            System.out.println("   Tip: " + member.getMemberType());
            System.out.println("   Max carti: " + member.getMaxBooks());
        } catch (IllegalArgumentException e) {
            System.out.println("✗ Eroare: " + e.getMessage());
        }
    }

    private void findMemberById(String token) {
        int id = MenuUtils.readIntWithCommands("ID membru (sau /show members): ", null, this::listAllMembersSimple, null, null);
        Member member = librarianService.findMember(token, id);
        displayMemberDetails(member);
    }

    private void findMemberByMembership(String token) {
        String membership = MenuUtils.readStringWithCommands("Numar membership (sau /show members): ", null, this::listAllMembersSimple, null, null);
        Member member = librarianService.findMemberByMembership(token, membership);
        displayMemberDetails(member);
    }

    private void findMemberByName(String token) {
        String name = MenuUtils.readStringWithCommands("Nume (sau /show members): ", null, this::listAllMembersSimple, null, null).toLowerCase();
        boolean found = false;

        System.out.println("\n--- REZULTATE CAUTARE ---");
        for (Member member : memberService.getAllMembers()) {
            if (member.getUserName().toLowerCase().contains(name)) {
                displayMemberSummary(member);
                found = true;
            }
        }

        if (!found) {
            System.out.println("Nu s-au gasit membri cu numele \"" + name + "\"");
        }
    }

    private void findMemberByEmail(String token) {
        String email = MenuUtils.readStringWithCommands("Email (sau /show members): ", null, this::listAllMembersSimple, null, null).toLowerCase();
        boolean found = false;

        System.out.println("\n--- REZULTATE CAUTARE ---");
        for (Member member : memberService.getAllMembers()) {
            if (member.getUserEmail().toLowerCase().contains(email)) {
                displayMemberSummary(member);
                found = true;
            }
        }

        if (!found) {
            System.out.println("Nu s-au gasit membri cu emailul \"" + email + "\"");
        }
    }

    private void displayMemberSummary(Member member) {
        System.out.printf("ID: %d | %s | %s | %s | %s | Imprumuturi: %d/%d\n",
                member.getUserId(),
                MenuUtils.truncate(member.getUserName(), 15),
                MenuUtils.truncate(member.getUserEmail(), 20),
                member.getMembershipNumber(),
                member.getMemberType(),
                member.getCurrentLoans(),
                member.getMaxBooks());
    }

    private void displayMemberDetails(Member member) {
        if (member == null) {
            System.out.println("Membru negasit");
            return;
        }

        System.out.println("\n--- DETALII MEMBRU ---");
        System.out.println("ID: " + member.getUserId());
        System.out.println("Nume: " + member.getUserName());
        System.out.println("Email: " + member.getUserEmail());
        System.out.println("Membership: " + member.getMembershipNumber());
        System.out.println("Tip: " + member.getMemberType());
        System.out.println("Max carti permise: " + member.getMaxBooks());
        System.out.println("Imprumuturi active: " + member.getCurrentLoans());
    }

    private int getHistoricalLoansCount(Member member) {
        return 0;
    }

    private void updateMemberType(String token) {
        int id = MenuUtils.readIntWithCommands("ID membru (sau /show members): ", null, this::listAllMembersSimple, null, null);
        Member member = memberService.getMember(id);

        if (member == null) {
            System.out.println("Membru negasit");
            return;
        }

        System.out.println("\nMembru: " + member.getUserName() + " (" + member.getMemberType() + ")");
        System.out.println("Imprumuturi active: " + member.getCurrentLoans() + "/" + member.getMaxBooks());
        System.out.println("\nTipuri disponibile:");
        System.out.println("1. SIMPLE (max 3 carti)");
        System.out.println("2. STUDENT (max 5 carti)");
        System.out.println("3. PROFESSOR (max 10 carti)");

        int tip = MenuUtils.readInt("Alege tip nou (1-3): ");
        MemberType newType;

        switch (tip) {
            case 1 -> newType = MemberType.SIMPLE;
            case 2 -> newType = MemberType.STUDENT;
            case 3 -> newType = MemberType.PROFESSOR;
            default -> {
                System.out.println("Tip invalid");
                return;
            }
        }

        librarianService.updateMemberType(token, id, newType);
        System.out.println("✓ Tip membru actualizat la " + newType);
    }

    private void deleteMember(String token) {
        int id = MenuUtils.readIntWithCommands("ID membru de sters (sau /show members): ", null, this::listAllMembersSimple, null, null);
        Member member = memberService.getMember(id);

        if (member == null) {
            System.out.println("Membru negasit");
            return;
        }

        System.out.println("\nMembru: " + member.getUserName());
        System.out.println("Imprumuturi active: " + member.getCurrentLoans());

        if (member.getCurrentLoans() > 0) {
            System.out.println("Membrul are imprumuturi active! Nu poate fi sters.");
            return;
        }

        String confirm = MenuUtils.readString("Confirmati stergerea? (da/nu): ");

        if (confirm.equalsIgnoreCase("da")) {
            librarianService.deleteMember(token, id);
            System.out.println("✓ Membru sters cu succes");
        }
    }
}