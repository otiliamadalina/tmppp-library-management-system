package com.example.tmppp_library_management.adapter;

import com.example.tmppp_library_management.user.Member;
import com.example.tmppp_library_management.user.MemberType;
import com.example.tmppp_library_management.services.MemberService;

public class StudentInput implements MemberInput {
    private MemberService memberService;

    public StudentInput() {
        this.memberService = MemberService.getInstance();
    }

    @Override
    public Member addMember(String name, String email) {
        return memberService.addMember(name, email, MemberType.STUDENT);
    }
}