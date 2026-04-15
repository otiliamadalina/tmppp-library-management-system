package com.example.tmppp_library_management.menus;

import com.example.tmppp_library_management.newspaper.LocalNewspaper;
import com.example.tmppp_library_management.newspaper.NationalNewspaper;
import com.example.tmppp_library_management.newspaper.Newspaper;
import com.example.tmppp_library_management.services.LibrarianService;
import com.example.tmppp_library_management.services.NewspaperService;

import java.util.List;

public class NewspaperMenu {
    private final NewspaperService newspaperService;
    private final LibrarianService librarianService;
    private int nextItemId = 200;

    public NewspaperMenu(NewspaperService newspaperService, LibrarianService librarianService) {
        this.newspaperService = newspaperService;
        this.librarianService = librarianService;
    }

    public void display(String token) {
        while (true) {
            System.out.println("\n--- ZIARE ---");
            System.out.println("1. Listeaza ziarele");
            System.out.println("2. Adauga ziar local");
            System.out.println("3. Adauga ziar national");
            System.out.println("4. Cauta dupa oras");
            System.out.println("5. Cauta dupa regiune");
            System.out.println("6. Cauta dupa orientare politica");
            System.out.println("0. Inapoi");

            int choice = MenuUtils.readInt("Alege: ");
            switch (choice) {
                case 1 -> listAllNewspapers();
                case 2 -> addLocalNewspaper(token);
                case 3 -> addNationalNewspaper(token);
                case 4 -> searchByCity();
                case 5 -> searchByRegion();
                case 6 -> searchByPoliticalOrientation();
                case 0 -> { return; }
                default -> System.out.println("Optiune invalida");
            }
        }
    }

    private void listAllNewspapers() {
        List<Newspaper> newspapers = newspaperService.getAllItems().stream()
                .filter(item -> item instanceof Newspaper)
                .map(item -> (Newspaper) item)
                .toList();

        if (newspapers.isEmpty()) {
            System.out.println("Nu exista ziare");
            return;
        }

        System.out.println("\nZIARE:");
        System.out.println("ID  | Tip          | Titlu                   | Publicatie        | ISSN");
        System.out.println("----+--------------+-------------------------+-------------------+------------");

        for (Newspaper n : newspapers) {
            String tip = (n instanceof LocalNewspaper) ? "Local" : "National";
            System.out.printf("%-3d | %-12s | %-23s | %-17s | %s\n",
                    n.getItemId(),
                    tip,
                    MenuUtils.truncate(n.getTitle(), 23),
                    MenuUtils.truncate(n.getPublisher().getName(), 17),
                    n.getIssn());

            if (n instanceof LocalNewspaper local) {
                System.out.println("     Oras: " + local.getCity() + ", Regiune: " + local.getRegion());
            } else if (n instanceof NationalNewspaper national) {
                System.out.println("     Distributie: " + national.getDistributionArea() +
                        ", Orientare: " + national.getPoliticalOrientation());
            }
        }
    }

    private void addLocalNewspaper(String token) {
        System.out.println("\n--- Adauga Ziar Local ---");
        System.out.println("(/show newspapers pentru a vedea ziarele existente)");

        // Folosim Chain of Responsibility pentru validare titlu
        String title = MenuUtils.readValidatedTitle("Titlu: ");
        if (title == null) {
            System.out.println("Operatiune anulata.");
            return;
        }

        // Folosim Chain of Responsibility pentru validare an
        String yearStr = MenuUtils.readValidatedYear("An: ");
        if (yearStr == null) {
            System.out.println("Operatiune anulata.");
            return;
        }
        int year = Integer.parseInt(yearStr);

        int pages = MenuUtils.readInt("Pagini: ");
        String publisher = MenuUtils.readString("Editura: ");

        // Folosim Chain of Responsibility pentru validare ISSN (cu verificare duplicat)
        String issn = MenuUtils.readValidatedIssn("ISSN: ");
        if (issn == null) {
            System.out.println("Operatiune anulata.");
            return;
        }

        String city = MenuUtils.readString("Oras: ");
        String region = MenuUtils.readString("Regiune: ");

        LocalNewspaper newspaper = librarianService.addLocalNewspaper(
                token, nextItemId++, title, year, pages, publisher, issn, city, region);
        System.out.println("✓ Ziar local adaugat cu ID: " + newspaper.getItemId());
    }

