package com.example.tmppp_library_management.chainOfResponsability;

import com.example.tmppp_library_management.services.BookService;
import com.example.tmppp_library_management.services.MemberService;
import com.example.tmppp_library_management.services.NewspaperService;

public class ValidationChainFactory {
    private final EmptyInputValidator emptyValidator;
    private final FormatInputValidator formatValidator;
    private final DuplicateValidator duplicateValidator;

    public ValidationChainFactory(BookService bookService, MemberService memberService, NewspaperService newspaperService) {
        this.emptyValidator = new EmptyInputValidator();
        this.formatValidator = new FormatInputValidator();
        this.duplicateValidator = new DuplicateValidator(bookService, memberService, newspaperService);
    }

    public InputValidator buildIsbnChain() {
        emptyValidator.setNext(formatValidator).setNext(duplicateValidator);
        return emptyValidator;
    }

    public InputValidator buildEmailChain() {
        emptyValidator.setNext(formatValidator).setNext(duplicateValidator);
        return emptyValidator;
    }

    public InputValidator buildNameChain() {
        emptyValidator.setNext(formatValidator);
        return emptyValidator;
    }

    public InputValidator buildIssnChain() {
        emptyValidator.setNext(formatValidator).setNext(duplicateValidator);
        return emptyValidator;
    }

    public InputValidator buildMembershipChain() {
        emptyValidator.setNext(formatValidator).setNext(duplicateValidator);
        return emptyValidator;
    }

    public InputValidator buildTitleChain() {
        emptyValidator.setNext(formatValidator);
        return emptyValidator;
    }

    public InputValidator buildYearChain() {
        emptyValidator.setNext(formatValidator);
        return emptyValidator;
    }

    public ValidationResult validate(InputValidator chain, String input, String fieldName, Object context) {
        return chain.validate(input, fieldName, context);
    }
}