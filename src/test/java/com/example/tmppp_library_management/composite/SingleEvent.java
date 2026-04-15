package com.example.tmppp_library_management.composite;

public class SingleEvent implements EventComponent {
    private int id;
    private String name;
    private String date;
    private String location;
    private String type;
    private int maxParticipants;
    private int registeredParticipants;

    // Constructor pentru evenimente cu locuri nelimitate
    public SingleEvent(int id, String name, String date, String location, String type) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.location = location;
        this.type = type;
        this.maxParticipants = -1; // -1 = nelimitat
        this.registeredParticipants = 0;
    }

    // Constructor pentru evenimente cu locuri limitate
    public SingleEvent(int id, String name, String date, String location, String type, int maxParticipants) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.location = location;
        this.type = type;
        this.maxParticipants = maxParticipants;
        this.registeredParticipants = 0;
    }

    public boolean registerParticipant() {
        if (maxParticipants == -1 || registeredParticipants < maxParticipants) {
            registeredParticipants++;
            return true;
        }
        return false;
    }

    public boolean hasAvailableSpots() {
        return maxParticipants == -1 || registeredParticipants < maxParticipants;
    }

    public int getAvailableSpots() {
        if (maxParticipants == -1) return -1;
        return maxParticipants - registeredParticipants;
    }

    public int getMaxParticipants() { return maxParticipants; }
    public int getRegisteredParticipants() { return registeredParticipants; }

    @Override
    public void execute() {
        String spotsInfo = maxParticipants == -1 ? "Locuri: nelimitate" : "Locuri disponibile: " + getAvailableSpots() + "/" + maxParticipants;
        System.out.println("Eveniment: " + name + " | Data: " + date + " | Locatie: " + location +
                " | Tip: " + type + " | " + spotsInfo);
    }

    @Override
    public int getId() { return id; }

    @Override
    public String getName() { return name; }

    @Override
    public String getDate() { return date; }

    @Override
    public String getLocation() { return location; }

    public String getType() { return type; }
}