package com.example.tmppp_library_management.menus;

import com.example.tmppp_library_management.composite.EventComponent;
import com.example.tmppp_library_management.composite.EventGroup;
import com.example.tmppp_library_management.composite.EventService;
import com.example.tmppp_library_management.composite.SingleEvent;
import com.example.tmppp_library_management.services.LibrarianService;

import java.util.List;

public class EventMenu {
    private final EventService eventService;
    private final LibrarianService librarianService;

    public EventMenu(LibrarianService librarianService) {
        this.librarianService = librarianService;
        this.eventService = EventService.getInstance();
    }

    public void display(String token) {
        while (true) {
            System.out.println("\n--- EVENIMENTE ---");
            System.out.println("1. Lista toate evenimentele");
            System.out.println("2. Adauga eveniment");
            System.out.println("3. Adauga eveniment in grup");
            System.out.println("4. Detalii eveniment");
            System.out.println("5. Statistici evenimente");
            System.out.println("0. Inapoi");

            int choice = MenuUtils.readInt("Alege optiunea: ");

            switch (choice) {
                case 1 -> eventService.executeAll();
                case 2 -> addEventSubMenu(token);
                case 3 -> addEventToGroup(token);
                case 4 -> showEventDetails(token);
                case 5 -> showStatistics();
                case 0 -> { return; }
                default -> System.out.println("Optiune invalida!");
            }
        }
    }

    private void addEventSubMenu(String token) {
        System.out.println("\n--- ADAUGARE EVENIMENT ---");
        System.out.println("1. Eveniment simplu");
        System.out.println("2. Grup de evenimente");
        System.out.println("0. Inapoi");

        int choice = MenuUtils.readInt("Alege: ");

        switch (choice) {
            case 1 -> addSingleEvent(token);
            case 2 -> addEventGroup(token);
            case 0 -> { return; }
            default -> System.out.println("Optiune invalida!");
        }
    }

    private void addSingleEvent(String token) {
        System.out.println("\n--- ADAUGARE EVENIMENT SIMPLU ---");

        System.out.println("(Poti scrie /show events pentru a vedea evenimentele existente)");
        String name = MenuUtils.readStringWithCommands("Nume eveniment: ", null, null, null, this::listAllEventsSimple);
        String date = MenuUtils.readStringWithCommands("Data (ex: 15.05.2024): ", null, null, null, this::listAllEventsSimple);
        String location = MenuUtils.readStringWithCommands("Locatie: ", null, null, null, this::listAllEventsSimple);

        System.out.println("Tip eveniment:");
        System.out.println("1. Lansare de carte");
        System.out.println("2. Club de lectura");
        System.out.println("3. Atelier");
        System.out.println("4. Expozitie");
        System.out.println("5. Altul");

        int tip = MenuUtils.readInt("Alege tip (1-5): ");
        String type = switch (tip) {
            case 1 -> "lansare";
            case 2 -> "club";
            case 3 -> "atelier";
            case 4 -> "expozitie";
            default -> "general";
        };

        SingleEvent event = eventService.createEvent(name, date, location, type);

        // Logging prin LibrarianService
        if (event != null && token != null) {
            librarianService.addToLog(token, "A creat evenimentul: " + name);
        }

        System.out.println("✓ Eveniment adaugat cu ID: " + event.getId());
    }

    private void addEventGroup(String token) {
        System.out.println("\n--- ADAUGARE GRUP DE EVENIMENTE ---");

        System.out.println("(Poti scrie /show events pentru a vedea grupurile existente)");
        String name = MenuUtils.readStringWithCommands("Nume grup: ", null, null, null, this::listAllEventsSimple);

        EventGroup group = eventService.createGroup(name);

        // Logging prin LibrarianService
        if (group != null && token != null) {
            librarianService.addToLog(token, "A creat grupul de evenimente: " + name);
        }

        System.out.println("✓ Grup adaugat cu ID: " + group.getId());
    }

