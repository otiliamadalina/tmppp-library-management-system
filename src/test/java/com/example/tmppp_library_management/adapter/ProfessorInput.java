package com.example.tmppp_library_management.adapter;

import com.example.tmppp_library_management.user.Member;
import com.example.tmppp_library_management.user.MemberType;
import com.example.tmppp_library_management.services.MemberService;

public class ProfessorInput implements MemberInput {
    private MemberService memberService;

    public ProfessorInput() {
        this.memberService = MemberService.getInstance();
    }

    @Override
    public Member addMember(String name, String email) {
        return memberService.addMember(name, email, MemberType.PROFESSOR);
    }
}