    private void addNationalNewspaper(String token) {
        System.out.println("\n--- Adauga Ziar National ---");
        System.out.println("(/show newspapers pentru a vedea ziarele existente)");

        // Folosim Chain of Responsibility pentru validare titlu
        String title = MenuUtils.readValidatedTitle("Titlu: ");
        if (title == null) {
            System.out.println("Operatiune anulata.");
            return;
        }

        // Folosim Chain of Responsibility pentru validare an
        String yearStr = MenuUtils.readValidatedYear("An: ");
        if (yearStr == null) {
            System.out.println("Operatiune anulata.");
            return;
        }
        int year = Integer.parseInt(yearStr);

        int pages = MenuUtils.readInt("Pagini: ");
        String publisher = MenuUtils.readString("Editura: ");

        // Folosim Chain of Responsibility pentru validare ISSN (cu verificare duplicat)
        String issn = MenuUtils.readValidatedIssn("ISSN: ");
        if (issn == null) {
            System.out.println("Operatiune anulata.");
            return;
        }

        String area = MenuUtils.readString("Arie distributie: ");
        String orientation = MenuUtils.readString("Orientare politica: ");

        NationalNewspaper newspaper = librarianService.addNationalNewspaper(
                token, nextItemId++, title, year, pages, publisher, issn, area, orientation);
        System.out.println("✓ Ziar national adaugat cu ID: " + newspaper.getItemId());
    }

    private void searchByCity() {
        System.out.println("(Poti scrie /show newspapers pentru a vedea toate ziarele)");
        String city = MenuUtils.readStringWithCommands("Oras: ", this::listAllNewspapers, null, null, null);
        List<LocalNewspaper> results = newspaperService.findByCity(city);

        if (results.isEmpty()) {
            System.out.println("Nu s-au gasit ziare in " + city);
            return;
        }

        System.out.println("\nZiare in " + city + ":");
        for (LocalNewspaper n : results) {
            System.out.printf("  - %s (%s, %s)\n",
                    n.getTitle(), n.getPublisher().getName(), n.getRegion());
        }
    }

    private void searchByRegion() {
        System.out.println("(/show newspapers pentru a vedea toate ziarele)");
        String region = MenuUtils.readStringWithCommands("Regiune: ", this::listAllNewspapers, null, null, null);
        List<Newspaper> results = newspaperService.findByRegion(region);

        if (results.isEmpty()) {
            System.out.println("Nu s-au gasit ziare in regiunea " + region);
            return;
        }

        System.out.println("\nZiare in regiunea " + region + ":");
        for (Newspaper n : results) {
            if (n instanceof LocalNewspaper local) {
                System.out.printf("  - %s (%s, %s)\n",
                        n.getTitle(), n.getPublisher().getName(), local.getCity());
            } else {
                System.out.printf("  - %s (%s)\n", n.getTitle(), n.getPublisher().getName());
            }
        }
    }

    private void searchByPoliticalOrientation() {
        System.out.println("(/show newspapers pentru a vedea toate ziarele)");
        String orientation = MenuUtils.readStringWithCommands("Orientare politica: ", this::listAllNewspapers, null, null, null);
        List<NationalNewspaper> results = new java.util.ArrayList<>();

        for (var item : newspaperService.getAllItems()) {
            if (item instanceof NationalNewspaper national &&
                    national.getPoliticalOrientation().toLowerCase().contains(orientation.toLowerCase())) {
                results.add(national);
            }
        }

        if (results.isEmpty()) {
            System.out.println("Nu s-au gasit ziare cu orientarea \"" + orientation + "\"");
            return;
        }

        System.out.println("\nZiare cu orientarea " + orientation + ":");
        for (NationalNewspaper n : results) {
            System.out.printf("  - %s (%s, distributie: %s)\n",
                    n.getTitle(), n.getPublisher().getName(), n.getDistributionArea());
        }
    }
}