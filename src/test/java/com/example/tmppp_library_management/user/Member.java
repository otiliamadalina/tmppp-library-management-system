package com.example.tmppp_library_management.user;

public class Member extends User {
    private MemberType memberType;
    private int currentLoans;
    private String membershipNumber;

    public Member(int userId, String userName, String userEmail,
                  MemberType memberType, String membershipNumber) {
        super(userId, userName, userEmail);
        this.memberType = memberType;
        this.membershipNumber = membershipNumber;
        this.currentLoans = 0;
    }

    public MemberType getMemberType() { return memberType; }
    public int getCurrentLoans() { return currentLoans; }
    public String getMembershipNumber() { return membershipNumber; }
    public int getMaxBooks() { return memberType.getMaxBooks(); }

    public void setMemberType(MemberType memberType) {
        this.memberType = memberType;
    }

    public void setCurrentLoans(int currentLoans) {
        this.currentLoans = currentLoans;
    }

    public boolean canBorrow() {
        return currentLoans < getMaxBooks();
    }

    @Override
    public String toString() {
        return String.format("Member[ID=%d, Name=%s, Type=%s, Membership=%s, Loans=%d/%d]",
                getUserId(), getUserName(), memberType, membershipNumber, currentLoans, getMaxBooks());
    }
}