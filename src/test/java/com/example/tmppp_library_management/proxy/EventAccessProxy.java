package com.example.tmppp_library_management.proxy;

import com.example.tmppp_library_management.composite.EventComponent;
import com.example.tmppp_library_management.composite.SingleEvent;
import com.example.tmppp_library_management.user.Member;
import com.example.tmppp_library_management.user.MemberType;
import java.util.ArrayList;
import java.util.List;

public class EventAccessProxy implements EventComponent {
    private SingleEvent realEvent;
    private MemberType requiredMemberType; // STUDENT, PROFESSOR, SIMPLE, sau null pentru oricine
    private int maxParticipants;
    private List<Member> registeredMembers;
    private boolean isInitialized;

    private String pendingName;
    private String pendingDate;
    private String pendingLocation;
    private String pendingType;

    public EventAccessProxy(String name, String date, String location, String type,
                            MemberType requiredMemberType, int maxParticipants) {
        this.realEvent = null;
        this.requiredMemberType = requiredMemberType;
        this.maxParticipants = maxParticipants;
        this.registeredMembers = new ArrayList<>();
        this.isInitialized = false;

        this.pendingName = name;
        this.pendingDate = date;
        this.pendingLocation = location;
        this.pendingType = type;
    }

    private void ensureInitialized() {
        if (!isInitialized) {
            this.realEvent = new SingleEvent(
                    (int)(System.currentTimeMillis() % 10000),
                    pendingName, pendingDate, pendingLocation, pendingType
            );
            this.isInitialized = true;
        }
    }

    @Override
    public void execute() {
        ensureInitialized();
        realEvent.execute();
    }

    public boolean register(Member member) {
        ensureInitialized();

        // (daca requiredMemberType e null = fara restrictii)
        if (requiredMemberType != null && member.getMemberType() != requiredMemberType) {
            System.out.println("Acces restrictionat: " + member.getMemberType() +
                    " nu poate participa. Necesar: " + requiredMemberType);
            return false;
        }

        if (registeredMembers.size() >= maxParticipants) {
            System.out.println("Eveniment complet: nu mai sunt locuri disponibile");
            return false;
        }

        if (registeredMembers.contains(member)) {
            System.out.println("Membrul este deja inregistrat");
            return false;
        }

        registeredMembers.add(member);
        System.out.println("Inregistrare reusita: " + member.getUserName() +
                " la evenimentul " + realEvent.getName());
        return true;
    }

    public boolean canAccess(Member member) {
        if (requiredMemberType == null) {
            return true;
        }
        return member.getMemberType() == requiredMemberType;
    }

    public int getAvailableSpots() {
        return maxParticipants - registeredMembers.size();
    }

    public int getRegisteredCount() {
        return registeredMembers.size();
    }

    public String getRestrictionInfo() {
        String restriction = (requiredMemberType == null) ? "Fara restrictii" : "Doar: " + requiredMemberType;
        return restriction + " | Locuri: " + registeredMembers.size() + "/" + maxParticipants;
    }

    @Override
    public int getId() {
        ensureInitialized();
        return realEvent.getId();
    }

    @Override
    public String getName() {
        ensureInitialized();
        return realEvent.getName();
    }

    @Override
    public String getDate() {
        ensureInitialized();
        return realEvent.getDate();
    }

    @Override
    public String getLocation() {
        ensureInitialized();
        return realEvent.getLocation();
    }
}