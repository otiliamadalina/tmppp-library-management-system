package com.example.tmppp_library_management.chainOfResponsability;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class FormatInputValidator extends BaseInputValidator {

    public enum FormatType {
        EMAIL, ISBN, ISSN, PHONE, NAME, TITLE, MEMBERSHIP, YEAR, PRICE, ANY
    }

    private final Map<FormatType, Predicate<String>> validators = new HashMap<>();
    private final Map<FormatType, String> errorMessages = new HashMap<>();

    public FormatInputValidator() {
        initializeValidators();
    }

    private void initializeValidators() {
        // EMAIL
        validators.put(FormatType.EMAIL,
                email -> email.matches("^[A-Za-z0-9+_.-]+@(.+)$"));
        errorMessages.put(FormatType.EMAIL, "exemplu: nume@domeniu.ro");

        // ISBN
        validators.put(FormatType.ISBN,
                isbn -> {
                    String digits = isbn.replaceAll("-", "");
                    return digits.length() == 12;
                });
        errorMessages.put(FormatType.ISBN, "12 cifre (ex: 111-111-111-111)");

        // ISSN
        validators.put(FormatType.ISSN,
                issn -> issn.matches("^\\d{4}-\\d{3}[\\dX]$"));
        errorMessages.put(FormatType.ISSN, "format: 1234-5678");

        // PHONE
        validators.put(FormatType.PHONE,
                phone -> phone.matches("^07[0-9]{8}$|^\\+40[0-9]{9}$"));
        errorMessages.put(FormatType.PHONE, "07xxxxxxxx sau +40xxxxxxxxx");

        // NAME
        validators.put(FormatType.NAME,
                name -> name.length() >= 2 && name.matches("^[a-zA-ZăâîșțĂÂÎȘȚ\\s-]+$"));
        errorMessages.put(FormatType.NAME, "minim 2 caractere, doar litere");

        // TITLE
        validators.put(FormatType.TITLE,
                title -> title.length() >= 3 && title.length() <= 200);
        errorMessages.put(FormatType.TITLE, "minim 3, maxim 200 caractere");

        // MEMBERSHIP
        validators.put(FormatType.MEMBERSHIP,
                membership -> membership.matches("^MEM-\\d{5}$"));
        errorMessages.put(FormatType.MEMBERSHIP, "format: MEM-xxxxx (ex: MEM-00123)");

        // YEAR
        validators.put(FormatType.YEAR,
                year -> year.matches("^\\d{4}$") && Integer.parseInt(year) >= 1000 && Integer.parseInt(year) <= 2026);
        errorMessages.put(FormatType.YEAR, "an intre 1000 si 2026");

        // PRICE
        validators.put(FormatType.PRICE,
                price -> price.matches("^\\d+(\\.\\d{1,2})?$") && Double.parseDouble(price) > 0);
        errorMessages.put(FormatType.PRICE, "numar pozitiv (ex: 29.99)");

        // ANY
        validators.put(FormatType.ANY, any -> true);
        errorMessages.put(FormatType.ANY, "");
    }

    @Override
    protected ValidationResult performValidation(String input, String fieldName, Object... context) {
        if (context.length == 0 || !(context[0] instanceof FormatType)) {
            return ValidationResult.success();
        }

        FormatType formatType = (FormatType) context[0];
        Predicate<String> validator = validators.get(formatType);

        if (validator != null && !validator.test(input)) {
            return ValidationResult.format(fieldName, errorMessages.get(formatType));
        }

        return ValidationResult.success();
    }
}