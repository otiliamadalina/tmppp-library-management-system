package com.example.tmppp_library_management.chainOfResponsability;

public class ValidationResult {
    private final boolean valid;
    private final String errorMessage;
    private final ErrorType errorType;

    public enum ErrorType {
        EMPTY,
        FORMAT,
        DUPLICATE,
        NONE
    }

    private ValidationResult(boolean valid, String errorMessage, ErrorType errorType) {
        this.valid = valid;
        this.errorMessage = errorMessage;
        this.errorType = errorType;
    }

    public static ValidationResult success() {
        return new ValidationResult(true, null, ErrorType.NONE);
    }

    public static ValidationResult empty() {
        return new ValidationResult(false, "Campul nu poate fi gol!", ErrorType.EMPTY);
    }

    public static ValidationResult format(String fieldName, String expectedFormat) {
        return new ValidationResult(false,
                String.format("%s are format invalid. Format asteptat: %s", fieldName, expectedFormat),
                ErrorType.FORMAT);
    }

    public static ValidationResult duplicate(String fieldName, String value) {
        return new ValidationResult(false,
                String.format("%s '%s' exista deja in sistem!", fieldName, value),
                ErrorType.DUPLICATE);
    }

    public boolean isValid() { return valid; }
    public String getErrorMessage() { return errorMessage; }
    public ErrorType getErrorType() { return errorType; }
}