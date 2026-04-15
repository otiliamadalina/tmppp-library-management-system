package com.example.tmppp_library_management.composite;

import java.util.ArrayList;
import java.util.List;

public class EventGroup implements EventComponent {
    private int groupId;
    private String groupName;
    private List<EventComponent> events;

    public EventGroup(int groupId, String groupName) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.events = new ArrayList<>();
    }

    public void add(EventComponent event) {
        events.add(event);
    }

    public void remove(EventComponent event) {
        events.remove(event);
    }

    public List<EventComponent> getEvents() {
        return new ArrayList<>(events);
    }

    @Override
    public void execute() {
        System.out.println("\nGRUP: [" + groupId + "] " + groupName +
                " (" + events.size() + " evenimente)");

        for (EventComponent event : events) {
            event.execute();
        }
    }

    @Override
    public int getId() { return groupId; }

    @Override
    public String getName() { return groupName; }

    @Override
    public String getDate() {
        if (events.isEmpty()) return "N/A";
        return events.get(0).getDate();
    }

    @Override
    public String getLocation() {
        if (events.isEmpty()) return "N/A";
        String firstLoc = events.get(0).getLocation();
        for (EventComponent e : events) {
            if (!e.getLocation().equals(firstLoc)) {
                return "Locatii multiple";
            }
        }
        return firstLoc;
    }
}