package com.example.tmppp_library_management.services;

import com.example.tmppp_library_management.user.Member;
import com.example.tmppp_library_management.user.MemberType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemberService {
    private static MemberService instance;
    private Map<Integer, Member> members;
    private int nextMemberId;
    private int nextMembershipNumber;

    private MemberService() {
        this.members = new HashMap<>();
        this.nextMemberId = 1;
        this.nextMembershipNumber = 1;
    }

    public static MemberService getInstance() {
        if (instance == null) {
            instance = new MemberService();
        }
        return instance;
    }

    // ============ OPERATII CRUD ============

    public Member addMember(String name, String email, MemberType type) {
        String membershipNumber = generateMembershipNumber();
        Member member = new Member(nextMemberId++, name, email, type, membershipNumber);
        members.put(member.getUserId(), member);
        return member;
    }

    private String generateMembershipNumber() {
        return "M" + String.format("%03d", nextMembershipNumber++);
    }

    public Member getMember(int memberId) {
        return members.get(memberId);
    }

    public Member getMemberByMembership(String membershipNumber) {
        for (Member member : members.values()) {
            if (member.getMembershipNumber().equals(membershipNumber)) {
                return member;
            }
        }
        return null;
    }

    public List<Member> getAllMembers() {
        return new ArrayList<>(members.values());
    }

    public boolean updateMember(int memberId, String newName, String newEmail) {
        Member member = members.get(memberId);
        if (member != null) {
            member.setUserName(newName);
            member.setUserEmail(newEmail);
            return true;
        }
        return false;
    }

    public boolean updateMemberType(int memberId, MemberType newType) {
        Member member = members.get(memberId);
        if (member != null) {
            member.setMemberType(newType);
            return true;
        }
        return false;
    }

    public void deleteMember(int memberId) {
        members.remove(memberId);
    }

    // ============ OPERATII IMPRUMUTURI ============

    public void incrementLoans(int memberId) {
        Member member = members.get(memberId);
        if (member != null) {
            member.setCurrentLoans(member.getCurrentLoans() + 1);
        }
    }

    public void decrementLoans(int memberId) {
        Member member = members.get(memberId);
        if (member != null && member.getCurrentLoans() > 0) {
            member.setCurrentLoans(member.getCurrentLoans() - 1);
        }
    }

    public boolean canBorrow(int memberId) {
        Member member = members.get(memberId);
        if (member == null) return false;
        return member.getCurrentLoans() < member.getMaxBooks();
    }

    // ============ METODE DE CAUTARE ============

    public List<Member> findMembersByName(String name) {
        List<Member> results = new ArrayList<>();
        String searchTerm = name.toLowerCase();
        for (Member member : members.values()) {
            if (member.getUserName().toLowerCase().contains(searchTerm)) {
                results.add(member);
            }
        }
        return results;
    }

    public List<Member> findMembersByEmail(String email) {
        List<Member> results = new ArrayList<>();
        String searchTerm = email.toLowerCase();
        for (Member member : members.values()) {
            if (member.getUserEmail().toLowerCase().contains(searchTerm)) {
                results.add(member);
            }
        }
        return results;
    }

    // ============ AFISARE ============

    public void listAllMembers() {
        if (members.isEmpty()) {
            System.out.println("Nu exista membri inregistrati.");
            return;
        }

        System.out.println("\n LISTA MEMBRI:");
        System.out.println("┌──────┬────────────────────┬────────────────────┬────────────┬────────┬─────────────┐");
        System.out.println("│ ID   │ Nume               │ Email              │ Membership │ Tip    │ Imprumuturi │");
        System.out.println("├──────┼────────────────────┼────────────────────┼────────────┼────────┼─────────────┤");

        for (Member member : members.values()) {
            System.out.printf("│ %-4d │ %-18s │ %-18s │ %-10s │ %-6s │    %d/%d     │\n",
                    member.getUserId(),
                    truncate(member.getUserName(), 18),
                    truncate(member.getUserEmail(), 18),
                    member.getMembershipNumber(),
                    member.getMemberType(),
                    member.getCurrentLoans(),
                    member.getMaxBooks());
        }
        System.out.println("└──────┴────────────────────┴────────────────────┴────────────┴────────┴─────────────┘");
    }

    public void printMemberDetails(int memberId) {
        Member member = members.get(memberId);
        if (member == null) {
            System.out.println("Membru negasit!");
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

    // ============ STATISTICI ============

    public int getMemberCount() {
        return members.size();
    }

    public int getTotalActiveLoans() {
        int total = 0;
        for (Member member : members.values()) {
            total += member.getCurrentLoans();
        }
        return total;
    }

    // ============ UTILITARE ============

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}