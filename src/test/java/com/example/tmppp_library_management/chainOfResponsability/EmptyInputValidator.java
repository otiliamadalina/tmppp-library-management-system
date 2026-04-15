package com.example.tmppp_library_management.chainOfResponsability;

public class EmptyInputValidator extends BaseInputValidator {
    @Override
    protected ValidationResult performValidation(String input, String fieldName, Object... context) {
        if (input == null || input.trim().isEmpty()) {
            return ValidationResult.empty();
        }
        return ValidationResult.success();
    }
}