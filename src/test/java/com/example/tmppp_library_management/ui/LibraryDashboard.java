package com.example.tmppp_library_management.ui;

import com.example.tmppp_library_management.builder.GiftPackage;
import com.example.tmppp_library_management.book.Author;
import com.example.tmppp_library_management.book.Book;
import com.example.tmppp_library_management.book.FantasyBook;
import com.example.tmppp_library_management.book.RomanceBook;
import com.example.tmppp_library_management.builder.GiftPackageService;
import com.example.tmppp_library_management.builder.PremiumGift;
import com.example.tmppp_library_management.chainOfResponsability.*;
import com.example.tmppp_library_management.composite.EventComponent;
import com.example.tmppp_library_management.composite.EventGroup;
import com.example.tmppp_library_management.composite.EventService;
import com.example.tmppp_library_management.composite.SingleEvent;
import com.example.tmppp_library_management.decorator.ApprovalRequiredDecorator;
import com.example.tmppp_library_management.decorator.BookDecorator;
import com.example.tmppp_library_management.decorator.ReadingRoomDecorator;
import com.example.tmppp_library_management.decorator.RestrictedAccessDecorator;
import com.example.tmppp_library_management.entity.Loan;
import com.example.tmppp_library_management.factories.BookFactory;
import com.example.tmppp_library_management.factories.NewspaperFactory;
import com.example.tmppp_library_management.flyweight.Publisher;
import com.example.tmppp_library_management.flyweight.PublisherFactory;
import com.example.tmppp_library_management.interfaces.IBorrowable;
import com.example.tmppp_library_management.memento.LoanCaretaker;
import com.example.tmppp_library_management.menus.InitializeData;
import com.example.tmppp_library_management.menus.ReceiptMenu;
import com.example.tmppp_library_management.newspaper.LocalNewspaper;
import com.example.tmppp_library_management.newspaper.NationalNewspaper;
import com.example.tmppp_library_management.newspaper.Newspaper;
import com.example.tmppp_library_management.services.*;
import com.example.tmppp_library_management.singleton.LoanTemplateRegistry;
import com.example.tmppp_library_management.user.Librarian;
import com.example.tmppp_library_management.user.Member;
import com.example.tmppp_library_management.user.MemberType;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LibraryDashboard extends JFrame {

    private final BookService bookService;
    private final NewspaperService newspaperService;
    private final StockService stockService;
    private final MemberService memberService;
    private final LoanService loanService;
    private final GiftPackageService giftPackageService;
    private final EventService eventService;
    private final LibrarianService librarianService;
    private final PaymentService paymentService;
    private LoanCaretaker loanCaretaker;
    private ReceiptMenu receiptMenu;
    private JTable loansTable;

    private ValidationChainFactory validationFactory;

    private String currentToken = null;
    private Librarian currentLibrarian = null;

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private String currentPanelName = "books";

    private CommandHistory commandHistory;
    private Map<String, Command> commands;

    private final Color PRIMARY_PINK = new Color(255, 182, 193);
    private final Color DARK_PINK = new Color(219, 112, 147);
    private final Color DARKER_PINK = new Color(199, 21, 133);
    private final Color LIGHT_PINK = new Color(255, 218, 185);

    public LibraryDashboard() {
        BookFactory bookFactory = new BookFactory();
        NewspaperFactory newspaperFactory = new NewspaperFactory();

        this.bookService = new BookService(bookFactory);
        this.newspaperService = new NewspaperService(newspaperFactory);
        this.stockService = StockService.getInstance();
        this.memberService = MemberService.getInstance();
        this.loanService = new LoanService();
        this.giftPackageService = new GiftPackageService(stockService);
        this.eventService = EventService.getInstance();
        this.librarianService = LibrarianService.getInstance();
        this.paymentService = PaymentService.getInstance();
        this.loanCaretaker = new LoanCaretaker(loanService);
        this.receiptMenu = new ReceiptMenu();

        this.commandHistory = new CommandHistory();
        this.commands = new HashMap<>();

        librarianService.injectServices(bookService, newspaperService, memberService,
                loanService, stockService, giftPackageService, eventService);

        this.validationFactory = new ValidationChainFactory(bookService, memberService, newspaperService);

        initializeTestData();
        setupUI();
        setupCommands();

        if (!showLoginDialog()) {
            System.exit(0);
        }
    }

    private void setupCommands() {
        commands.put("books", new ShowBooksCommand(this));
        commands.put("members", new ShowMembersCommand(this));
        commands.put("loans", new ShowLoansCommand(this));
        commands.put("stats", new ShowStatsCommand(this));
        commands.put("events", new ShowEventsCommand(this));
        commands.put("gifts", new ShowGiftsCommand(this));
        commands.put("receipts", new ShowReceiptsCommand(this));
        commands.put("logout", new LogoutCommand(this));
    }

    public void executeCommand(String commandName) {
        Command command = commands.get(commandName);
        if (command != null) {
            command.execute();
            commandHistory.push(command);
        }
    }

    public void undoLastCommand() {
        if (commandHistory.canUndo()) {
            commandHistory.undo();
        } else {
            JOptionPane.showMessageDialog(this, "Nu exista comenzi de anulat!", "Undo", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void redoLastCommand() {
        if (commandHistory.canRedo()) {
            commandHistory.redo();
        } else {
            JOptionPane.showMessageDialog(this, "Nu exista comenzi de refacut!", "Redo", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void showPanel(String panelName) {
        this.currentPanelName = panelName;
        cardLayout.show(contentPanel, panelName);
    }

    public String getCurrentPanelName() {
        return currentPanelName;
    }

    public void performLogout() {
        librarianService.logout(currentToken);
        currentToken = null;
        dispose();
        new LibraryDashboard().setVisible(true);
    }

    private void initializeTestData() {
        LoanTemplateRegistry.getInstance();
        InitializeData initializer = new InitializeData(bookService, newspaperService,
                memberService, stockService, loanService, receiptMenu);
        initializer.initializeAll();
    }

    private String validateField(String value, String fieldName, InputValidator chain, Object context) {
        ValidationResult result = chain.validate(value, fieldName, context);
        if (!result.isValid()) {
            JOptionPane.showMessageDialog(this, result.getErrorMessage(), "Eroare validare", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return value;
    }

    private String validateName(String name) {
        return validateField(name, "Nume", validationFactory.buildNameChain(), FormatInputValidator.FormatType.NAME);
    }

    private String validateEmail(String email) {
        return validateField(email, "Email", validationFactory.buildEmailChain(), FormatInputValidator.FormatType.EMAIL);
    }

    private String validateTitle(String title) {
        return validateField(title, "Titlu", validationFactory.buildTitleChain(), FormatInputValidator.FormatType.TITLE);
    }

    private String validateYear(String yearStr) {
        return validateField(yearStr, "An", validationFactory.buildYearChain(), FormatInputValidator.FormatType.YEAR);
    }

    private String validateIsbn(String isbn) {
        return validateField(isbn, "ISBN", validationFactory.buildIsbnChain(), FormatInputValidator.FormatType.ISBN);
    }

    private String validateIssn(String issn) {
        return validateField(issn, "ISSN", validationFactory.buildIssnChain(), FormatInputValidator.FormatType.ISSN);
    }

    private boolean showLoginDialog() {
        JDialog loginDialog = new JDialog(this, "Autentificare", true);
        loginDialog.setSize(400, 280);
        loginDialog.setLayout(new BorderLayout());
        loginDialog.setLocationRelativeTo(this);
        loginDialog.getContentPane().setBackground(PRIMARY_PINK);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(PRIMARY_PINK);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Autentificare Librarian");
        titleLabel.setFont(new Font("Garamond", Font.BOLD, 20));
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(Color.BLACK);
        userLabel.setFont(new Font("Garamond", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(userLabel, gbc);

        JTextField usernameField = new JTextField(20);
        usernameField.setBackground(Color.WHITE);
        usernameField.setForeground(Color.BLACK);
        usernameField.setFont(new Font("Garamond", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 1;
        mainPanel.add(usernameField, gbc);

        JLabel passLabel = new JLabel("Parola:");
        passLabel.setForeground(Color.BLACK);
        passLabel.setFont(new Font("Garamond", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(passLabel, gbc);

        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setBackground(Color.WHITE);
        passwordField.setForeground(Color.BLACK);
        passwordField.setFont(new Font("Garamond", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 2;
        mainPanel.add(passwordField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(PRIMARY_PINK);

        JButton loginBtn = createStyledButton("Login", DARK_PINK, Color.BLACK);
        JButton cancelBtn = createStyledButton("Cancel", DARK_PINK, Color.BLACK);

        buttonPanel.add(loginBtn);
        buttonPanel.add(cancelBtn);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        mainPanel.add(buttonPanel, gbc);

        loginDialog.add(mainPanel, BorderLayout.CENTER);

        final boolean[] loggedIn = {false};

        loginBtn.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            currentToken = librarianService.login(username, password);
            if (currentToken != null) {
                currentLibrarian = librarianService.getCurrentLibrarian(currentToken);
                loggedIn[0] = true;
                loginDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(loginDialog,
                        "Username sau parola incorecta!\n\nConturi test:\nadmin / admin123\nmaria / parola123",
                        "Eroare autentificare",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> {
            loggedIn[0] = false;
            loginDialog.dispose();
        });

        loginDialog.setVisible(true);
        return loggedIn[0];
    }

    private void setupUI() {
        setTitle("Library Management System");
        setSize(1300, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(PRIMARY_PINK);
        setLayout(new BorderLayout());

        JPanel sideMenu = createSideMenu();
        add(sideMenu, BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(PRIMARY_PINK);

        contentPanel.add(createBooksPanel(), "books");
        contentPanel.add(createNewspapersPanel(), "newspapers");
        contentPanel.add(createMembersPanel(), "members");
        contentPanel.add(createLoansPanel(), "loans");
        contentPanel.add(createGiftsPanel(), "gifts");
        contentPanel.add(createEventsPanel(), "events");
        contentPanel.add(createStatsPanel(), "stats");
        contentPanel.add(createReceiptsPanel(), "receipts");

        add(contentPanel, BorderLayout.CENTER);
        add(createTopBar(), BorderLayout.NORTH);
    }

    private JPanel createSideMenu() {
        JPanel sideMenu = new JPanel();
        sideMenu.setPreferredSize(new Dimension(200, 0));
        sideMenu.setBackground(DARKER_PINK);
        sideMenu.setLayout(new BoxLayout(sideMenu, BoxLayout.Y_AXIS));

        String[] menuItems = {"Carti", "Ziare", "Membri", "Imprumuturi", "Pachete Cadou", "Evenimente", "Statistici", "Chitante", "Undo", "Redo", "Delogare"};
        String[] commandNames = {"books", "newspapers", "members", "loans", "gifts", "events", "stats", "receipts", "undo", "redo", "logout"};

        sideMenu.add(Box.createVerticalStrut(30));

        for (int i = 0; i < menuItems.length; i++) {
            String item = menuItems[i];
            String cmdName = commandNames[i];
            JButton btn = createMenuButton(item, DARK_PINK, DARKER_PINK);

            if (cmdName.equals("undo")) {
                btn.addActionListener(e -> undoLastCommand());
            } else if (cmdName.equals("redo")) {
                btn.addActionListener(e -> redoLastCommand());
            } else if (cmdName.equals("logout")) {
                btn.addActionListener(e -> executeCommand(cmdName));
            } else if (cmdName.equals("newspapers")) {
                btn.addActionListener(e -> cardLayout.show(contentPanel, "newspapers"));
            } else {
                btn.addActionListener(e -> executeCommand(cmdName));
            }

            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            sideMenu.add(btn);
            sideMenu.add(Box.createVerticalStrut(10));
        }

        sideMenu.add(Box.createVerticalGlue());

        return sideMenu;
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(DARK_PINK);
        topBar.setPreferredSize(new Dimension(0, 60));

        JLabel welcomeLabel = new JLabel("  Bine ai venit, " + (currentLibrarian != null ? currentLibrarian.getUserName() : "Admin") + "!");
        welcomeLabel.setForeground(Color.BLACK);
        welcomeLabel.setFont(new Font("Garamond", Font.BOLD, 18));

        JLabel dateLabel = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + "  ");
        dateLabel.setForeground(Color.BLACK);
        dateLabel.setFont(new Font("Garamond", Font.PLAIN, 14));

        topBar.add(welcomeLabel, BorderLayout.WEST);
        topBar.add(dateLabel, BorderLayout.EAST);

        return topBar;
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setFont(new Font("Garamond", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }

    private JButton createMenuButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(180, 45));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        btn.setFont(new Font("Garamond", Font.BOLD, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }

    private JPanel createBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PRIMARY_PINK);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(PRIMARY_PINK);
        JButton addBookBtn = createStyledButton("Adauga Carte", DARK_PINK, Color.BLACK);
        JButton deleteBookBtn = createStyledButton("Sterge Carte", DARK_PINK, Color.BLACK);
        JButton setRestrictionBtn = createStyledButton("Set Restrictii", DARK_PINK, Color.BLACK);
        JButton refreshBtn = createStyledButton("Refresh", DARK_PINK, Color.BLACK);
        buttonPanel.add(addBookBtn);
        buttonPanel.add(deleteBookBtn);
        buttonPanel.add(setRestrictionBtn);
        buttonPanel.add(refreshBtn);

        JTable table = new JTable();
        table.setModel(new BooksTableModel());
        styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        refreshBtn.addActionListener(e -> ((BooksTableModel) table.getModel()).refresh());
        addBookBtn.addActionListener(e -> showAddBookDialog(table));
        deleteBookBtn.addActionListener(e -> showDeleteBookDialog(table));
        setRestrictionBtn.addActionListener(e -> showSetRestrictionDialog(table));

        return panel;
    }

    private JPanel createNewspapersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PRIMARY_PINK);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(PRIMARY_PINK);
        JButton addNewspaperBtn = createStyledButton("Adauga Ziar", DARK_PINK, Color.BLACK);
        JButton refreshBtn = createStyledButton("Refresh", DARK_PINK, Color.BLACK);
        buttonPanel.add(addNewspaperBtn);
        buttonPanel.add(refreshBtn);

        JTable table = new JTable();
        table.setModel(new NewspapersTableModel());
        styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        refreshBtn.addActionListener(e -> ((NewspapersTableModel) table.getModel()).refresh());
        addNewspaperBtn.addActionListener(e -> showAddNewspaperDialog(table));

        return panel;
    }

    private JPanel createMembersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PRIMARY_PINK);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(PRIMARY_PINK);
        JButton addMemberBtn = createStyledButton("Adauga Membru", DARK_PINK, Color.BLACK);
        JButton refreshBtn = createStyledButton("Refresh", DARK_PINK, Color.BLACK);
        buttonPanel.add(addMemberBtn);
        buttonPanel.add(refreshBtn);

        JTable table = new JTable();
        table.setModel(new MembersTableModel());
        styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        refreshBtn.addActionListener(e -> ((MembersTableModel) table.getModel()).refresh());
        addMemberBtn.addActionListener(e -> showAddMemberDialog(table));

        return panel;
    }

    class ActiveLoansTableModel extends AbstractTableModel {
        private List<Loan> loans;
        private final String[] columns = {"ID", "Membru", "Carte", "Restrictii", "Penalitate", "Data imprumut", "Returnare", "Status"};

        public ActiveLoansTableModel() {
            refresh();
        }

        public void refresh() {
            this.loans = loanService.getActiveLoans();
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() { return loans != null ? loans.size() : 0; }

        @Override
        public int getColumnCount() { return columns.length; }

        @Override
        public Object getValueAt(int row, int col) {
            Loan l = loans.get(row);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            return switch (col) {
                case 0 -> l.getLoanId();
                case 1 -> l.getUser().getUserName();
                case 2 -> l.getBookTitle();
                case 3 -> getLoanRestrictions(l);
                case 4 -> String.format("%.2f lei", l.calculatePenalty());
                case 5 -> l.getStartDate().format(formatter);
                case 6 -> l.getReturnDate().format(formatter);
                case 7 -> l.isActive() ? "Activ" : "Inchis";
                default -> "";
            };
        }

        private String getLoanRestrictions(Loan loan) {
            IBorrowable item = loan.getItem();
            if (item instanceof ReadingRoomDecorator) {
                ReadingRoomDecorator rr = (ReadingRoomDecorator) item;
                return "Doar in sala (" + rr.getRoom() + ")";
            } else if (item instanceof RestrictedAccessDecorator) {
                RestrictedAccessDecorator ra = (RestrictedAccessDecorator) item;
                return "Acces: " + ra.getRequiredLevel();
            } else if (item instanceof ApprovalRequiredDecorator) {
                ApprovalRequiredDecorator ar = (ApprovalRequiredDecorator) item;
                return "Aprobare: " + (ar.isApproved() ? "Aprobat" : "Necesara");
            }
            return "Fara restrictii";
        }

        @Override
        public String getColumnName(int col) { return columns[col]; }
    }

    class ClosedLoansTableModel extends AbstractTableModel {
        private List<Loan> loans;
        private final String[] columns = {"ID", "Membru", "Carte", "Data imprumut", "Data returnare", "Status"};

        public ClosedLoansTableModel() {
            refresh();
        }

        public void refresh() {
            this.loans = loanService.getAllClosedLoans();
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() { return loans != null ? loans.size() : 0; }

        @Override
        public int getColumnCount() { return columns.length; }

        @Override
        public Object getValueAt(int row, int col) {
            Loan l = loans.get(row);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            return switch (col) {
                case 0 -> l.getLoanId();
                case 1 -> l.getUser().getUserName();
                case 2 -> l.getBookTitle();
                case 3 -> l.getStartDate().format(formatter);
                case 4 -> l.getReturnDate().format(formatter);
                case 5 -> l.isActive() ? "Activ" : "Inchis";
                default -> "";
            };
        }

        @Override
        public String getColumnName(int col) { return columns[col]; }
    }

    private void showReturnLoanDialog(JTable activeTable, JTable historyTable) {
        int selectedRow = activeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Selectati un imprumut pentru returnare!", "Eroare", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int loanId = (int) activeTable.getValueAt(selectedRow, 0);
        Loan loan = loanService.getLoanById(loanId);

        if (loan == null) {
            JOptionPane.showMessageDialog(this, "Imprumutul nu a fost gasit!", "Eroare", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Book book = loan.getBook();
        Member member = loan.getUser();

        LocalDate returnDate = LocalDate.now();
        int daysLate = 0;
        double latePenalty = 0;

        if (returnDate.isAfter(loan.getReturnDate())) {
            daysLate = (int) java.time.temporal.ChronoUnit.DAYS.between(loan.getReturnDate(), returnDate);
            latePenalty = daysLate * 1.0;
        }

        String[] damageOptions = {"In stare buna", "Usor deteriorata (20% din pret)", "Foarte deteriorata (50% din pret)", "Pierduta (100% din pret)"};
        int damageChoice = JOptionPane.showOptionDialog(this,
                "Selectati starea cartii:\n\n" +
                        "Carte: " + book.getTitle() + "\n" +
                        "Pret carte: " + book.getPrice() + " lei\n" +
                        "Zile intarziere: " + daysLate + "\n" +
                        "Penalizare intarziere: " + latePenalty + " lei",
                "Stare Carte",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, damageOptions, damageOptions[0]);

        double damagePenalty = 0;
        String damageDescription = "";

        switch (damageChoice) {
            case 1:
                damagePenalty = book.getPrice() * 0.20;
                damageDescription = "Usor deteriorata - 20% din pret";
                break;
            case 2:
                damagePenalty = book.getPrice() * 0.50;
                damageDescription = "Foarte deteriorata - 50% din pret";
                break;
            case 3:
                damagePenalty = book.getPrice();
                damageDescription = "Pierduta - 100% din pret";
                break;
            default:
                damageDescription = "In stare buna";
        }

        double totalPenalty = latePenalty + damagePenalty;

        if (totalPenalty > 0) {
            String[] paymentOptions = {"Da, s-a platit", "Nu, nu s-a platit"};
            int paymentChoice = JOptionPane.showOptionDialog(this,
                    "=== DETALII PLATA ===\n\n" +
                            "Carte: " + book.getTitle() + "\n" +
                            "Pret carte: " + book.getPrice() + " lei\n\n" +
                            "Penalizare intarziere (" + daysLate + " zile): " + latePenalty + " lei\n" +
                            "Penalizare dauna: " + damagePenalty + " lei (" + damageDescription + ")\n" +
                            "TOTAL DE PLATA: " + totalPenalty + " lei\n\n" +
                            "S-a primit plata?",
                    "Confirmare Plata",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, paymentOptions, paymentOptions[0]);

            if (paymentChoice == 1) {
                JOptionPane.showMessageDialog(this, "Returnarea nu poate fi finalizata fara plata!", "Eroare", JOptionPane.ERROR_MESSAGE);
                return;
            }

            paymentService.processPayment(totalPenalty, "CASH",
                    "Returnare - " + book.getTitle() + " - " + damageDescription + " - intarziere " + daysLate + " zile",
                    member.getUserId());
        }

        loan.close();
        stockService.increaseStock(book.getIsbn(), 1);

        receiptMenu.printReturnReceipt(loan, returnDate, daysLate, totalPenalty, totalPenalty > 0);

        ((ActiveLoansTableModel) activeTable.getModel()).refresh();
        ((ClosedLoansTableModel) historyTable.getModel()).refresh();

        JOptionPane.showMessageDialog(this,
                "Returnare procesata cu succes!\n\n" +
                        "Carte: " + book.getTitle() + "\n" +
                        "Membru: " + member.getUserName() + "\n" +
                        "Total plata: " + totalPenalty + " lei",
                "Succes",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel createLoansPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PRIMARY_PINK);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(PRIMARY_PINK);
        JButton createLoanBtn = createStyledButton("Creaza Imprumut", DARK_PINK, Color.BLACK);
        JButton returnLoanBtn = createStyledButton("Returneaza", DARK_PINK, Color.BLACK);
        JButton renewLoanBtn = createStyledButton("Prelungeste", DARK_PINK, Color.BLACK);
        JButton undoBtn = createStyledButton("Undo Ultimul Imprumut", DARK_PINK, Color.BLACK);
        JButton showHistoryBtn = createStyledButton("Istoric Imprumuturi", DARK_PINK, Color.BLACK);
        JButton refreshBtn = createStyledButton("Refresh", DARK_PINK, Color.BLACK);
        buttonPanel.add(createLoanBtn);
        buttonPanel.add(returnLoanBtn);
        buttonPanel.add(renewLoanBtn);
        buttonPanel.add(undoBtn);
        buttonPanel.add(showHistoryBtn);
        buttonPanel.add(refreshBtn);

        JTable activeTable = new JTable();
        activeTable.setModel(new ActiveLoansTableModel());
        styleTable(activeTable);
        activeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.loansTable = activeTable;

        JScrollPane activeScrollPane = new JScrollPane(activeTable);
        activeScrollPane.setBorder(BorderFactory.createTitledBorder("Imprumuturi Active"));

        JTable historyTable = new JTable();
        historyTable.setModel(new ClosedLoansTableModel());
        styleTable(historyTable);
        JScrollPane historyScrollPane = new JScrollPane(historyTable);
        historyScrollPane.setBorder(BorderFactory.createTitledBorder("Istoric Imprumuturi"));
        historyScrollPane.setVisible(false);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, activeScrollPane, historyScrollPane);
        splitPane.setBackground(PRIMARY_PINK);
        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(0.5);

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(splitPane, BorderLayout.CENTER);

        showHistoryBtn.addActionListener(e -> {
            boolean isVisible = historyScrollPane.isVisible();
            historyScrollPane.setVisible(!isVisible);
            splitPane.setDividerLocation(300);
            if (!isVisible) {
                ((ClosedLoansTableModel) historyTable.getModel()).refresh();
                showHistoryBtn.setText("Ascunde Istoric");
            } else {
                showHistoryBtn.setText("Istoric Imprumuturi");
            }
        });

        refreshBtn.addActionListener(e -> {
            ((ActiveLoansTableModel) activeTable.getModel()).refresh();
            if (historyScrollPane.isVisible()) {
                ((ClosedLoansTableModel) historyTable.getModel()).refresh();
            }
        });
        createLoanBtn.addActionListener(e -> showCreateLoanDialog(activeTable));
        returnLoanBtn.addActionListener(e -> showReturnLoanDialog(activeTable, historyTable));
        renewLoanBtn.addActionListener(e -> renewLoan(activeTable));
        undoBtn.addActionListener(e -> {
            if (loanCaretaker.canUndo()) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Esti sigur ca vrei sa anulezi ultimul imprumut?\n" +
                                loanCaretaker.getUndoDescription(),
                        "Confirmare Undo",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (loanCaretaker.undo()) {
                        ((ActiveLoansTableModel) activeTable.getModel()).refresh();
                        JOptionPane.showMessageDialog(this, "Ultimul imprumut a fost anulat cu succes!", "Undo", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Nu s-a putut face undo!", "Eroare", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Nu exista imprumuturi de anulat!", "Undo", JOptionPane.WARNING_MESSAGE);
            }
        });

        return panel;
    }

    private JPanel createGiftsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PRIMARY_PINK);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(PRIMARY_PINK);
        JButton createStandardBtn = createStyledButton("Pachet Standard", DARK_PINK, Color.BLACK);
        JButton createPremiumBtn = createStyledButton("Pachet Premium", DARK_PINK, Color.BLACK);
        JButton refreshBtn = createStyledButton("Refresh", DARK_PINK, Color.BLACK);
        buttonPanel.add(createStandardBtn);
        buttonPanel.add(createPremiumBtn);
        buttonPanel.add(refreshBtn);

        JTable table = new JTable();
        table.setModel(new GiftsTableModel());
        styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        refreshBtn.addActionListener(e -> ((GiftsTableModel) table.getModel()).refresh());
        createStandardBtn.addActionListener(e -> showCreateGiftDialog("standard", table));
        createPremiumBtn.addActionListener(e -> showCreateGiftDialog("premium", table));

        return panel;
    }

    private JPanel createReceiptsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PRIMARY_PINK);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(PRIMARY_PINK);

        JButton processReturnBtn = createStyledButton("Procesare Returnare", DARK_PINK, Color.BLACK);
        JButton showHistoryBtn = createStyledButton("Istoric Chitante", DARK_PINK, Color.BLACK);
        JButton clearHistoryBtn = createStyledButton("Goleste Istoric", DARK_PINK, Color.BLACK);

        buttonPanel.add(processReturnBtn);
        buttonPanel.add(showHistoryBtn);
        buttonPanel.add(clearHistoryBtn);

        JTextArea receiptArea = new JTextArea();
        receiptArea.setEditable(false);
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        receiptArea.setBackground(LIGHT_PINK);
        receiptArea.setForeground(Color.BLACK);

        JScrollPane scrollPane = new JScrollPane(receiptArea);
        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        processReturnBtn.addActionListener(e -> showProcessReturnDialog(receiptArea));
        showHistoryBtn.addActionListener(e -> showReceiptHistory(receiptArea));
        clearHistoryBtn.addActionListener(e -> {
            receiptMenu.clearHistory();
            receiptArea.setText("Istoric golit.\n");
        });

        return panel;
    }

    class EventsTableModel extends AbstractTableModel {
        private List<SingleEvent> events;
        private final String[] columns = {"ID", "Nume", "Data", "Locatie", "Tip", "Locuri", "Inscrisi"};

        public EventsTableModel() {
            refresh();
        }

        public void refresh() {
            this.events = eventService.getAllSingleEvents();
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() { return events != null ? events.size() : 0; }

        @Override
        public int getColumnCount() { return columns.length; }

        @Override
        public Object getValueAt(int row, int col) {
            SingleEvent e = events.get(row);
            return switch (col) {
                case 0 -> e.getId();
                case 1 -> e.getName();
                case 2 -> e.getDate();
                case 3 -> e.getLocation();
                case 4 -> e.getType();
                case 5 -> e.getMaxParticipants() == -1 ? "Nelimitat" : String.valueOf(e.getMaxParticipants());
                case 6 -> e.getRegisteredParticipants() + "/" + (e.getMaxParticipants() == -1 ? "∞" : e.getMaxParticipants());
                default -> "";
            };
        }

        @Override
        public String getColumnName(int col) { return columns[col]; }
    }

    private JPanel createEventsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PRIMARY_PINK);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(PRIMARY_PINK);
        JButton addEventBtn = createStyledButton("Adauga Eveniment", DARK_PINK, Color.BLACK);
        JButton addGroupBtn = createStyledButton("Adauga Grup", DARK_PINK, Color.BLACK);
        JButton addToGroupBtn = createStyledButton("Adauga Eveniment in Grup", DARK_PINK, Color.BLACK);
        JButton registerEventBtn = createStyledButton("Inregistrare Eveniment", DARK_PINK, Color.BLACK);
        JButton refreshBtn = createStyledButton("Refresh", DARK_PINK, Color.BLACK);
        buttonPanel.add(addEventBtn);
        buttonPanel.add(addGroupBtn);
        buttonPanel.add(addToGroupBtn);
        buttonPanel.add(registerEventBtn);
        buttonPanel.add(refreshBtn);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setBackground(PRIMARY_PINK);
        splitPane.setDividerLocation(600);

        JTable eventsTable = new JTable();
        eventsTable.setModel(new EventsTableModel());
        styleTable(eventsTable);
        eventsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScrollPane = new JScrollPane(eventsTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Evenimente Simple"));

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Toate Evenimentele");
        DefaultTreeModel treeModel = new DefaultTreeModel(root);
        JTree eventTree = new JTree(treeModel);
        eventTree.setRootVisible(true);
        eventTree.setShowsRootHandles(true);
        eventTree.setBackground(LIGHT_PINK);
        eventTree.setForeground(Color.BLACK);
        eventTree.setFont(new Font("Garamond", Font.PLAIN, 12));
        JScrollPane treeScrollPane = new JScrollPane(eventTree);
        treeScrollPane.setBorder(BorderFactory.createTitledBorder("Grupuri Evenimente"));

        splitPane.setLeftComponent(tableScrollPane);
        splitPane.setRightComponent(treeScrollPane);

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(splitPane, BorderLayout.CENTER);

        refreshEventsTable(eventsTable);
        refreshEventTree(eventTree);

        refreshBtn.addActionListener(e -> {
            refreshEventsTable(eventsTable);
            refreshEventTree(eventTree);
        });
        addEventBtn.addActionListener(e -> showAddEventDialog(eventsTable, eventTree));
        addGroupBtn.addActionListener(e -> showAddGroupDialog(eventTree));
        addToGroupBtn.addActionListener(e -> showAddEventToGroupDialog(eventsTable, eventTree));
        registerEventBtn.addActionListener(e -> showRegisterToEventDialog(eventTree));

        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PRIMARY_PINK);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTextArea statsArea = new JTextArea();
        statsArea.setEditable(false);
        statsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        statsArea.setBackground(LIGHT_PINK);
        statsArea.setForeground(Color.BLACK);

        JScrollPane scrollPane = new JScrollPane(statsArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        refreshStatsPanel(statsArea);

        return panel;
    }

    private void styleTable(JTable table) {
        table.getTableHeader().setBackground(DARK_PINK);
        table.getTableHeader().setForeground(Color.BLACK);
        table.getTableHeader().setFont(new Font("Garamond", Font.BOLD, 12));
        table.setRowHeight(25);
        table.setBackground(LIGHT_PINK);
        table.setForeground(Color.BLACK);
        table.setFont(new Font("Garamond", Font.PLAIN, 12));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? LIGHT_PINK : Color.WHITE);
                    setForeground(Color.BLACK);
                } else {
                    setBackground(DARK_PINK);
                    setForeground(Color.WHITE);
                }
                return this;
            }
        });
    }

    class BooksTableModel extends AbstractTableModel implements com.example.tmppp_library_management.observer.BookObserver {
        private List<IBorrowable> books;
        private final String[] columns = {"ID", "Titlu", "Autor", "An", "Stoc", "ISBN", "Pret", "Restrictii"};

        public BooksTableModel() {
            refresh();
            bookService.attach(this);
        }

        public void refresh() {
            this.books = bookService.findAllBooks();
            fireTableDataChanged();
        }

        @Override
        public void update(com.example.tmppp_library_management.observer.BookEvent event) {
            refresh();
            System.out.println("[Observer] " + event.getMessage());
        }

        @Override
        public int getRowCount() { return books != null ? books.size() : 0; }

        @Override
        public int getColumnCount() { return columns.length; }

        @Override
        public Object getValueAt(int row, int col) {
            IBorrowable item = books.get(row);
            Book book = getBookFromItem(item);
            if (book == null) return "";

            return switch (col) {
                case 0 -> book.getItemId();
                case 1 -> book.getTitle();
                case 2 -> book.getAuthor().getName();
                case 3 -> book.getPublicationDate();
                case 4 -> stockService.getStock(book.getIsbn()) != null ?
                        stockService.getStock(book.getIsbn()).getAvailableQuantity() : 0;
                case 5 -> formatIsbnForDisplay(book.getIsbn());
                case 6 -> String.format("%.2f lei", book.getPrice());
                case 7 -> getRestrictions(item);
                default -> "";
            };
        }

        private String formatIsbnForDisplay(String isbn) {
            if (isbn == null) return "";
            String digits = isbn.replaceAll("-", "");
            if (digits.length() == 12) {
                return digits.substring(0, 3) + "-" +
                        digits.substring(3, 6) + "-" +
                        digits.substring(6, 9) + "-" +
                        digits.substring(9);
            }
            return isbn;
        }

        private String getRestrictions(IBorrowable item) {
            if (item instanceof ReadingRoomDecorator) {
                ReadingRoomDecorator rr = (ReadingRoomDecorator) item;
                return "Doar in sala (" + rr.getRoom() + ")";
            } else if (item instanceof RestrictedAccessDecorator) {
                RestrictedAccessDecorator ra = (RestrictedAccessDecorator) item;
                return "Acces: " + ra.getRequiredLevel();
            } else if (item instanceof ApprovalRequiredDecorator) {
                ApprovalRequiredDecorator ar = (ApprovalRequiredDecorator) item;
                return "Aprobare: " + (ar.isApproved() ? "Aprobat" : "Necesara");
            }
            return "Fara restrictii";
        }

        @Override
        public String getColumnName(int col) { return columns[col]; }

        private Book getBookFromItem(IBorrowable item) {
            if (item instanceof Book) {
                return (Book) item;
            } else if (item instanceof BookDecorator) {
                return ((BookDecorator) item).getOriginalBook();
            }
            return null;
        }
    }

    class NewspapersTableModel extends AbstractTableModel {
        private List<Newspaper> newspapers;
        private final String[] columns = {"ID", "Titlu", "Tip", "Publicatie", "ISSN"};

        public NewspapersTableModel() {
            refresh();
        }

        public void refresh() {
            this.newspapers = newspaperService.getAllItems().stream()
                    .filter(item -> item instanceof Newspaper)
                    .map(item -> (Newspaper) item)
                    .toList();
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() { return newspapers != null ? newspapers.size() : 0; }

        @Override
        public int getColumnCount() { return columns.length; }

        @Override
        public Object getValueAt(int row, int col) {
            Newspaper n = newspapers.get(row);
            return switch (col) {
                case 0 -> n.getItemId();
                case 1 -> n.getTitle();
                case 2 -> n instanceof LocalNewspaper ? "Local" : "National";
                case 3 -> n.getPublisher();
                case 4 -> n.getIssn();
                default -> "";
            };
        }

        @Override
        public String getColumnName(int col) { return columns[col]; }
    }

    class MembersTableModel extends AbstractTableModel {
        private List<Member> members;
        private final String[] columns = {"ID", "Nume", "Email", "Membership", "Tip", "Imprumuturi"};

        public MembersTableModel() {
            refresh();
        }

        public void refresh() {
            this.members = memberService.getAllMembers();
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() { return members != null ? members.size() : 0; }

        @Override
        public int getColumnCount() { return columns.length; }

        @Override
        public Object getValueAt(int row, int col) {
            Member m = members.get(row);
            return switch (col) {
                case 0 -> m.getUserId();
                case 1 -> m.getUserName();
                case 2 -> m.getUserEmail();
                case 3 -> m.getMembershipNumber();
                case 4 -> m.getMemberType();
                case 5 -> m.getCurrentLoans() + "/" + m.getMaxBooks();
                default -> "";
            };
        }

        @Override
        public String getColumnName(int col) { return columns[col]; }
    }

    class GiftsTableModel extends AbstractTableModel {
        private List<GiftPackage> gifts;
        private final String[] columns = {"Carte", "Tip", "Pret"};

        public GiftsTableModel() {
            refresh();
        }

        public void refresh() {
            this.gifts = giftPackageService.getCreatedPackages();
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() { return gifts != null ? gifts.size() : 0; }

        @Override
        public int getColumnCount() { return columns.length; }

        @Override
        public Object getValueAt(int row, int col) {
            GiftPackage g = gifts.get(row);
            return switch (col) {
                case 0 -> g.getBook().getTitle();
                case 1 -> g instanceof PremiumGift ? "Premium" : "Standard";
                case 2 -> String.format("%.2f lei", g.calculateTotalPrice());
                default -> "";
            };
        }

        @Override
        public String getColumnName(int col) { return columns[col]; }
    }

    private Book getBookFromItem(IBorrowable item) {
        if (item instanceof Book) {
            return (Book) item;
        } else if (item instanceof BookDecorator) {
            return ((BookDecorator) item).getOriginalBook();
        }
        return null;
    }

    private Loan findActiveLoan(int bookId, int memberId) {
        for (Loan loan : loanService.getActiveLoans()) {
            Book book = loan.getBook();
            if (book != null && book.getItemId() == bookId &&
                    loan.getUser().getUserId() == memberId) {
                return loan;
            }
        }
        return null;
    }

    private void showSetRestrictionDialog(JTable table) {
        List<IBorrowable> allItems = bookService.findAllBooks();
        List<Book> simpleBooks = new ArrayList<>();

        for (IBorrowable item : allItems) {
            Book book = getBookFromItem(item);
            if (book != null) {
                simpleBooks.add(book);
            }
        }

        if (simpleBooks.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nu exista carti in sistem!", "Eroare", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Setare Restrictii Carte", true);
        dialog.setSize(500, 400);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(PRIMARY_PINK);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<Book> bookCombo = new JComboBox<>(simpleBooks.toArray(new Book[0]));
        bookCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof Book) {
                    Book b = (Book) value;
                    setText(b.getTitle() + " - " + b.getAuthor().getName() + " (ID: " + b.getItemId() + ")");
                } else {
                    setText(value != null ? value.toString() : "");
                }
                if (isSelected) {
                    setBackground(list.getSelectionBackground());
                    setForeground(list.getSelectionForeground());
                } else {
                    setBackground(list.getBackground());
                    setForeground(list.getForeground());
                }
                return this;
            }
        });

        String[] restrictionTypes = {
                "Fara restrictii",
                "Doar in sala de lectura",
                "Acces restrictionat",
                "Necesita aprobare"
        };
        JComboBox<String> restrictionTypeCombo = new JComboBox<>(restrictionTypes);

        JTextField roomField = new JTextField(20);
        JComboBox<MemberType> accessLevelCombo = new JComboBox<>(MemberType.values());

        styleTextField(roomField);

        int row = 0;

        JPanel bookPanel = new JPanel(new BorderLayout());
        bookPanel.setBackground(PRIMARY_PINK);
        JLabel bookLabel = new JLabel("Selecteaza carte:");
        bookLabel.setForeground(Color.BLACK);
        bookPanel.add(bookLabel, BorderLayout.WEST);
        bookPanel.add(bookCombo, BorderLayout.CENTER);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        dialog.add(bookPanel, gbc);
        row++;

        JPanel typePanel = new JPanel(new BorderLayout());
        typePanel.setBackground(PRIMARY_PINK);
        JLabel typeLabel = new JLabel("Tip restrictie:");
        typeLabel.setForeground(Color.BLACK);
        typePanel.add(typeLabel, BorderLayout.WEST);
        typePanel.add(restrictionTypeCombo, BorderLayout.CENTER);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        dialog.add(typePanel, gbc);
        row++;

        JPanel roomPanel = new JPanel(new BorderLayout());
        roomPanel.setBackground(PRIMARY_PINK);
        JLabel roomLabel = new JLabel("Sala de lectura:");
        roomLabel.setForeground(Color.BLACK);
        roomPanel.add(roomLabel, BorderLayout.WEST);
        roomPanel.add(roomField, BorderLayout.CENTER);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        dialog.add(roomPanel, gbc);
        row++;

        JPanel accessPanel = new JPanel(new BorderLayout());
        accessPanel.setBackground(PRIMARY_PINK);
        JLabel accessLabel = new JLabel("Nivel acces:");
        accessLabel.setForeground(Color.BLACK);
        accessPanel.add(accessLabel, BorderLayout.WEST);
        accessPanel.add(accessLevelCombo, BorderLayout.CENTER);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        dialog.add(accessPanel, gbc);
        row++;

        roomPanel.setVisible(false);
        accessPanel.setVisible(false);

        restrictionTypeCombo.addActionListener(e -> {
            String selected = (String) restrictionTypeCombo.getSelectedItem();
            roomPanel.setVisible(false);
            accessPanel.setVisible(false);
            if ("Doar in sala de lectura".equals(selected)) {
                roomPanel.setVisible(true);
            } else if ("Acces restrictionat".equals(selected)) {
                accessPanel.setVisible(true);
            }
            dialog.pack();
        });

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(PRIMARY_PINK);
        JButton saveBtn = createStyledButton("Salveaza", DARK_PINK, Color.BLACK);
        JButton cancelBtn = createStyledButton("Anuleaza", DARK_PINK, Color.BLACK);
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        saveBtn.addActionListener(e -> {
            try {
                Book selectedBook = (Book) bookCombo.getSelectedItem();
                if (selectedBook == null) {
                    throw new Exception("Selecteaza o carte");
                }

                String restrictionType = (String) restrictionTypeCombo.getSelectedItem();
                IBorrowable updatedBook = null;

                switch (restrictionType) {
                    case "Fara restrictii":
                        updatedBook = selectedBook;
                        break;
                    case "Doar in sala de lectura":
                        String room = roomField.getText();
                        if (room.isEmpty()) {
                            throw new Exception("Introduceti sala de lectura");
                        }
                        updatedBook = new ReadingRoomDecorator(selectedBook, room);
                        break;
                    case "Acces restrictionat":
                        MemberType level = (MemberType) accessLevelCombo.getSelectedItem();
                        updatedBook = new RestrictedAccessDecorator(selectedBook, level);
                        break;
                    case "Necesita aprobare":
                        updatedBook = new ApprovalRequiredDecorator(selectedBook);
                        if (currentLibrarian != null) {
                            ((ApprovalRequiredDecorator) updatedBook).approve(currentLibrarian.getUserName());
                        }
                        break;
                }

                if (updatedBook != null) {
                    bookService.updateBook(selectedBook.getItemId(), updatedBook);
                    ((BooksTableModel) table.getModel()).refresh();
                    dialog.dispose();
                    JOptionPane.showMessageDialog(this, "Restrictii actualizate cu succes!");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Eroare: " + ex.getMessage(), "Eroare", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void showAddBookDialog(JTable table) {
        JDialog dialog = new JDialog(this, "Adauga Carte", true);
        dialog.setSize(550, 750);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(PRIMARY_PINK);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(PRIMARY_PINK);
        GridBagConstraints formGbc = new GridBagConstraints();
        formGbc.insets = new Insets(5, 10, 5, 10);
        formGbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        ValidatedField titleField = new ValidatedField(formPanel, formGbc, row,
                "Titlu:", validationFactory.buildTitleChain(), "Titlu", FormatInputValidator.FormatType.TITLE);
        row += 2;

        ValidatedField yearField = new ValidatedField(formPanel, formGbc, row,
                "An:", validationFactory.buildYearChain(), "An", FormatInputValidator.FormatType.YEAR);
        row += 2;

        JLabel pagesLabel = new JLabel("Pagini:");
        pagesLabel.setForeground(Color.BLACK);
        pagesLabel.setFont(new Font("Garamond", Font.PLAIN, 12));
        formGbc.gridx = 0;
        formGbc.gridy = row;
        formPanel.add(pagesLabel, formGbc);

        JTextField pagesField = new JTextField(20);
        styleTextField(pagesField);
        formGbc.gridx = 1;
        formPanel.add(pagesField, formGbc);
        row++;

        JLabel pagesError = new JLabel(" ");
        pagesError.setForeground(Color.RED);
        pagesError.setFont(new Font("Garamond", Font.ITALIC, 11));
        formGbc.gridx = 1;
        formGbc.gridy = row;
        formPanel.add(pagesError, formGbc);
        row++;

        ValidatedField authorField = new ValidatedField(formPanel, formGbc, row,
                "Autor:", validationFactory.buildNameChain(), "Autor", FormatInputValidator.FormatType.NAME);
        row += 2;

        ValidatedField isbnField = new ValidatedField(formPanel, formGbc, row,
                "ISBN:", validationFactory.buildIsbnChain(), "ISBN", FormatInputValidator.FormatType.ISBN);
        isbnField.setIsbnFormatting();
        row += 2;

        JLabel publisherLabel = new JLabel("Editura:");
        publisherLabel.setForeground(Color.BLACK);
        publisherLabel.setFont(new Font("Garamond", Font.PLAIN, 12));
        formGbc.gridx = 0;
        formGbc.gridy = row;
        formPanel.add(publisherLabel, formGbc);

        JTextField publisherField = new JTextField(20);
        styleTextField(publisherField);
        formGbc.gridx = 1;
        formPanel.add(publisherField, formGbc);
        row++;

        JLabel publisherError = new JLabel(" ");
        publisherError.setForeground(Color.RED);
        publisherError.setFont(new Font("Garamond", Font.ITALIC, 11));
        formGbc.gridx = 1;
        formGbc.gridy = row;
        formPanel.add(publisherError, formGbc);
        row++;

        JLabel priceLabel = new JLabel("Pret (lei):");
        priceLabel.setForeground(Color.BLACK);
        priceLabel.setFont(new Font("Garamond", Font.PLAIN, 12));
        formGbc.gridx = 0;
        formGbc.gridy = row;
        formPanel.add(priceLabel, formGbc);

        JTextField priceField = new JTextField(20);
        styleTextField(priceField);
        formGbc.gridx = 1;
        formPanel.add(priceField, formGbc);
        row++;

        JLabel priceError = new JLabel(" ");
        priceError.setForeground(Color.RED);
        priceError.setFont(new Font("Garamond", Font.ITALIC, 11));
        formGbc.gridx = 1;
        formGbc.gridy = row;
        formPanel.add(priceError, formGbc);
        row++;

        JLabel typeLabel = new JLabel("Tip:");
        typeLabel.setForeground(Color.BLACK);
        typeLabel.setFont(new Font("Garamond", Font.PLAIN, 12));
        formGbc.gridx = 0;
        formGbc.gridy = row;
        formPanel.add(typeLabel, formGbc);

        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Fantasy", "Romance"});
        styleComboBox(typeCombo);
        formGbc.gridx = 1;
        formPanel.add(typeCombo, formGbc);
        row++;

        JLabel levelLabel = new JLabel("Nivel romance (1-5):");
        levelLabel.setForeground(Color.BLACK);
        levelLabel.setFont(new Font("Garamond", Font.PLAIN, 12));
        formGbc.gridx = 0;
        formGbc.gridy = row;
        formPanel.add(levelLabel, formGbc);

        JTextField levelField = new JTextField(20);
        styleTextField(levelField);
        formGbc.gridx = 1;
        formPanel.add(levelField, formGbc);
        row++;

        JLabel levelError = new JLabel(" ");
        levelError.setForeground(Color.RED);
        levelError.setFont(new Font("Garamond", Font.ITALIC, 11));
        formGbc.gridx = 1;
        formGbc.gridy = row;
        formPanel.add(levelError, formGbc);
        row++;

        JLabel tropesLabel = new JLabel("Tropi:");
        tropesLabel.setForeground(Color.BLACK);
        tropesLabel.setFont(new Font("Garamond", Font.PLAIN, 12));
        formGbc.gridx = 0;
        formGbc.gridy = row;
        formPanel.add(tropesLabel, formGbc);

        JTextField tropesField = new JTextField(20);
        styleTextField(tropesField);
        formGbc.gridx = 1;
        formPanel.add(tropesField, formGbc);
        row++;

        levelLabel.setVisible(false);
        levelField.setVisible(false);
        levelError.setVisible(false);
        tropesLabel.setVisible(false);
        tropesField.setVisible(false);

        typeCombo.addActionListener(e -> {
            String selected = (String) typeCombo.getSelectedItem();
            boolean isRomance = "Romance".equals(selected);
            levelLabel.setVisible(isRomance);
            levelField.setVisible(isRomance);
            levelError.setVisible(isRomance);
            tropesLabel.setVisible(isRomance);
            tropesField.setVisible(isRomance);
            dialog.pack();
        });

        JLabel stockLabel = new JLabel("Stoc initial:");
        stockLabel.setForeground(Color.BLACK);
        stockLabel.setFont(new Font("Garamond", Font.PLAIN, 12));
        formGbc.gridx = 0;
        formGbc.gridy = row;
        formPanel.add(stockLabel, formGbc);

        JTextField stockField = new JTextField(20);
        stockField.setText("1");
        styleTextField(stockField);
        formGbc.gridx = 1;
        formPanel.add(stockField, formGbc);
        row++;

        JLabel stockError = new JLabel(" ");
        stockError.setForeground(Color.RED);
        stockError.setFont(new Font("Garamond", Font.ITALIC, 11));
        formGbc.gridx = 1;
        formGbc.gridy = row;
        formPanel.add(stockError, formGbc);
        row++;

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(PRIMARY_PINK);
        JButton saveBtn = createStyledButton("Salveaza", DARK_PINK, Color.BLACK);
        JButton cancelBtn = createStyledButton("Anuleaza", DARK_PINK, Color.BLACK);
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(formPanel, gbc);
        gbc.gridy = 1;
        dialog.add(buttonPanel, gbc);

        saveBtn.addActionListener(e -> {
            boolean titleValid = titleField.validate();
            boolean yearValid = yearField.validate();
            boolean authorValid = authorField.validate();
            boolean isbnValid = isbnField.validate();

            boolean pagesValid = true;
            boolean publisherValid = true;
            boolean priceValid = true;
            boolean levelValid = true;
            boolean stockValid = true;
            boolean isbnUnique = true;

            int pages = 0;
            try {
                pages = Integer.parseInt(pagesField.getText());
                if (pages <= 0) {
                    pagesError.setText("Pagini trebuie sa fie un numar pozitiv");
                    pagesValid = false;
                } else {
                    pagesError.setText(" ");
                }
            } catch (NumberFormatException ex) {
                pagesError.setText("Pagini trebuie sa fie un numar valid");
                pagesValid = false;
            }

            if (publisherField.getText().trim().isEmpty()) {
                publisherError.setText("Editura nu poate fi goala");
                publisherValid = false;
            } else {
                publisherError.setText(" ");
            }

            double price = 0;
            try {
                price = Double.parseDouble(priceField.getText());
                if (price <= 0) {
                    priceError.setText("Pretul trebuie sa fie un numar pozitiv");
                    priceValid = false;
                } else {
                    priceError.setText(" ");
                }
            } catch (NumberFormatException ex) {
                priceError.setText("Pretul trebuie sa fie un numar valid");
                priceValid = false;
            }

            int stockQuantity = 0;
            try {
                stockQuantity = Integer.parseInt(stockField.getText());
                if (stockQuantity <= 0) {
                    stockError.setText("Stocul trebuie sa fie un numar pozitiv");
                    stockValid = false;
                } else {
                    stockError.setText(" ");
                }
            } catch (NumberFormatException ex) {
                stockError.setText("Stocul trebuie sa fie un numar valid");
                stockValid = false;
            }

            String isbn = isbnField.getRawValue();
            List<Book> existingBooks = bookService.findAllBooksLegacy();
            for (Book b : existingBooks) {
                if (b.getIsbn().equals(isbn)) {
                    isbnUnique = false;
                    JOptionPane.showMessageDialog(dialog,
                            "O carte cu acest ISBN exista deja!\nISBN: " + isbn + "\nFolositi un ISBN diferit.",
                            "ISBN duplicat",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            String type = (String) typeCombo.getSelectedItem();
            int level = 0;
            String tropes = "";

            if ("Romance".equals(type)) {
                try {
                    level = Integer.parseInt(levelField.getText());
                    if (level < 1 || level > 5) {
                        levelError.setText("Nivelul trebuie sa fie intre 1 si 5");
                        levelValid = false;
                    } else {
                        levelError.setText(" ");
                    }
                    tropes = tropesField.getText();
                } catch (NumberFormatException ex) {
                    levelError.setText("Nivelul trebuie sa fie un numar valid");
                    levelValid = false;
                }
            }

            if (titleValid && yearValid && authorValid && isbnValid && pagesValid && publisherValid && priceValid && levelValid && stockValid && isbnUnique) {
                try {
                    String title = titleField.getValue();
                    int year = Integer.parseInt(yearField.getValue());
                    String authorName = authorField.getValue();
                    String publisher = publisherField.getText();

                    Author author = new Author((int)(System.currentTimeMillis() % 10000), authorName);

                    if ("Fantasy".equals(type)) {
                        FantasyBook book = librarianService.addFantasyBook(currentToken, (int)(System.currentTimeMillis() % 10000),
                                title, year, pages, author, isbn, publisher, price);
                        stockService.addStock(book.getIsbn(), stockQuantity);
                    } else {
                        RomanceBook book = librarianService.addRomanceBook(currentToken, (int)(System.currentTimeMillis() % 10000),
                                title, year, pages, author, isbn, publisher, level, tropes, price);
                        stockService.addStock(book.getIsbn(), stockQuantity);
                    }

                    ((BooksTableModel) table.getModel()).refresh();
                    dialog.dispose();
                    JOptionPane.showMessageDialog(this, "Carte adaugata cu succes!\nStoc initial: " + stockQuantity + " bucati");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Eroare: " + ex.getMessage(), "Eroare", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void showAddNewspaperDialog(JTable table) {
        JDialog dialog = new JDialog(this, "Adauga Ziar", true);
        dialog.setSize(550, 600);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(PRIMARY_PINK);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField titleField = new JTextField(20);
        JTextField yearField = new JTextField(20);
        JTextField pageCountField = new JTextField(20);
        JTextField publisherField = new JTextField(20);
        JTextField issnField = new JTextField(20);
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Local", "National"});

        JTextField cityField = new JTextField(20);
        JTextField regionField = new JTextField(20);
        JTextField distributionAreaField = new JTextField(20);
        JTextField politicalOrientationField = new JTextField(20);

        styleTextField(titleField);
        styleTextField(yearField);
        styleTextField(pageCountField);
        styleTextField(publisherField);
        styleTextField(issnField);
        styleTextField(cityField);
        styleTextField(regionField);
        styleTextField(distributionAreaField);
        styleTextField(politicalOrientationField);

        int row = 0;
        addFormRow(dialog, "Titlu:", titleField, gbc, row++);
        addFormRow(dialog, "An publicare:", yearField, gbc, row++);
        addFormRow(dialog, "Numar pagini:", pageCountField, gbc, row++);
        addFormRow(dialog, "Editura:", publisherField, gbc, row++);
        addFormRow(dialog, "ISSN:", issnField, gbc, row++);
        addFormRow(dialog, "Tip:", typeCombo, gbc, row++);

        JLabel cityLabel = new JLabel("Oras (pentru Local):");
        cityLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(cityLabel, gbc);
        gbc.gridx = 1;
        dialog.add(cityField, gbc);
        row++;

        JLabel regionLabel = new JLabel("Regiune (pentru Local):");
        regionLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(regionLabel, gbc);
        gbc.gridx = 1;
        dialog.add(regionField, gbc);
        row++;

        JLabel areaLabel = new JLabel("Arie distributie (pentru National):");
        areaLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(areaLabel, gbc);
        gbc.gridx = 1;
        dialog.add(distributionAreaField, gbc);
        row++;

        JLabel orientationLabel = new JLabel("Orientare politica (pentru National):");
        orientationLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(orientationLabel, gbc);
        gbc.gridx = 1;
        dialog.add(politicalOrientationField, gbc);
        row++;

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(PRIMARY_PINK);
        JButton saveBtn = createStyledButton("Salveaza", DARK_PINK, Color.BLACK);
        JButton cancelBtn = createStyledButton("Anuleaza", DARK_PINK, Color.BLACK);
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        saveBtn.addActionListener(e -> {
            try {
                String title = titleField.getText();
                if (validateTitle(title) == null) return;

                String yearStr = yearField.getText();
                if (validateYear(yearStr) == null) return;
                int publicationDate = Integer.parseInt(yearStr);

                int pageCount = Integer.parseInt(pageCountField.getText());
                String publisherName = publisherField.getText();

                if (publisherName.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Editura este obligatorie!", "Eroare", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String issn = issnField.getText();
                if (validateIssn(issn) == null) return;

                String type = (String) typeCombo.getSelectedItem();

                PublisherFactory publisherFactory = PublisherFactory.getInstance();
                Publisher publisher = publisherFactory.getPublisher(publisherName);

                Newspaper newspaper;
                if ("Local".equals(type)) {
                    String city = cityField.getText();
                    String region = regionField.getText();
                    if (city.isEmpty()) {
                        JOptionPane.showMessageDialog(dialog, "Orasul este obligatoriu pentru ziar local!", "Eroare", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    newspaper = new LocalNewspaper((int)(System.currentTimeMillis() % 10000),
                            title, publicationDate, pageCount, publisher, issn, city, region);
                } else {
                    String distributionArea = distributionAreaField.getText();
                    String politicalOrientation = politicalOrientationField.getText();
                    if (distributionArea.isEmpty()) {
                        JOptionPane.showMessageDialog(dialog, "Aria de distributie este obligatorie pentru ziar national!", "Eroare", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    newspaper = new NationalNewspaper((int)(System.currentTimeMillis() % 10000),
                            title, publicationDate, pageCount, publisher, issn, distributionArea, politicalOrientation);
                }

                newspaperService.addItem(newspaper);
                ((NewspapersTableModel) table.getModel()).refresh();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Ziar adaugat cu succes!");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Eroare: Te rugam sa introduci valori numerice valide pentru an si pagini", "Eroare", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Eroare: " + ex.getMessage(), "Eroare", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void showDeleteBookDialog(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Selectati o carte din tabel pentru a o sterge!",
                    "Eroare",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int bookId = (int) table.getValueAt(selectedRow, 0);
        String bookTitle = (String) table.getValueAt(selectedRow, 1);
        String bookIsbn = (String) table.getValueAt(selectedRow, 5);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Sunteti sigur ca doriti sa stergeti aceasta carte?\n\n" +
                        "ID: " + bookId + "\n" +
                        "Titlu: " + bookTitle + "\n" +
                        "ISBN: " + bookIsbn + "\n\n" +
                        "ATENTIE: Se va sterge si stocul asociat!",
                "Confirmare stergere",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean deleted = librarianService.deleteBook(currentToken, bookId);
            if (deleted) {
                ((BooksTableModel) table.getModel()).refresh();
                JOptionPane.showMessageDialog(this,
                        "Cartea \"" + bookTitle + "\" a fost stearsa cu succes!",
                        "Succes",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Eroare la stergerea cartii!",
                        "Eroare",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAddMemberDialog(JTable table) {
        JDialog dialog = new JDialog(this, "Adauga Membru", true);
        dialog.setSize(500, 350);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(PRIMARY_PINK);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(PRIMARY_PINK);
        GridBagConstraints formGbc = new GridBagConstraints();
        formGbc.insets = new Insets(5, 10, 5, 10);
        formGbc.fill = GridBagConstraints.HORIZONTAL;

        ValidatedField nameField = new ValidatedField(formPanel, formGbc, 0,
                "Nume:", validationFactory.buildNameChain(), "Nume", FormatInputValidator.FormatType.NAME);

        ValidatedField emailField = new ValidatedField(formPanel, formGbc, 2,
                "Email:", validationFactory.buildEmailChain(), "Email", FormatInputValidator.FormatType.EMAIL);

        JLabel typeLabel = new JLabel("Tip membru:");
        typeLabel.setForeground(Color.BLACK);
        typeLabel.setFont(new Font("Garamond", Font.PLAIN, 12));
        formGbc.gridx = 0;
        formGbc.gridy = 4;
        formPanel.add(typeLabel, formGbc);

        JComboBox<MemberType> typeCombo = new JComboBox<>(MemberType.values());
        styleComboBox(typeCombo);
        formGbc.gridx = 1;
        formPanel.add(typeCombo, formGbc);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(PRIMARY_PINK);
        JButton saveBtn = createStyledButton("Salveaza", DARK_PINK, Color.BLACK);
        JButton cancelBtn = createStyledButton("Anuleaza", DARK_PINK, Color.BLACK);
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(formPanel, gbc);
        gbc.gridy = 1;
        dialog.add(buttonPanel, gbc);

        saveBtn.addActionListener(e -> {
            boolean nameValid = nameField.validate();
            boolean emailValid = emailField.validate();

            if (nameValid && emailValid) {
                try {
                    String name = nameField.getValue();
                    String email = emailField.getValue();
                    MemberType type = (MemberType) typeCombo.getSelectedItem();

                    Member member = memberService.addMember(name, email, type);
                    ((MembersTableModel) table.getModel()).refresh();
                    dialog.dispose();
                    JOptionPane.showMessageDialog(this, "Membru adaugat cu succes!\nNumar membership: " + member.getMembershipNumber());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Eroare: " + ex.getMessage(), "Eroare", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(Color.BLACK);
        comboBox.setFont(new Font("Garamond", Font.PLAIN, 12));
    }

    private void showCreateLoanDialog(JTable table) {
        JDialog dialog = new JDialog(this, "Creaza Imprumut", true);
        dialog.setSize(550, 400);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(PRIMARY_PINK);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        List<Member> members = memberService.getAllMembers();
        List<IBorrowable> books = bookService.findAllBooks();

        if (members.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nu exista membri in sistem! Adauga mai intai un membru.", "Eroare", JOptionPane.WARNING_MESSAGE);
            dialog.dispose();
            return;
        }

        if (books.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nu exista carti in sistem! Adauga mai intai o carte.", "Eroare", JOptionPane.WARNING_MESSAGE);
            dialog.dispose();
            return;
        }

        JComboBox<Member> memberCombo = new JComboBox<>(members.toArray(new Member[0]));
        JComboBox<IBorrowable> bookCombo = new JComboBox<>(books.toArray(new IBorrowable[0]));

        memberCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof Member) {
                    Member m = (Member) value;
                    setText(m.getUserName() + " (" + m.getMemberType() + ") - " +
                            m.getCurrentLoans() + "/" + m.getMaxBooks() + " imprumuturi | " +
                            "Membership: " + m.getMembershipNumber());
                } else {
                    setText(value != null ? value.toString() : "");
                }
                if (isSelected) {
                    setBackground(list.getSelectionBackground());
                    setForeground(list.getSelectionForeground());
                } else {
                    setBackground(list.getBackground());
                    setForeground(list.getForeground());
                }
                return this;
            }
        });

        bookCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof IBorrowable) {
                    Book book = getBookFromItem((IBorrowable) value);
                    if (book != null) {
                        int stock = stockService.getStock(book.getIsbn()) != null ?
                                stockService.getStock(book.getIsbn()).getAvailableQuantity() : 0;

                        String type = "";
                        String restrictions = "";

                        if (book instanceof FantasyBook) {
                            type = "[Fantasy]";
                        } else if (book instanceof RomanceBook) {
                            type = "[Romance]";
                        }

                        if (value instanceof ReadingRoomDecorator) {
                            restrictions = " [DOAR IN SALA - NU SE POATE IMPRUMUTA]";
                        } else if (value instanceof RestrictedAccessDecorator) {
                            restrictions = " [Acces restrictionat]";
                        } else if (value instanceof ApprovalRequiredDecorator) {
                            restrictions = " [Necesita aprobare]";
                        }

                        setText(String.format("%s %s - %s | Stoc: %d | Pret: %.2f lei%s",
                                type, book.getTitle(), book.getAuthor().getName(), stock, book.getPrice(), restrictions));
                    } else {
                        setText(value.toString());
                    }
                } else {
                    setText(value != null ? value.toString() : "");
                }
                if (isSelected) {
                    setBackground(list.getSelectionBackground());
                    setForeground(list.getSelectionForeground());
                } else {
                    setBackground(list.getBackground());
                    setForeground(list.getForeground());
                }
                return this;
            }
        });

        int row = 0;
        addFormRow(dialog, "Selecteaza membru:", memberCombo, gbc, row++);
        addFormRow(dialog, "Selecteaza carte:", bookCombo, gbc, row++);

        JPanel infoPanel = new JPanel(new GridLayout(3, 1));
        infoPanel.setBackground(PRIMARY_PINK);
        JLabel infoLabel1 = new JLabel("Nota: Membrii au limita de imprumuturi in functie de tipul lor.");
        JLabel infoLabel2 = new JLabel("Cartile cu restrictii pot necesita aprobare speciala.");
        JLabel infoLabel3 = new JLabel("Cartile cu 'DOAR IN SALA' NU pot fi imprumutate acasa.");
        infoLabel1.setForeground(Color.BLACK);
        infoLabel2.setForeground(Color.BLACK);
        infoLabel3.setForeground(Color.RED);
        infoLabel1.setFont(new Font("Garamond", Font.ITALIC, 11));
        infoLabel2.setFont(new Font("Garamond", Font.ITALIC, 11));
        infoLabel3.setFont(new Font("Garamond", Font.BOLD, 11));
        infoPanel.add(infoLabel1);
        infoPanel.add(infoLabel2);
        infoPanel.add(infoLabel3);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        dialog.add(infoPanel, gbc);
        row++;

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(PRIMARY_PINK);
        JButton saveBtn = createStyledButton("Creaza Imprumut", DARK_PINK, Color.BLACK);
        JButton cancelBtn = createStyledButton("Anuleaza", DARK_PINK, Color.BLACK);
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        saveBtn.addActionListener(e -> {
            try {
                Member member = (Member) memberCombo.getSelectedItem();
                IBorrowable book = (IBorrowable) bookCombo.getSelectedItem();

                if (member == null || book == null) {
                    throw new Exception("Selecteaza membru si carte");
                }

                if (book instanceof ReadingRoomDecorator) {
                    JOptionPane.showMessageDialog(dialog,
                            "Aceasta carte poate fi citita DOAR in sala de lectura!\n\n" +
                                    "Nu se poate crea un imprumut pentru a fi luata acasa.\n" +
                                    "Va rugam sa folositi cartea in biblioteca.",
                            "Imprumut nepermis",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (book instanceof RestrictedAccessDecorator) {
                    RestrictedAccessDecorator ra = (RestrictedAccessDecorator) book;
                    if (ra.getRequiredLevel() != member.getMemberType()) {
                        JOptionPane.showMessageDialog(dialog,
                                "Nu aveti acces la aceasta carte!\nNivel necesar: " + ra.getRequiredLevel() +
                                        "\nNivelul dvs: " + member.getMemberType(),
                                "Acces restrictionat",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                if (book instanceof ApprovalRequiredDecorator) {
                    ApprovalRequiredDecorator ar = (ApprovalRequiredDecorator) book;
                    if (!ar.isApproved()) {
                        JOptionPane.showMessageDialog(dialog,
                                "Aceasta carte necesita aprobare speciala!\nContactati un bibliotecar.",
                                "Aprobare necesara",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }

                if (!member.canBorrow()) {
                    throw new Exception("Membrul a atins limita maxima de imprumuturi (" + member.getMaxBooks() + ")");
                }

                Book bookObj = getBookFromItem(book);
                if (bookObj != null) {
                    int stock = stockService.getStock(bookObj.getIsbn()) != null ?
                            stockService.getStock(bookObj.getIsbn()).getAvailableQuantity() : 0;
                    if (stock <= 0) {
                        throw new Exception("Cartea nu este disponibila (stoc epuizat)");
                    }
                }

                Loan loan = loanService.createLoan(member, book);
                if (loan != null) {
                    memberService.incrementLoans(member.getUserId());
                    if (bookObj != null) {
                        stockService.decreaseStock(bookObj.getIsbn(), 1);
                    }
                    ((ActiveLoansTableModel) table.getModel()).refresh();
                    dialog.dispose();
                    JOptionPane.showMessageDialog(this,
                            String.format("Imprumut creat cu succes!\n\nMembru: %s\nCarte: %s\nData returnare: %s",
                                    member.getUserName(),
                                    bookObj != null ? bookObj.getTitle() : "N/A",
                                    loan.getReturnDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))),
                            "Succes",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Nu s-a putut crea imprumutul!\nVerifica daca membrul are deja imprumuturi active sau daca cartea are restrictii.",
                            "Eroare",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Eroare: " + ex.getMessage(), "Eroare", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void returnLoan(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Selecteaza un imprumut pentru returnare", "Eroare", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer loanId = (Integer) table.getValueAt(selectedRow, 0);
        Loan loan = loanService.getLoanById(loanId);

        if (loan != null) {
            loanService.closeLoan(loan);
            ((ActiveLoansTableModel) table.getModel()).refresh();
            JOptionPane.showMessageDialog(this, "Imprumut returnat cu succes!");
        } else {
            JOptionPane.showMessageDialog(this, "Imprumutul nu a fost gasit!", "Eroare", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void renewLoan(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Selecteaza un imprumut pentru prelungire", "Eroare", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int loanId = (int) table.getValueAt(selectedRow, 0);
        Loan loan = loanService.getLoanById(loanId);

        if (loan != null) {
            String[] options = {"7 zile", "14 zile", "30 zile"};
            int choice = JOptionPane.showOptionDialog(this,
                    "Cu cate zile doriti sa prelungiti?",
                    "Prelungire Imprumut",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);

            int extraDays = 0;
            switch (choice) {
                case 0 -> extraDays = 7;
                case 1 -> extraDays = 14;
                case 2 -> extraDays = 30;
                default -> { return; }
            }

            loanService.renewLoan(loan, extraDays);
            ((ActiveLoansTableModel) table.getModel()).refresh();
            JOptionPane.showMessageDialog(this, "Imprumut prelungit cu succes! +" + extraDays + " zile");
        } else {
            JOptionPane.showMessageDialog(this, "Imprumutul nu a fost gasit!", "Eroare", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showCreateGiftDialog(String type, JTable table) {
        JDialog dialog = new JDialog(this, "Creeaza Pachet " + (type.equals("premium") ? "Premium" : "Standard"), true);
        dialog.setSize(450, 300);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(PRIMARY_PINK);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        List<IBorrowable> books = bookService.findAllBooks();
        JComboBox<IBorrowable> bookCombo = new JComboBox<>(books.toArray(new IBorrowable[0]));

        bookCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof IBorrowable) {
                    Book book = getBookFromItem((IBorrowable) value);
                    if (book != null) {
                        String type = "";
                        if (book instanceof FantasyBook) {
                            type = "[Fantasy] ";
                        } else if (book instanceof RomanceBook) {
                            type = "[Romance] ";
                        }
                        setText(type + book.getTitle() + " - " + book.getAuthor().getName() +
                                " | " + String.format("%.2f", book.getPrice()) + " lei");
                    } else {
                        setText(value.toString());
                    }
                } else {
                    setText(value != null ? value.toString() : "");
                }

                if (isSelected) {
                    setBackground(list.getSelectionBackground());
                    setForeground(list.getSelectionForeground());
                } else {
                    setBackground(list.getBackground());
                    setForeground(list.getForeground());
                }
                return this;
            }
        });

        int row = 0;
        addFormRow(dialog, "Alege cartea:", bookCombo, gbc, row++);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(PRIMARY_PINK);
        JButton saveBtn = createStyledButton("Creeaza", DARK_PINK, Color.BLACK);
        JButton cancelBtn = createStyledButton("Anuleaza", DARK_PINK, Color.BLACK);
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        saveBtn.addActionListener(e -> {
            try {
                IBorrowable item = (IBorrowable) bookCombo.getSelectedItem();
                Book book = getBookFromItem(item);

                if (book == null) {
                    throw new Exception("Selecteaza o carte valida");
                }

                GiftPackage gift = giftPackageService.createGiftPackage(type, book, "Cadou special");

                ((GiftsTableModel) table.getModel()).refresh();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Pachet creat cu succes! Pret total: " + String.format("%.2f", gift.calculateTotalPrice()) + " lei");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Eroare: " + ex.getMessage(), "Eroare", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void refreshEventsTable(JTable table) {
        ((EventsTableModel) table.getModel()).refresh();
    }

    private void refreshEventTree(JTree tree) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Toate Evenimentele");

        List<SingleEvent> singleEvents = eventService.getAllSingleEvents();
        List<EventGroup> groups = eventService.getAllGroups();

        List<SingleEvent> eventsNotInGroups = new ArrayList<>(singleEvents);

        for (EventGroup group : groups) {
            DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(
                    group.getName() + " (" + group.getEvents().size() + " evenimente)"
            );

            for (EventComponent child : group.getEvents()) {
                if (child instanceof SingleEvent) {
                    SingleEvent event = (SingleEvent) child;
                    eventsNotInGroups.remove(event);

                    String spotsInfo;
                    if (event.getMaxParticipants() == -1) {
                        spotsInfo = "Locuri: nelimitate";
                    } else {
                        spotsInfo = "Locuri: " + event.getRegisteredParticipants() + "/" + event.getMaxParticipants();
                    }
                    groupNode.add(new DefaultMutableTreeNode(
                            "  " + event.getName() + " (" + event.getDate() + " - " +
                                    event.getLocation() + ") [" + spotsInfo + "]"
                    ));
                } else if (child instanceof EventGroup) {
                    addEventsToNode(groupNode, (EventGroup) child);
                }
            }
            root.add(groupNode);
        }

        if (!eventsNotInGroups.isEmpty()) {
            DefaultMutableTreeNode ungroupedNode = new DefaultMutableTreeNode("Evenimente fara grup");
            for (SingleEvent event : eventsNotInGroups) {
                String spotsInfo;
                if (event.getMaxParticipants() == -1) {
                    spotsInfo = "Locuri: nelimitate";
                } else {
                    spotsInfo = "Locuri: " + event.getRegisteredParticipants() + "/" + event.getMaxParticipants();
                }
                ungroupedNode.add(new DefaultMutableTreeNode(
                        event.getName() + " (" + event.getDate() + " - " +
                                event.getLocation() + ") [" + spotsInfo + "]"
                ));
            }
            root.add(ungroupedNode);
        }

        DefaultTreeModel model = new DefaultTreeModel(root);
        tree.setModel(model);
        ((DefaultTreeModel) tree.getModel()).reload();
    }

    private void addEventsToNode(DefaultMutableTreeNode node, EventGroup group) {
        DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(group.getName() + " (" + group.getEvents().size() + " evenimente)");
        for (EventComponent child : group.getEvents()) {
            if (child instanceof SingleEvent) {
                SingleEvent event = (SingleEvent) child;
                String spotsInfo;
                if (event.getMaxParticipants() == -1) {
                    spotsInfo = "Locuri: nelimitate";
                } else {
                    spotsInfo = "Locuri: " + event.getRegisteredParticipants() + "/" + event.getMaxParticipants();
                }
                groupNode.add(new DefaultMutableTreeNode(
                        "  " + event.getName() + " (" + event.getDate() + " - " +
                                event.getLocation() + ") [" + spotsInfo + "]"
                ));
            } else if (child instanceof EventGroup) {
                addEventsToNode(groupNode, (EventGroup) child);
            }
        }
        node.add(groupNode);
    }

    private void refreshStatsPanel(JTextArea statsArea) {
        StringBuilder stats = new StringBuilder();
        stats.append("=== STATISTICI BIBLIOTECA ===\n\n");
        stats.append("Carti totale: ").append(bookService.findAllBooks().size()).append("\n");
        stats.append("Ziare totale: ").append(newspaperService.getAllItems().size()).append("\n");
        stats.append("Membri totali: ").append(memberService.getAllMembers().size()).append("\n");
        stats.append("Imprumuturi active: ").append(loanService.getActiveLoans().size()).append("\n");
        stats.append("Pachete cadou create: ").append(giftPackageService.getCreatedPackages().size()).append("\n\n");

        stats.append("=== IMPRUMUTURI PER TIP MEMBRU ===\n");
        long standardCount = loanService.getActiveLoans().stream()
                .filter(l -> l.getUser().getMemberType().toString().equals("SIMPLE"))
                .count();
        long studentCount = loanService.getActiveLoans().stream()
                .filter(l -> l.getUser().getMemberType().toString().equals("STUDENT"))
                .count();
        long professorCount = loanService.getActiveLoans().stream()
                .filter(l -> l.getUser().getMemberType().toString().equals("PROFESSOR"))
                .count();

        stats.append("SIMPLE: ").append(standardCount).append("\n");
        stats.append("STUDENT: ").append(studentCount).append("\n");
        stats.append("PROFESSOR: ").append(professorCount).append("\n");

        statsArea.setText(stats.toString());
    }

    private void showAddEventDialog(JTable eventsTable, JTree tree) {
        JDialog dialog = new JDialog(this, "Adauga Eveniment", true);
        dialog.setSize(500, 500);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(PRIMARY_PINK);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(20);
        JTextField dateField = new JTextField(20);
        JTextField locationField = new JTextField(20);
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Lansare carte", "Atelier", "Conferinta", "Targ de carte"});
        JCheckBox unlimitedCheckBox = new JCheckBox("Locuri nelimitate");
        JTextField maxParticipantsField = new JTextField(20);

        styleTextField(nameField);
        styleTextField(dateField);
        styleTextField(locationField);
        styleTextField(maxParticipantsField);

        maxParticipantsField.setEnabled(false);
        maxParticipantsField.setText("0");
        unlimitedCheckBox.setSelected(true);

        unlimitedCheckBox.addActionListener(e -> {
            maxParticipantsField.setEnabled(!unlimitedCheckBox.isSelected());
            if (unlimitedCheckBox.isSelected()) {
                maxParticipantsField.setText("0");
                maxParticipantsField.setEnabled(false);
            } else {
                maxParticipantsField.setText("");
                maxParticipantsField.setEnabled(true);
            }
        });

        int row = 0;
        addFormRow(dialog, "Nume eveniment:", nameField, gbc, row++);
        addFormRow(dialog, "Data (dd.MM.yyyy):", dateField, gbc, row++);
        addFormRow(dialog, "Locatie:", locationField, gbc, row++);
        addFormRow(dialog, "Tip eveniment:", typeCombo, gbc, row++);

        JPanel capacityPanel = new JPanel(new BorderLayout());
        capacityPanel.setBackground(PRIMARY_PINK);
        capacityPanel.add(unlimitedCheckBox, BorderLayout.WEST);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        dialog.add(capacityPanel, gbc);
        row++;

        addFormRow(dialog, "Numar locuri (daca este cazul):", maxParticipantsField, gbc, row++);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(PRIMARY_PINK);
        JButton saveBtn = createStyledButton("Salveaza", DARK_PINK, Color.BLACK);
        JButton cancelBtn = createStyledButton("Anuleaza", DARK_PINK, Color.BLACK);
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        saveBtn.addActionListener(e -> {
            try {
                String name = nameField.getText();
                String date = dateField.getText();
                String location = locationField.getText();
                String type = (String) typeCombo.getSelectedItem();

                if (name.isEmpty()) throw new Exception("Numele evenimentului este obligatoriu");
                if (date.isEmpty()) throw new Exception("Data este obligatorie");
                if (location.isEmpty()) throw new Exception("Locatia este obligatorie");

                SingleEvent event;
                if (unlimitedCheckBox.isSelected()) {
                    event = eventService.createEvent(name, date, location, type);
                } else {
                    int maxParticipants = Integer.parseInt(maxParticipantsField.getText());
                    if (maxParticipants <= 0) throw new Exception("Numarul de locuri trebuie sa fie mai mare decat 0");
                    event = eventService.createEventWithCapacity(name, date, location, type, maxParticipants);
                }

                refreshEventsTable(eventsTable);
                refreshEventTree(tree);
                dialog.dispose();

                String message = "Eveniment adaugat cu succes!\n";
                if (event.getMaxParticipants() == -1) {
                    message += "Locuri: nelimitate";
                } else {
                    message += "Locuri disponibile: " + event.getAvailableSpots() + "/" + event.getMaxParticipants();
                }
                JOptionPane.showMessageDialog(this, message);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Eroare: Introduceti un numar valid pentru locuri", "Eroare", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Eroare: " + ex.getMessage(), "Eroare", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void showAddGroupDialog(JTree tree) {
        JDialog dialog = new JDialog(this, "Adauga Grup Evenimente", true);
        dialog.setSize(450, 250);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(PRIMARY_PINK);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(20);
        styleTextField(nameField);

        int row = 0;
        addFormRow(dialog, "Nume grup:", nameField, gbc, row++);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(PRIMARY_PINK);
        JButton saveBtn = createStyledButton("Salveaza", DARK_PINK, Color.BLACK);
        JButton cancelBtn = createStyledButton("Anuleaza", DARK_PINK, Color.BLACK);
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        saveBtn.addActionListener(e -> {
            try {
                String name = nameField.getText();
                if (name.isEmpty()) throw new Exception("Numele grupului este obligatoriu");

                eventService.createGroup(name);
                refreshEventTree(tree);
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Grup adaugat cu succes!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Eroare: " + ex.getMessage(), "Eroare", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void showAddEventToGroupDialog(JTable eventsTable, JTree tree) {
        List<SingleEvent> singleEvents = eventService.getAllSingleEvents();
        List<EventGroup> groups = eventService.getAllGroups();

        if (singleEvents.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nu exista evenimente simple!", "Eroare", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (groups.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nu exista grupuri! Creati mai intai un grup.", "Eroare", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<SingleEvent> availableEvents = new ArrayList<>();
        for (SingleEvent event : singleEvents) {
            boolean alreadyInGroup = false;
            for (EventGroup group : groups) {
                for (EventComponent ec : group.getEvents()) {
                    if (ec instanceof SingleEvent && ((SingleEvent) ec).getId() == event.getId()) {
                        alreadyInGroup = true;
                        break;
                    }
                }
            }
            if (!alreadyInGroup) {
                availableEvents.add(event);
            }
        }

        if (availableEvents.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Toate evenimentele sunt deja in grupuri!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Adauga Eveniment in Grup", true);
        dialog.setSize(500, 300);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(PRIMARY_PINK);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<SingleEvent> eventCombo = new JComboBox<>(availableEvents.toArray(new SingleEvent[0]));
        JComboBox<EventGroup> groupCombo = new JComboBox<>(groups.toArray(new EventGroup[0]));

        eventCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof SingleEvent) {
                    SingleEvent e = (SingleEvent) value;
                    String spotsInfo;
                    if (e.getMaxParticipants() == -1) {
                        spotsInfo = "Locuri: nelimitate";
                    } else {
                        spotsInfo = "Locuri: " + e.getRegisteredParticipants() + "/" + e.getMaxParticipants();
                    }
                    setText(e.getName() + " (" + e.getDate() + " - " + e.getLocation() + ") [" + spotsInfo + "]");
                } else {
                    setText(value != null ? value.toString() : "");
                }

                if (isSelected) {
                    setBackground(list.getSelectionBackground());
                    setForeground(list.getSelectionForeground());
                } else {
                    setBackground(list.getBackground());
                    setForeground(list.getForeground());
                }
                return this;
            }
        });

        groupCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof EventGroup) {
                    EventGroup g = (EventGroup) value;
                    setText(g.getName() + " (" + g.getEvents().size() + " evenimente)");
                } else {
                    setText(value != null ? value.toString() : "");
                }

                if (isSelected) {
                    setBackground(list.getSelectionBackground());
                    setForeground(list.getSelectionForeground());
                } else {
                    setBackground(list.getBackground());
                    setForeground(list.getForeground());
                }
                return this;
            }
        });

        int row = 0;
        addFormRow(dialog, "Selecteaza eveniment:", eventCombo, gbc, row++);
        addFormRow(dialog, "Selecteaza grup:", groupCombo, gbc, row++);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(PRIMARY_PINK);
        JButton saveBtn = createStyledButton("Adauga in Grup", DARK_PINK, Color.BLACK);
        JButton cancelBtn = createStyledButton("Anuleaza", DARK_PINK, Color.BLACK);
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        saveBtn.addActionListener(e -> {
            SingleEvent event = (SingleEvent) eventCombo.getSelectedItem();
            EventGroup group = (EventGroup) groupCombo.getSelectedItem();

            if (event != null && group != null) {
                group.add(event);
                refreshEventsTable(eventsTable);
                refreshEventTree(tree);
                dialog.dispose();
                JOptionPane.showMessageDialog(this,
                        "Eveniment adaugat in grupul " + group.getName() + " cu succes!",
                        "Succes",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void showRegisterToEventDialog(JTree tree) {
        List<SingleEvent> events = eventService.getAllSingleEvents();
        if (events.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nu exista evenimente in sistem!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<Member> members = memberService.getAllMembers();
        if (members.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nu exista membri in sistem!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Inregistrare la Eveniment", true);
        dialog.setSize(550, 350);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(PRIMARY_PINK);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<Member> memberCombo = new JComboBox<>(members.toArray(new Member[0]));
        JComboBox<SingleEvent> eventCombo = new JComboBox<>(events.toArray(new SingleEvent[0]));

        memberCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof Member) {
                    Member m = (Member) value;
                    setText(m.getUserName() + " (" + m.getMemberType() + ") - " +
                            m.getCurrentLoans() + "/" + m.getMaxBooks() + " imprumuturi");
                } else {
                    setText(value != null ? value.toString() : "");
                }

                if (isSelected) {
                    setBackground(list.getSelectionBackground());
                    setForeground(list.getSelectionForeground());
                } else {
                    setBackground(list.getBackground());
                    setForeground(list.getForeground());
                }
                return this;
            }
        });

        eventCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof SingleEvent) {
                    SingleEvent e = (SingleEvent) value;
                    String spotsInfo;
                    if (e.getMaxParticipants() == -1) {
                        spotsInfo = "Locuri nelimitate";
                    } else {
                        spotsInfo = "Locuri disponibile: " + e.getAvailableSpots() + "/" + e.getMaxParticipants();
                    }
                    setText(e.getName() + " (" + e.getDate() + " - " + e.getLocation() + ") - " + spotsInfo);
                } else {
                    setText(value != null ? value.toString() : "");
                }

                if (isSelected) {
                    setBackground(list.getSelectionBackground());
                    setForeground(list.getSelectionForeground());
                } else {
                    setBackground(list.getBackground());
                    setForeground(list.getForeground());
                }
                return this;
            }
        });

        int row = 0;
        addFormRow(dialog, "Selecteaza membru:", memberCombo, gbc, row++);
        addFormRow(dialog, "Selecteaza eveniment:", eventCombo, gbc, row++);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(PRIMARY_PINK);
        JButton registerBtn = createStyledButton("Inregistreaza", DARK_PINK, Color.BLACK);
        JButton cancelBtn = createStyledButton("Anuleaza", DARK_PINK, Color.BLACK);
        buttonPanel.add(registerBtn);
        buttonPanel.add(cancelBtn);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        registerBtn.addActionListener(e -> {
            Member member = (Member) memberCombo.getSelectedItem();
            SingleEvent event = (SingleEvent) eventCombo.getSelectedItem();

            if (member != null && event != null) {
                if (!event.hasAvailableSpots()) {
                    JOptionPane.showMessageDialog(dialog,
                            "Ne pare rau, evenimentul este complet!\nNu mai sunt locuri disponibile.",
                            "Eveniment plin",
                            JOptionPane.WARNING_MESSAGE);
                } else if (event.registerParticipant()) {
                    refreshEventTree(tree);
                    dialog.dispose();
                    JOptionPane.showMessageDialog(this,
                            "Inregistrare reusita!\n" + member.getUserName() + " a fost inregistrat la " + event.getName() +
                                    "\nLocuri ramase: " + event.getAvailableSpots(),
                            "Succes",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Inregistrare esuata!",
                            "Eroare",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
            dialog.dispose();
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void showProcessReturnDialog(JTextArea receiptArea) {
        JDialog dialog = new JDialog(this, "Procesare Returnare", true);
        dialog.setSize(600, 450);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(PRIMARY_PINK);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        List<Book> allBooks = bookService.findAllBooksLegacy();

        List<BookDisplay> bookDisplays = new ArrayList<>();
        for (Book book : allBooks) {
            bookDisplays.add(new BookDisplay(book));
        }

        SearchableComboBox<BookDisplay> bookCombo = new SearchableComboBox<>(bookDisplays);
        SearchableComboBox<MemberDisplay> memberCombo = new SearchableComboBox<>(new ArrayList<>());

        bookCombo.setPreferredSize(new Dimension(350, 60));
        memberCombo.setPreferredSize(new Dimension(350, 60));

        int row = 0;

        JLabel bookLabel = new JLabel("Selecteaza carte:");
        bookLabel.setForeground(Color.BLACK);
        bookLabel.setFont(new Font("Garamond", Font.PLAIN, 12));
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(bookLabel, gbc);
        gbc.gridx = 1;
        dialog.add(bookCombo, gbc);
        row++;

        JLabel memberLabel = new JLabel("Selecteaza membru:");
        memberLabel.setForeground(Color.BLACK);
        memberLabel.setFont(new Font("Garamond", Font.PLAIN, 12));
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(memberLabel, gbc);
        gbc.gridx = 1;
        dialog.add(memberCombo, gbc);
        row++;

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(PRIMARY_PINK);
        JButton processBtn = createStyledButton("Proceseaza Returnare", DARK_PINK, Color.BLACK);
        JButton cancelBtn = createStyledButton("Anuleaza", DARK_PINK, Color.BLACK);
        buttonPanel.add(processBtn);
        buttonPanel.add(cancelBtn);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        bookCombo.addActionListener(e -> {
            BookDisplay selectedBookDisplay = bookCombo.getSelectedItem();
            if (selectedBookDisplay != null) {
                Book selectedBook = selectedBookDisplay.getBook();
                if (selectedBook != null) {
                    List<Member> membersWhoBorrowed = new ArrayList<>();
                    for (Loan loan : loanService.getActiveLoans()) {
                        Book loanBook = loan.getBook();
                        if (loanBook != null && loanBook.getItemId() == selectedBook.getItemId()) {
                            Member member = loan.getUser();
                            if (member != null && !membersWhoBorrowed.contains(member)) {
                                membersWhoBorrowed.add(member);
                            }
                        }
                    }

                    memberCombo.setEnabled(true);
                    List<MemberDisplay> memberDisplays = new ArrayList<>();
                    for (Member member : membersWhoBorrowed) {
                        memberDisplays.add(new MemberDisplay(member));
                    }

                    memberCombo.setItems(memberDisplays);

                    if (membersWhoBorrowed.isEmpty()) {
                        JOptionPane.showMessageDialog(dialog,
                                "Aceasta carte nu este imprumutata de niciun membru!",
                                "Info", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });

        processBtn.addActionListener(e -> {
            BookDisplay selectedBook = bookCombo.getSelectedItem();
            MemberDisplay selectedMember = memberCombo.getSelectedItem();

            if (selectedBook == null || selectedMember == null) {
                JOptionPane.showMessageDialog(dialog, "Selectati o carte si un membru!", "Eroare", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Book book = selectedBook.getBook();
            Member member = selectedMember.getMember();

            if (book == null || member == null) {
                JOptionPane.showMessageDialog(dialog, "Date invalide!", "Eroare", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Loan activeLoan = findActiveLoan(book.getItemId(), member.getUserId());

            if (activeLoan == null) {
                JOptionPane.showMessageDialog(dialog,
                        "Nu exista un imprumut activ pentru aceasta carte si acest membru!",
                        "Eroare", JOptionPane.ERROR_MESSAGE);
                return;
            }

            LocalDate returnDate = LocalDate.now();
            int daysLate = 0;
            double penalty = 0;

            if (returnDate.isAfter(activeLoan.getReturnDate())) {
                daysLate = (int) java.time.temporal.ChronoUnit.DAYS.between(activeLoan.getReturnDate(), returnDate);
                penalty = daysLate * 1.0;
            }

            if (penalty > 0) {
                String[] options = {"Da, s-a platit", "Nu, nu s-a platit"};
                int result = JOptionPane.showOptionDialog(dialog,
                        "Penalitate: " + penalty + " lei\nS-a primit plata?",
                        "Confirmare plata",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null, options, options[0]);

                if (result == 1) {
                    JOptionPane.showMessageDialog(dialog,
                            "Returnarea nu poate fi finalizata fara plata penalitatii!",
                            "Eroare", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                paymentService.processPayment(penalty, "CASH",
                        "Penalitate intarziere - " + book.getTitle(), member.getUserId());
            }

            activeLoan.close();
            stockService.increaseStock(book.getIsbn(), 1);

            receiptMenu.printReturnReceipt(activeLoan, returnDate, daysLate, penalty, penalty > 0);

            if (loansTable != null) {
                ((ActiveLoansTableModel) loansTable.getModel()).refresh();
            }

            receiptArea.append("\n=== RETURNARE PROCESATA ===\n");
            receiptArea.append("Carte: " + book.getTitle() + "\n");
            receiptArea.append("Membru: " + member.getUserName() + "\n");
            receiptArea.append("Penalitate: " + penalty + " lei\n");
            receiptArea.append("Status: ACHITAT\n\n");

            JOptionPane.showMessageDialog(dialog, "Returnare procesata cu succes!\nChitanta a fost generata.");
            dialog.dispose();
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void showReceiptHistory(JTextArea receiptArea) {
        List<String> history = receiptMenu.getReceiptHistory();
        if (history.isEmpty()) {
            receiptArea.setText("Nu exista chitante in istoric.\n");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("=== ISTORIC CHITANTE ===\n\n");
            for (int i = 0; i < history.size(); i++) {
                sb.append("--- Chitanta #").append(i + 1).append(" ---\n");
                sb.append(history.get(i));
                sb.append("\n\n");
            }
            receiptArea.setText(sb.toString());
        }
    }

    private void styleTextField(JTextField field) {
        field.setBackground(Color.WHITE);
        field.setForeground(Color.BLACK);
        field.setFont(new Font("Garamond", Font.PLAIN, 12));
        field.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    }

    private void addFormRow(JDialog dialog, String label, JComponent field, GridBagConstraints gbc, int row) {
        JLabel lbl = new JLabel(label);
        lbl.setForeground(Color.BLACK);
        lbl.setFont(new Font("Garamond", Font.PLAIN, 12));
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(lbl, gbc);
        gbc.gridx = 1;
        dialog.add(field, gbc);
    }

    private void addFormRowToPanel(JPanel panel, String label, JComponent field, GridBagConstraints gbc, int row) {
        JLabel lbl = new JLabel(label);
        lbl.setForeground(Color.BLACK);
        lbl.setFont(new Font("Garamond", Font.PLAIN, 12));
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(lbl, gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    public class MemberDisplay {
        private Member member;

        public MemberDisplay(Member member) {
            this.member = member;
        }

        public Member getMember() { return member; }

        @Override
        public String toString() {
            return String.format("%s (ID: %d, Membership: %s, Tip: %s)",
                    member.getUserName(), member.getUserId(),
                    member.getMembershipNumber(), member.getMemberType());
        }
    }

    public class BookDisplay {
        private Book book;

        public BookDisplay(Book book) {
            this.book = book;
        }

        public Book getBook() { return book; }

        @Override
        public String toString() {
            return String.format("%s - %s (ID: %d, ISBN: %s)",
                    book.getTitle(), book.getAuthor().getName(),
                    book.getItemId(), book.getIsbn());
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new LibraryDashboard().setVisible(true));
    }
}