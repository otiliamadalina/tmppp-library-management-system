package com.example.tmppp_library_management.chainOfResponsability;

public abstract class BaseInputValidator implements InputValidator {
    protected InputValidator next;

    @Override
    public InputValidator setNext(InputValidator next) {
        this.next = next;
        return next;
    }

    @Override
    public ValidationResult validate(String input, String fieldName, Object... context) {
        ValidationResult result = performValidation(input, fieldName, context);

        if (!result.isValid()) {
            return result;
        }

        if (next != null) {
            return next.validate(input, fieldName, context);
        }

        return ValidationResult.success();
    }

    protected abstract ValidationResult performValidation(String input, String fieldName, Object... context);
}