    private void addEventToGroup(String token) {
        System.out.println("\n--- ADAUGARE EVENIMENT IN GRUP ---");

        List<EventGroup> groups = eventService.getAllGroups();
        if (groups.isEmpty()) {
            System.out.println("Nu exista grupuri. Creati mai intai un grup.");
            return;
        }

        System.out.println("\nGrupuri disponibile:");
        for (EventGroup g : groups) {
            System.out.println("  ID " + g.getId() + ": " + g.getName() +
                    " (" + g.getEvents().size() + " evenimente)");
        }

        System.out.println("(Poti scrie /show events pentru a vedea toate evenimentele)");
        int groupId = MenuUtils.readIntWithCommands("ID grup: ", null, null, null, this::listAllEventsSimple);
        EventGroup group = eventService.findGroupById(groupId);

        if (group == null) {
            System.out.println("Grup negasit!");
            return;
        }

        List<SingleEvent> allEvents = eventService.getAllSingleEvents();
        List<EventComponent> existingEvents = group.getEvents();

        List<SingleEvent> availableEvents = allEvents.stream()
                .filter(e -> !existingEvents.contains(e))
                .toList();

        if (availableEvents.isEmpty()) {
            System.out.println("Nu exista evenimente disponibile de adaugat.");
            return;
        }

        System.out.println("\nEvenimente disponibile:");
        for (SingleEvent e : availableEvents) {
            System.out.println("  ID " + e.getId() + ": " + e.getName() +
                    " (" + e.getDate() + ", " + e.getLocation() + ")");
        }

        System.out.println("(Poti scrie /show events pentru a vedea toate evenimentele)");
        int eventId = MenuUtils.readIntWithCommands("ID eveniment: ", null, null, null, this::listAllEventsSimple);
        SingleEvent event = eventService.findEventById(eventId);

        if (event == null) {
            System.out.println("Eveniment negasit!");
            return;
        }

        group.add(event);

        // Logging prin LibrarianService
        if (token != null) {
            librarianService.addToLog(token, "A adaugat evenimentul " + event.getName() +
                    " in grupul " + group.getName());
        }

        System.out.println("✓ Eveniment adaugat in grup!");
    }

    private void showEventDetails(String token) {
        System.out.println("(Poti scrie /show events pentru a vedea toate evenimentele)");
        int id = MenuUtils.readIntWithCommands("ID eveniment/grup: ", null, null, null, this::listAllEventsSimple);
        EventComponent event = eventService.findComponentById(id);

        if (event == null) {
            System.out.println("Element negasit!");
            return;
        }

        // Logging prin LibrarianService
        if (token != null) {
            librarianService.addToLog(token, "A vizualizat detaliile pentru: " + event.getName());
        }

        System.out.println("\n--- DETALII ---");
        System.out.println("ID: " + event.getId());
        System.out.println("Nume: " + event.getName());

        if (event instanceof SingleEvent) {
            SingleEvent e = (SingleEvent) event;
            System.out.println("Tip: " + e.getType());
            System.out.println("Data: " + e.getDate());
            System.out.println("Locatie: " + e.getLocation());
        } else if (event instanceof EventGroup) {
            EventGroup g = (EventGroup) event;
            System.out.println("Tip: GRUP");
            System.out.println("Numar evenimente: " + g.getEvents().size());

            if (!g.getEvents().isEmpty()) {
                System.out.println("Data: " + g.getDate());
                System.out.println("Locatie: " + g.getLocation());

                System.out.println("\nEvenimente in grup:");
                for (EventComponent e : g.getEvents()) {
                    if (e instanceof SingleEvent) {
                        System.out.println("  - " + e.getName() + " (" + e.getDate() + ")");
                    } else {
                        System.out.println("  - [GRUP] " + e.getName());
                    }
                }
            }
        }
    }

    private void showStatistics() {
        List<SingleEvent> allEvents = eventService.getAllSingleEvents();
        List<EventGroup> allGroups = eventService.getAllGroups();

        int eventsInGroups = 0;
        for (EventGroup g : allGroups) {
            eventsInGroups += g.getEvents().size();
        }

        System.out.println("\n--- STATISTICI EVENIMENTE ---");
        System.out.println("Total evenimente individuale: " + allEvents.size());
        System.out.println("Total grupuri: " + allGroups.size());
        System.out.println("Evenimente in grupuri: " + eventsInGroups);
    }

    private void listAllEventsSimple() {
        eventService.executeAll();
    }
}