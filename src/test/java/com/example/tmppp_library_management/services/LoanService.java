package com.example.tmppp_library_management.services;

import com.example.tmppp_library_management.book.Book;
import com.example.tmppp_library_management.bridge.PenaltyCalculator;
import com.example.tmppp_library_management.decorator.*;
import com.example.tmppp_library_management.entity.Loan;
import com.example.tmppp_library_management.interfaces.IBorrowable;
import com.example.tmppp_library_management.user.Member;
import com.example.tmppp_library_management.prototype.LoanTemplate;
import com.example.tmppp_library_management.singleton.LoanTemplateRegistry;
import com.example.tmppp_library_management.memento.LoanMemento;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class LoanService {
    private LoanTemplateRegistry templateRegistry;
    private List<Loan> activeLoans;
    private List<Loan> closedLoans;
    private int nextLoanId = 1;
    private Stack<LoanMemento> undoStack;
    private Stack<LoanMemento> redoStack;

    public LoanService() {
        this.templateRegistry = LoanTemplateRegistry.getInstance();
        this.activeLoans = new ArrayList<>();
        this.closedLoans = new ArrayList<>();
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
    }

    private void saveState(Member member, IBorrowable item, Loan loan) {
        Book book = getBookFromItem(item);
        if (book != null) {
            int oldStock = stockService.getStock(book.getIsbn()) != null ?
                    stockService.getStock(book.getIsbn()).getQuantity() : 0;
            LoanMemento memento = new LoanMemento(
                    member.getUserId(), member.getUserName(), member.getUserEmail(),
                    member.getMemberType(), member.getCurrentLoans(),
                    book.getIsbn(), book.getTitle(), oldStock, loan.getLoanId()
            );
            undoStack.push(memento);
            redoStack.clear();
        }
    }

    private Book getBookFromItem(IBorrowable item) {
        if (item instanceof Book) {
            return (Book) item;
        } else if (item instanceof BookDecorator) {
            return ((BookDecorator) item).getOriginalBook();
        }
        return null;
    }

    private StockService stockService = StockService.getInstance();
    private MemberService memberService = MemberService.getInstance();

    public Loan createLoan(Member member, IBorrowable item) {
        if (item instanceof RestrictedAccessDecorator) {
            RestrictedAccessDecorator restricted = (RestrictedAccessDecorator) item;
            if (!restricted.canBorrow(member.getMemberType())) {
                System.out.println("Acces restrictionat pentru aceasta carte");
                return null;
            }
        }

        if (item instanceof ApprovalRequiredDecorator) {
            ApprovalRequiredDecorator approval = (ApprovalRequiredDecorator) item;
            if (!approval.isApproved()) {
                System.out.println("Cartea necesita aprobare");
                return null;
            }
        }

        if (item instanceof ReadingRoomDecorator) {
            System.out.println("Atentie: Aceasta carte poate fi citita doar in sala de lectura!");
        }

        Book book = getBookFromItem(item);
        if (book == null) {
            System.out.println("Itemul nu este o carte valida");
            return null;
        }

        LoanTemplate template = templateRegistry.getTemplateForMember(member.getMemberType());
        if (template == null) {
            throw new IllegalArgumentException("Nu exista template pentru " + member.getMemberType());
        }

        Loan loan = template.createLoan(member, book);
        loan.setLoanId(nextLoanId++);
        loan.setItem(item);
        loan.setActive(true);

        saveState(member, item, loan);

        activeLoans.add(loan);
        return loan;
    }

    public boolean undoLastLoan() {
        if (undoStack.isEmpty()) {
            return false;
        }

        LoanMemento memento = undoStack.pop();
        redoStack.push(memento);

        Member member = memberService.getMember(memento.getMemberId());
        if (member != null) {
            member.setCurrentLoans(memento.getMemberOldLoans());
        }

        StockService stockService = StockService.getInstance();
        stockService.updateStock(memento.getIsbn(), memento.getOldStock() -
                (stockService.getStock(memento.getIsbn()) != null ?
                        stockService.getStock(memento.getIsbn()).getQuantity() : 0));

        for (int i = 0; i < activeLoans.size(); i++) {
            if (activeLoans.get(i).getLoanId() == memento.getLoanId()) {
                Loan loan = activeLoans.get(i);
                loan.setActive(false);
                if (loan.getUser() != null) {
                    memberService.decrementLoans(loan.getUser().getUserId());
                }
                activeLoans.remove(i);
                closedLoans.add(loan);
                break;
            }
        }

        System.out.println("Undo realizat: " + memento.getDescription());
        return true;
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public String getLastUndoDescription() {
        if (undoStack.isEmpty()) return "Nu exista operatii de undo";
        return undoStack.peek().getDescription();
    }

    public int getUndoHistorySize() {
        return undoStack.size();
    }

    public void clearHistory() {
        undoStack.clear();
        redoStack.clear();
    }

    private PenaltyCalculator defaultPenaltyCalculator;

    public void setDefaultPenaltyCalculator(PenaltyCalculator calculator) {
        this.defaultPenaltyCalculator = calculator;
    }

    public PenaltyCalculator getDefaultPenaltyCalculator() {
        return defaultPenaltyCalculator;
    }

    public void closeLoan(Loan loan) {
        if (loan.isActive()) {
            loan.close();
            loan.setActive(false);
            activeLoans.remove(loan);
            closedLoans.add(loan);
        }
    }

    public void renewLoan(Loan loan, int extraDays) {
        if (loan.isActive()) {
            loan.setReturnDate(loan.getReturnDate().plusDays(extraDays));
        }
    }

    public void showTemplates() {
        templateRegistry.listTemplates();
    }

    public List<Loan> getActiveLoans() {
        List<Loan> result = new ArrayList<>();
        for (Loan loan : activeLoans) {
            if (loan.isActive()) {
                result.add(loan);
            }
        }
        return result;
    }

    public List<Loan> getAllClosedLoans() {
        List<Loan> result = new ArrayList<>();
        for (Loan loan : closedLoans) {
            if (!loan.isActive()) {
                result.add(loan);
            }
        }
        for (Loan loan : activeLoans) {
            if (!loan.isActive()) {
                result.add(loan);
            }
        }
        return result;
    }

    public List<Loan> getAllLoans() {
        List<Loan> all = new ArrayList<>();
        all.addAll(getActiveLoans());
        all.addAll(getAllClosedLoans());
        return all;
    }

    public Loan getLoanById(int loanId) {
        for (Loan loan : activeLoans) {
            if (loan.getLoanId() == loanId) {
                return loan;
            }
        }
        for (Loan loan : closedLoans) {
            if (loan.getLoanId() == loanId) {
                return loan;
            }
        }
        return null;
    }
}