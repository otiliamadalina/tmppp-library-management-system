package com.example.tmppp_library_management.chainOfResponsability;

import com.example.tmppp_library_management.services.BookService;
import com.example.tmppp_library_management.services.MemberService;
import com.example.tmppp_library_management.services.NewspaperService;
import com.example.tmppp_library_management.book.Book;
import com.example.tmppp_library_management.newspaper.Newspaper;

public class DuplicateValidator extends BaseInputValidator {

    public enum DuplicateType {
        ISBN, ISSN, EMAIL, MEMBERSHIP, NONE
    }

    private final BookService bookService;
    private final MemberService memberService;
    private final NewspaperService newspaperService;

    public DuplicateValidator(BookService bookService, MemberService memberService, NewspaperService newspaperService) {
        this.bookService = bookService;
        this.memberService = memberService;
        this.newspaperService = newspaperService;
    }

    @Override
    protected ValidationResult performValidation(String input, String fieldName, Object... context) {
        if (context.length == 0 || !(context[0] instanceof DuplicateType)) {
            return ValidationResult.success();
        }

        DuplicateType dupType = (DuplicateType) context[0];
        boolean exists = checkDuplicate(dupType, input);

        if (exists) {
            return ValidationResult.duplicate(fieldName, input);
        }

        return ValidationResult.success();
    }

    private boolean checkDuplicate(DuplicateType type, String value) {
        switch (type) {
            case ISBN:
                return checkIsbnDuplicate(value);
            case ISSN:
                return checkIssnDuplicate(value);
            case EMAIL:
                return checkEmailDuplicate(value);
            case MEMBERSHIP:
                return checkMembershipDuplicate(value);
            default:
                return false;
        }
    }

    private boolean checkIsbnDuplicate(String isbn) {
        if (bookService == null) return false;

        java.util.List<com.example.tmppp_library_management.book.Book> allBooks = bookService.findAllBooksLegacy();
        for (com.example.tmppp_library_management.book.Book book : allBooks) {
            if (book.getIsbn() != null && book.getIsbn().equals(isbn)) {
                return true;
            }
        }

        java.util.List<com.example.tmppp_library_management.interfaces.IBorrowable> allItems = bookService.findAllBooks();
        for (com.example.tmppp_library_management.interfaces.IBorrowable item : allItems) {
            if (item instanceof com.example.tmppp_library_management.decorator.BookDecorator) {
                com.example.tmppp_library_management.decorator.BookDecorator decorator =
                        (com.example.tmppp_library_management.decorator.BookDecorator) item;
                com.example.tmppp_library_management.book.Book original = decorator.getOriginalBook();
                if (original.getIsbn() != null && original.getIsbn().equals(isbn)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean checkIssnDuplicate(String issn) {
        if (newspaperService == null) return false;

        java.util.List<com.example.tmppp_library_management.abstractClasses.LibraryItem> allItems = newspaperService.getAllItems();
        for (com.example.tmppp_library_management.abstractClasses.LibraryItem item : allItems) {
            if (item instanceof Newspaper) {
                Newspaper newspaper = (Newspaper) item;
                if (newspaper.getIssn() != null && newspaper.getIssn().equals(issn)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean checkEmailDuplicate(String email) {
        if (memberService == null) return false;

        java.util.List<com.example.tmppp_library_management.user.Member> allMembers = memberService.getAllMembers();
        for (com.example.tmppp_library_management.user.Member member : allMembers) {
            if (member.getUserEmail() != null && member.getUserEmail().equalsIgnoreCase(email)) {
                return true;
            }
        }

        return false;
    }

    private boolean checkMembershipDuplicate(String membership) {
        if (memberService == null) return false;

        java.util.List<com.example.tmppp_library_management.user.Member> allMembers = memberService.getAllMembers();
        for (com.example.tmppp_library_management.user.Member member : allMembers) {
            if (member.getMembershipNumber() != null && member.getMembershipNumber().equals(membership)) {
                return true;
            }
        }

        return false;
    }
}