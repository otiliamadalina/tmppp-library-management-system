package com.example.tmppp_library_management.adapter;

import com.example.tmppp_library_management.user.Member;
import com.example.tmppp_library_management.services.MemberService;
import java.util.HashMap;
import java.util.Map;

public class LibraryMemberAdapter implements MemberInput {
    private Map<String, MemberInput> inputMethods;
    private MemberService memberService;

    public LibraryMemberAdapter() {
        this.memberService = MemberService.getInstance();
        this.inputMethods = new HashMap<>();

        inputMethods.put("student", new StudentInput());
        inputMethods.put("professor", new ProfessorInput());
        inputMethods.put("simple", new SimpleMemberInput());
        inputMethods.put("s", new StudentInput());
        inputMethods.put("p", new ProfessorInput());
        inputMethods.put("m", new SimpleMemberInput());
    }

    public Member addMember(String name, String email, String type) {
        MemberInput input = inputMethods.get(type.toLowerCase());
        if (input == null) {
            throw new IllegalArgumentException("Tip membru necunoscut: " + type);
        }
        return input.addMember(name, email);
    }

    @Override
    public Member addMember(String name, String email) {
        return addMember(name, email, "simple");
    }

    public String getAvailableTypes() {
        return "student (s), professor (p), simple (m)";
    }
}