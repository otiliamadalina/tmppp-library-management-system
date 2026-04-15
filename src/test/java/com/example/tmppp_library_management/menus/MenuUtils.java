package com.example.tmppp_library_management.menus;

import com.example.tmppp_library_management.chainOfResponsability.*;
import com.example.tmppp_library_management.services.BookService;
import com.example.tmppp_library_management.services.MemberService;
import com.example.tmppp_library_management.services.NewspaperService;

import java.util.Scanner;

public class MenuUtils {
    private static final Scanner scanner = new Scanner(System.in);
    private static ValidationChainFactory validationFactory;
    private static boolean validationInitialized = false;

    public static void initValidation(BookService bookService, MemberService memberService, NewspaperService newspaperService) {
        if (!validationInitialized) {
            validationFactory = new ValidationChainFactory(bookService, memberService, newspaperService);
            validationInitialized = true;
        }
    }

    public static String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public static int readInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();

                if (input.startsWith("/")) {
                    handleMenuCommand(input);
                    continue;
                }

                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Introdu un numar valid sau o comanda (ex: /show)");
            }
        }
    }

    public static int readIntInMemberContext(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();

                if (input.startsWith("/")) {
                    handleMemberCommand(input);
                    continue;
                }

                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Introdu un numar valid sau o comanda (ex: /show)");
            }
        }
    }

    public static int readIntWithCommands(String prompt, Runnable showBooks, Runnable showMembers, Runnable showEvents, Runnable showLoans) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();

                if (input.startsWith("/")) {
                    handleContextualCommand(input, showBooks, showMembers, showEvents, showLoans);
                    continue;
                }

                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Introdu un numar valid sau o comanda (ex: /show)");
            }
        }
    }

    public static String readStringWithCommands(String prompt, Runnable showBooks, Runnable showMembers, Runnable showEvents, Runnable showLoans) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.startsWith("/")) {
                handleContextualCommand(input, showBooks, showMembers, showEvents, showLoans);
                continue;
            }

            return input;
        }
    }

    // ============ METODE DE VALIDARE CHAIN OF RESPONSIBILITY ============

    public static String readValidatedString(String prompt, String fieldName, InputValidator chain, Object context) {
        while (true) {
            String input = readString(prompt);
            ValidationResult result = chain.validate(input, fieldName, context);

            if (result.isValid()) {
                return input;
            }

            System.out.println("✗ " + result.getErrorMessage());
            System.out.print("Doriti sa incercati din nou? (da/nu): ");
            String retry = scanner.nextLine().trim();
            if (!retry.equalsIgnoreCase("da")) {
                return null;
            }
        }
    }

    public static String readValidatedIsbn(String prompt) {
        if (!validationInitialized) {
            throw new IllegalStateException("Validation not initialized. Call initValidation() first.");
        }
        InputValidator chain = validationFactory.buildIsbnChain();
        return readValidatedString(prompt, "ISBN", chain, FormatInputValidator.FormatType.ISBN);
    }

    public static String readValidatedEmail(String prompt) {
        if (!validationInitialized) {
            throw new IllegalStateException("Validation not initialized. Call initValidation() first.");
        }
        InputValidator chain = validationFactory.buildEmailChain();
        return readValidatedString(prompt, "Email", chain, FormatInputValidator.FormatType.EMAIL);
    }

    public static String readValidatedName(String prompt) {
        if (!validationInitialized) {
            throw new IllegalStateException("Validation not initialized. Call initValidation() first.");
        }
        InputValidator chain = validationFactory.buildNameChain();
        return readValidatedString(prompt, "Nume", chain, FormatInputValidator.FormatType.NAME);
    }

    public static String readValidatedIssn(String prompt) {
        if (!validationInitialized) {
            throw new IllegalStateException("Validation not initialized. Call initValidation() first.");
        }
        InputValidator chain = validationFactory.buildIssnChain();
        return readValidatedString(prompt, "ISSN", chain, FormatInputValidator.FormatType.ISSN);
    }

    public static String readValidatedTitle(String prompt) {
        if (!validationInitialized) {
            throw new IllegalStateException("Validation not initialized. Call initValidation() first.");
        }
        InputValidator chain = validationFactory.buildTitleChain();
        return readValidatedString(prompt, "Titlu", chain, FormatInputValidator.FormatType.TITLE);
    }

    public static String readValidatedYear(String prompt) {
        if (!validationInitialized) {
            throw new IllegalStateException("Validation not initialized. Call initValidation() first.");
        }
        InputValidator chain = validationFactory.buildYearChain();
        return readValidatedString(prompt, "An", chain, FormatInputValidator.FormatType.YEAR);
    }

    // ============ METODE EXISTENTE ============

    private static void handleMenuCommand(String command) {
        switch (command.toLowerCase()) {
            case "/show":
            case "/show all":
                System.out.println("Comenzi disponibile:");
                System.out.println("  /show books - afiseaza toate cartile");
                System.out.println("  /show members - afiseaza toti membrii");
                System.out.println("  /show loans - afiseaza imprumuturile active");
                System.out.println("  /show events - afiseaza evenimentele");
                System.out.println("  /show help - afiseaza aceasta lista");
                break;

            default:
                System.out.println("Comanda necunoscuta. Scrie /show pentru ajutor.");
        }
    }

    private static void handleMemberCommand(String command) {
        switch (command.toLowerCase()) {
            case "/show":
            case "/show all":
                System.out.println("Comenzi disponibile:");
                System.out.println("  /show books - afiseaza toate cartile");
                System.out.println("  /show members - afiseaza toti membrii");
                System.out.println("  /show loans - afiseaza imprumuturile active");
                System.out.println("  /show events - afiseaza evenimentele");
                break;

            case "/show members":
                System.out.println("Aceasta comanda functioneaza doar in interiorul optiunilor de cautare, nu in meniul principal.");
                System.out.println("Mergeti la '3. Cauta membru' pentru a folosi /show members.");
                break;

            default:
                System.out.println("Comanda necunoscuta. Scrie /show pentru ajutor.");
        }
    }

    private static void handleContextualCommand(String command, Runnable showBooks, Runnable showMembers, Runnable showEvents, Runnable showLoans) {
        switch (command.toLowerCase()) {
            case "/show books":
                if (showBooks != null) showBooks.run();
                else System.out.println("Nu pot afisa cartile aici.");
                break;

            case "/show members":
                if (showMembers != null) showMembers.run();
                else System.out.println("Nu pot afisa membrii aici.");
                break;

            case "/show events":
                if (showEvents != null) showEvents.run();
                else System.out.println("Nu pot afisa evenimentele aici.");
                break;

            case "/show loans":
                if (showLoans != null) showLoans.run();
                else System.out.println("Nu pot afisa imprumuturi aici");
                break;

            default:
                handleMenuCommand(command);
        }
    }

    public static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    public static double readDouble(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Introdu un numar valid (ex: 1.5)");
            }
        }
    }
}