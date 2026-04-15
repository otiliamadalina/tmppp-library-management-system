package com.example.tmppp_library_management.composite;

import com.example.tmppp_library_management.proxy.EventAccessProxy;
import com.example.tmppp_library_management.user.MemberType;

import java.util.ArrayList;
import java.util.List;

public class EventService {
    private static EventService instance;
    private List<EventComponent> allEvents;
    private int nextEventId = 1;
    private int nextGroupId = 1;

    private EventService() {
        this.allEvents = new ArrayList<>();
    }

    public static EventService getInstance() {
        if (instance == null) {
            instance = new EventService();
        }
        return instance;
    }

    // METODA NOUA - creeaza eveniment cu locuri nelimitate
    public SingleEvent createEvent(String name, String date, String location, String type) {
        SingleEvent event = new SingleEvent(nextEventId++, name, date, location, type);
        allEvents.add(event);
        return event;
    }

    // Metoda existenta - creeaza eveniment cu capacitate limitata
    public SingleEvent createEventWithCapacity(String name, String date, String location, String type, int maxParticipants) {
        SingleEvent event = new SingleEvent(nextEventId++, name, date, location, type, maxParticipants);
        allEvents.add(event);
        return event;
    }

    public EventGroup createGroup(String name) {
        EventGroup group = new EventGroup(nextGroupId++, name);
        allEvents.add(group);
        return group;
    }

    public void executeAll() {
        System.out.println("\n=== TOATE EVENIMENTELE ===");
        for (EventComponent event : allEvents) {
            event.execute();
        }
    }

    public List<SingleEvent> getAllSingleEvents() {
        List<SingleEvent> result = new ArrayList<>();
        for (EventComponent comp : allEvents) {
            if (comp instanceof SingleEvent) {
                result.add((SingleEvent) comp);
            }
        }
        return result;
    }

    public List<EventGroup> getAllGroups() {
        List<EventGroup> result = new ArrayList<>();
        for (EventComponent comp : allEvents) {
            if (comp instanceof EventGroup) {
                result.add((EventGroup) comp);
            }
        }
        return result;
    }

    public EventGroup findGroupById(int id) {
        for (EventComponent comp : allEvents) {
            if (comp instanceof EventGroup && comp.getId() == id) {
                return (EventGroup) comp;
            }
        }
        return null;
    }

    public SingleEvent findEventById(int id) {
        for (EventComponent comp : allEvents) {
            if (comp instanceof SingleEvent && comp.getId() == id) {
                return (SingleEvent) comp;
            }
        }
        return null;
    }

    public EventComponent findComponentById(int id) {
        for (EventComponent comp : allEvents) {
            if (comp.getId() == id) {
                return comp;
            }
            if (comp instanceof EventGroup) {
                EventGroup group = (EventGroup) comp;
                for (EventComponent child : group.getEvents()) {
                    if (child.getId() == id) {
                        return child;
                    }
                }
            }
        }
        return null;
    }

    private List<EventAccessProxy> proxiedEvents = new ArrayList<>();

    public EventAccessProxy createRestrictedEvent(String name, String date, String location,
                                                  String type, MemberType requiredType,
                                                  int maxParticipants) {
        EventAccessProxy proxy = new EventAccessProxy(name, date, location, type,
                requiredType, maxParticipants);
        proxiedEvents.add(proxy);
        allEvents.add(proxy);
        return proxy;
    }

    public List<EventAccessProxy> getAllProxiedEvents() {
        return new ArrayList<>(proxiedEvents);
    }
}