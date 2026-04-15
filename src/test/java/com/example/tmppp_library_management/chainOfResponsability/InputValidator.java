package com.example.tmppp_library_management.chainOfResponsability;

public interface InputValidator {
    InputValidator setNext(InputValidator next);
    ValidationResult validate(String input, String fieldName, Object... context);
}