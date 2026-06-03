package com.example.tmppp_library_management.ui;

import com.example.tmppp_library_management.abstractClasses.LibraryItem;
import com.example.tmppp_library_management.builder.GiftPackage;
import com.example.tmppp_library_management.builder.GiftPackageService;
import com.example.tmppp_library_management.builder.PremiumGift;
import com.example.tmppp_library_management.book.Author;
import com.example.tmppp_library_management.book.Book;
import com.example.tmppp_library_management.book.FantasyBook;
import com.example.tmppp_library_management.book.RomanceBook;
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
import com.example.tmppp_library_management.entity.Stock;
import com.example.tmppp_library_management.factories.BookFactory;
import com.example.tmppp_library_management.factories.NewspaperFactory;
import com.example.tmppp_library_management.flyweight.Publisher;
import com.example.tmppp_library_management.flyweight.PublisherFactory;
import com.example.tmppp_library_management.interfaces.IBorrowable;
import com.example.tmppp_library_management.iterator.book.BookCollection;
import com.example.tmppp_library_management.iterator.book.BookIterator;
import com.example.tmppp_library_management.iterator.newspaper.NewspaperCollection;
import com.example.tmppp_library_management.iterator.newspaper.NewspaperIterator;
import com.example.tmppp_library_management.mediator.DashboardRefreshMediator;
import com.example.tmppp_library_management.memento.LoanCaretaker;
import com.example.tmppp_library_management.menus.InitializeData;
import com.example.tmppp_library_management.menus.ReceiptMenu;
import com.example.tmppp_library_management.menus.StatisticsMenu;
import com.example.tmppp_library_management.newspaper.LocalNewspaper;
import com.example.tmppp_library_management.newspaper.NationalNewspaper;
import com.example.tmppp_library_management.newspaper.Newspaper;
import com.example.tmppp_library_management.services.*;
import com.example.tmppp_library_management.singleton.LoanTemplateRegistry;
import com.example.tmppp_library_management.user.Librarian;
import com.example.tmppp_library_management.user.Member;
import com.example.tmppp_library_management.user.MemberType;
import com.example.tmppp_library_management.entity.AuditLogger;
import com.example.tmppp_library_management.visitor.StatisticsResult;
import com.example.tmppp_library_management.visitor.StatisticsVisitor;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
    private StatisticsMenu statisticsMenu;

    private ValidationChainFactory validationFactory;

    private String currentToken = null;
    private Librarian currentLibrarian = null;

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private String currentPanelName = "books";

    private CommandHistory commandHistory;
    private Map<String, Command> commands;

    private final Color CREAM_50 = new Color(253, 250, 244);
    private final Color CREAM_100 = new Color(245, 239, 224);
    private final Color CREAM_200 = new Color(234, 224, 203);
    private final Color CREAM_300 = new Color(210, 198, 175);
    private final Color FOREST_700 = new Color(45, 74, 53);
    private final Color FOREST_600 = new Color(58, 94, 68);
    private final Color FOREST_500 = new Color(74, 117, 88);
    private final Color FOREST_400 = new Color(106, 155, 120);
    private final Color FOREST_300 = new Color(140, 180, 150);
    private final Color FOREST_200 = new Color(181, 212, 188);
    private final Color AMBER = new Color(196, 137, 58);
    private final Color TEXT_DARK = new Color(30, 45, 34);
    private final Color TEXT_MUTED = new Color(100, 120, 100);

    private final Font GEORGIA_PLAIN = new Font("Georgia", Font.PLAIN, 12);
    private final Font GEORGIA_BOLD = new Font("Georgia", Font.BOLD, 12);
    private final Font GEORGIA_ITALIC = new Font("Georgia", Font.ITALIC, 11);
    private final Font GEORGIA_BIG = new Font("Georgia", Font.BOLD, 18);
    private final Font GEORGIA_TITLE = new Font("Georgia", Font.BOLD, 14);

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
        this.statisticsMenu = new StatisticsMenu(bookService, newspaperService, loanService, memberService);

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
        commands.put("audit", new ShowAuditCommand(this));
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
        int confirm = JOptionPane.showConfirmDialog(this,
                "Sunteti sigur ca doriti sa va delogati?",
                "Confirmare delogare",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            AuditLogger.getInstance().log("DELOGARE", "Utilizatorul '" + (currentLibrarian != null ? currentLibrarian.getUserName() : "Necunoscut") + "' s-a deconectat");
            librarianService.logout(currentToken);
            currentToken = null;
            dispose();
            new LibraryDashboard().setVisible(true);
        }
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
        loginDialog.getContentPane().setBackground(CREAM_50);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(CREAM_50);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Autentificare Librarian");
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 20));
        titleLabel.setForeground(FOREST_700);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(TEXT_DARK);
        userLabel.setFont(GEORGIA_PLAIN);
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(userLabel, gbc);

        JTextField usernameField = new JTextField(20);
        styleTextField(usernameField);
        gbc.gridx = 1;
        gbc.gridy = 1;
        mainPanel.add(usernameField, gbc);

        JLabel passLabel = new JLabel("Parola:");
        passLabel.setForeground(TEXT_DARK);
        passLabel.setFont(GEORGIA_PLAIN);
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(passLabel, gbc);

        JPasswordField passwordField = new JPasswordField(20);
        styleTextField(passwordField);
        gbc.gridx = 1;
        gbc.gridy = 2;
        mainPanel.add(passwordField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(CREAM_50);

        JButton loginBtn = createStyledButton("Login", FOREST_500, TEXT_DARK);
        JButton cancelBtn = createStyledButton("Cancel", FOREST_500, TEXT_DARK);

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
                AuditLogger.getInstance().setCurrentUser(username);
                AuditLogger.getInstance().log("AUTENTIFICARE", "Utilizatorul '" + username + "' s-a conectat cu succes");
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
        setTitle("Biblioteca MANAGEMENT SYSTEM");
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(CREAM_100);
        setLayout(new BorderLayout());

        JPanel sideMenu = createSideMenu();
        add(sideMenu, BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(CREAM_50);

        contentPanel.add(createBooksPanel(), "books");
        contentPanel.add(createNewspapersPanel(), "newspapers");
        contentPanel.add(createMembersPanel(), "members");
        contentPanel.add(createLoansPanel(), "loans");
        contentPanel.add(createGiftsPanel(), "gifts");
        contentPanel.add(createEventsPanel(), "events");
        contentPanel.add(createStatsPanel(), "stats");
        contentPanel.add(createReceiptsPanel(), "receipts");
        contentPanel.add(createAuditPanel(), "audit");

        add(contentPanel, BorderLayout.CENTER);
        add(createTopBar(), BorderLayout.NORTH);

        initRefreshMediator();
    }

    private JPanel createSideMenu() {
        JPanel sideMenu = new JPanel();
        sideMenu.setPreferredSize(new Dimension(240, 0));
        sideMenu.setBackground(FOREST_700);
        sideMenu.setLayout(new BoxLayout(sideMenu, BoxLayout.Y_AXIS));
        sideMenu.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        JLabel titleLabel = new JLabel("COLECTIE");
        titleLabel.setForeground(CREAM_300);
        titleLabel.setFont(GEORGIA_BOLD);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sideMenu.add(titleLabel);
        sideMenu.add(Box.createVerticalStrut(15));

        String[] menuItems = {"Carti", "Ziare", "OPERATIUNI", "Membri", "Imprumuturi", "Pachete Cadou", "Evenimente", "RAPOARTE", "Statistici", "Chitante", "Audit Log"};
        String[] commandNames = {"books", "newspapers", "", "members", "loans", "gifts", "events", "", "stats", "receipts", "audit"};

        for (int i = 0; i < menuItems.length; i++) {
            String item = menuItems[i];
            String cmdName = commandNames[i];

            if (item.equals("OPERATIUNI") || item.equals("RAPOARTE")) {
                sideMenu.add(Box.createVerticalStrut(20));
                JLabel sectionLabel = new JLabel(item);
                sectionLabel.setForeground(AMBER);
                sectionLabel.setFont(GEORGIA_BOLD);
                sectionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                sideMenu.add(sectionLabel);
                sideMenu.add(Box.createVerticalStrut(10));
                continue;
            }

            JButton btn = createMenuButton(item, FOREST_600, TEXT_DARK);

            if (!cmdName.isEmpty()) {
                if (cmdName.equals("newspapers")) {
                    btn.addActionListener(e -> cardLayout.show(contentPanel, "newspapers"));
                } else if (cmdName.equals("audit")) {
                    btn.addActionListener(e -> cardLayout.show(contentPanel, "audit"));
                } else {
                    btn.addActionListener(e -> executeCommand(cmdName));
                }
            }

            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            sideMenu.add(btn);
            sideMenu.add(Box.createVerticalStrut(5));
        }

        sideMenu.add(Box.createVerticalGlue());

        JPanel undoRedoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        undoRedoPanel.setBackground(FOREST_700);
        undoRedoPanel.setMaximumSize(new Dimension(220, 50));

        JButton undoBtn = createSmallButton("Undo", FOREST_500, TEXT_DARK);
        JButton redoBtn = createSmallButton("Redo", FOREST_500, TEXT_DARK);

        undoBtn.addActionListener(e -> undoLastCommand());
        redoBtn.addActionListener(e -> redoLastCommand());

        undoRedoPanel.add(undoBtn);
        undoRedoPanel.add(redoBtn);
        undoRedoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sideMenu.add(undoRedoPanel);
        sideMenu.add(Box.createVerticalStrut(10));

        JButton logoutBtn = createMenuButton("Delogare", FOREST_600, TEXT_DARK);
        logoutBtn.addActionListener(e -> performLogout());
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        sideMenu.add(logoutBtn);
        sideMenu.add(Box.createVerticalStrut(10));

        return sideMenu;
    }

    private JButton createSmallButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btn.setFont(GEORGIA_PLAIN);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(FOREST_400);
                btn.setForeground(TEXT_DARK);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg);
                btn.setForeground(fg);
            }
        });
        return btn;
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(FOREST_600);
        topBar.setPreferredSize(new Dimension(0, 70));
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));

        JLabel welcomeLabel = new JLabel("Bine ai venit, " + (currentLibrarian != null ? currentLibrarian.getUserName() : "Admin") + "!");
        welcomeLabel.setForeground(CREAM_50);
        welcomeLabel.setFont(GEORGIA_BIG);

        JLabel dateLabel = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        dateLabel.setForeground(CREAM_200);
        dateLabel.setFont(GEORGIA_PLAIN);

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
        btn.setFont(GEORGIA_BOLD);
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
        btn.setMaximumSize(new Dimension(200, 40));
        btn.setPreferredSize(new Dimension(200, 40));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setFont(GEORGIA_PLAIN);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(FOREST_500);
                btn.setForeground(CREAM_50);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg);
                btn.setForeground(fg);
            }
        });
        return btn;
    }

    private JPanel createBooksStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        statsPanel.setBackground(CREAM_50);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 15, 10));

        int totalBooks = bookService.findAllBooksLegacy().size();
        int activeLoans = loanService.getActiveLoans().size();
        int availableStock = 0;
        for (Book book : bookService.findAllBooksLegacy()) {
            Stock stock = stockService.getStock(book.getIsbn());
            if (stock != null) {
                availableStock += stock.getAvailableQuantity();
            }
        }

        statsPanel.add(createStatCard("TOTAL CARTI", String.valueOf(totalBooks), "12 adaugate luna aceasta", "5 scadente azi"));
        statsPanel.add(createStatCard("IMPRUMUTURI ACTIVE", String.valueOf(activeLoans), "3 inregistrati recent", ""));
        statsPanel.add(createStatCard("DISPONIBILE STOC", String.valueOf(availableStock), "din " + totalBooks + " titluri", ""));

        return statsPanel;
    }

    private JPanel createNewspapersStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        statsPanel.setBackground(CREAM_50);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 15, 10));

        int totalNewspapers = newspaperService.getAllItems().size();
        int availableStock = totalNewspapers;

        statsPanel.add(createStatCard("TOTAL ZIARE", String.valueOf(totalNewspapers), "3 adaugate luna aceasta", ""));
        statsPanel.add(createStatCard("DISPONIBILE STOC", String.valueOf(availableStock), "din " + totalNewspapers + " titluri", ""));

        return statsPanel;
    }

    private JPanel createStatCard(String title, String value, String subtitle1, String subtitle2) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CREAM_100);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CREAM_200, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(GEORGIA_BOLD);
        titleLabel.setForeground(TEXT_MUTED);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Georgia", Font.BOLD, 32));
        valueLabel.setForeground(FOREST_700);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub1Label = new JLabel(subtitle1);
        sub1Label.setFont(GEORGIA_ITALIC);
        sub1Label.setForeground(TEXT_MUTED);
        sub1Label.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(sub1Label);

        if (!subtitle2.isEmpty()) {
            JLabel sub2Label = new JLabel(subtitle2);
            sub2Label.setFont(GEORGIA_ITALIC);
            sub2Label.setForeground(TEXT_MUTED);
            sub2Label.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(sub2Label);
        }

        return card;
    }

    private JPanel createBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CREAM_50);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        panel.add(createBooksStatsPanel(), BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(CREAM_50);
        JLabel searchLabel = new JLabel("Cauta titlu, autor...");
        searchLabel.setFont(GEORGIA_ITALIC);
        searchLabel.setForeground(TEXT_DARK);
        JTextField searchField = new JTextField(30);
        styleTextField(searchField);
        JButton searchBtn = createStyledButton("Cauta", FOREST_400, TEXT_DARK);
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setBackground(CREAM_50);
        JButton addBookBtn = createStyledButton("Adauga Carte", FOREST_500, TEXT_DARK);
        JButton deleteBookBtn = createStyledButton("Sterge Carte", FOREST_500, TEXT_DARK);
        JButton setRestrictionBtn = createStyledButton("Set Restrictii", FOREST_500, TEXT_DARK);
        JButton refreshBtn = createStyledButton("Refresh", FOREST_500, TEXT_DARK);
        JButton availabilityBtn = createStyledButton("Disponibilitate", AMBER, TEXT_DARK);
        JButton sortByTitleBtn = createStyledButton("Sorteaza dupa Titlu", FOREST_400, TEXT_DARK);
        JButton sortByAuthorBtn = createStyledButton("Sorteaza dupa Autor", FOREST_400, TEXT_DARK);
        JButton sortByYearBtn = createStyledButton("Sorteaza dupa An", FOREST_400, TEXT_DARK);

        buttonPanel.add(addBookBtn);
        buttonPanel.add(deleteBookBtn);
        buttonPanel.add(setRestrictionBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(availabilityBtn);
        buttonPanel.add(sortByTitleBtn);
        buttonPanel.add(sortByAuthorBtn);
        buttonPanel.add(sortByYearBtn);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(CREAM_50);
        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        JTable table = new JTable();
        table.setModel(new BooksTableModel());
        styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(CREAM_200),
                "Catalog carti",
                TitledBorder.LEFT, TitledBorder.TOP,
                GEORGIA_BOLD, FOREST_600
        ));

        // Create details panel with nicer styling
        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBackground(CREAM_50);
        detailsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, CREAM_200),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        detailsPanel.setVisible(false);

        JTextArea detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(GEORGIA_PLAIN);
        detailsArea.setBackground(CREAM_100);
        detailsArea.setForeground(TEXT_DARK);
        detailsArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane detailsScroll = new JScrollPane(detailsArea);
        detailsScroll.setBorder(BorderFactory.createLineBorder(CREAM_200));
        detailsScroll.getViewport().setBackground(CREAM_100);

        JLabel detailsTitle = new JLabel("DETALII CARTE");
        detailsTitle.setFont(GEORGIA_BOLD);
        detailsTitle.setForeground(FOREST_600);
        detailsTitle.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 0));

        detailsPanel.add(detailsTitle, BorderLayout.NORTH);
        detailsPanel.add(detailsScroll, BorderLayout.CENTER);

        // Use JSplitPane for resizable details panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, detailsPanel);
        splitPane.setBackground(CREAM_50);
        splitPane.setResizeWeight(0.7);
        splitPane.setDividerSize(8);
        splitPane.setDividerLocation(500);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(splitPane, BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    showBookDetails(selectedRow, table, detailsArea);
                    detailsPanel.setVisible(true);
                } else {
                    detailsPanel.setVisible(false);
                }
            }
        });

        refreshBtn.addActionListener(e -> ((BooksTableModel) table.getModel()).refresh());
        addBookBtn.addActionListener(e -> showAddBookDialog(table));
        deleteBookBtn.addActionListener(e -> showDeleteBookDialog(table));
        setRestrictionBtn.addActionListener(e -> showSetRestrictionDialog(table));
        availabilityBtn.addActionListener(e -> showAvailabilityDialog(table));

        sortByTitleBtn.addActionListener(e -> refreshBooksWithIterator(table, "title"));
        sortByAuthorBtn.addActionListener(e -> refreshBooksWithIterator(table, "author"));
        sortByYearBtn.addActionListener(e -> refreshBooksWithIterator(table, "year"));

        searchBtn.addActionListener(e -> {
            String searchTerm = searchField.getText().toLowerCase();
            if (searchTerm.isEmpty()) {
                ((BooksTableModel) table.getModel()).refresh();
            } else {
                List<IBorrowable> filtered = new ArrayList<>();
                for (IBorrowable item : bookService.findAllBooks()) {
                    Book book = getBookFromItem(item);
                    if (book != null && (book.getTitle().toLowerCase().contains(searchTerm) ||
                            book.getAuthor().getName().toLowerCase().contains(searchTerm))) {
                        filtered.add(item);
                    }
                }
                ((BooksTableModel) table.getModel()).updateWithFilteredBooks(filtered);
            }
        });

        return panel;
    }

    private void showBookDetails(int row, JTable table, JTextArea detailsArea) {
        int bookId = (int) table.getValueAt(row, 0);
        String title = (String) table.getValueAt(row, 1);
        String author = (String) table.getValueAt(row, 2);
        int year = (int) table.getValueAt(row, 3);
        String price = (String) table.getValueAt(row, 4);

        Book book = findBookById(bookId);

        StringBuilder details = new StringBuilder();
        details.append("Titlu: ").append(title).append("\n");
        details.append("Autor: ").append(author).append("\n");
        details.append("An publicare: ").append(year).append("\n");
        details.append("Pret: ").append(price).append("\n");
        details.append("--------------------------------------------------\n");

        if (book != null) {
            details.append("ISBN: ").append(book.getIsbn()).append("\n");
            details.append("Editura: ").append(book.getPublisher()).append("\n");
            details.append("Pagini: ").append(book.getPageCount()).append("\n");

            if (book instanceof FantasyBook) {
                details.append("Tip: Fantasy\n");
            } else if (book instanceof RomanceBook) {
                RomanceBook rb = (RomanceBook) book;
                details.append("Tip: Romance\n");
                details.append("Nivel romance: ").append(rb.getRomanceLevel()).append("/5\n");
                details.append("Tropi: ").append(rb.getTropes()).append("\n");
            }

            Stock stock = stockService.getStock(book.getIsbn());
            if (stock != null) {
                int borrowed = 0;
                for (Loan loan : loanService.getActiveLoans()) {
                    Book loanBook = loan.getBook();
                    if (loanBook != null && loanBook.getItemId() == bookId) {
                        borrowed++;
                    }
                }

                int total = stock.getQuantity();
                int availableCorrect = total - borrowed;

                details.append("\n--- STOC ---\n");
                details.append("Total exemplare: ").append(total).append("\n");
                details.append("Imprumutate: ").append(borrowed).append("\n");
                details.append("Disponibile: ").append(availableCorrect).append("\n");
            }
        }

        detailsArea.setText(details.toString());
        detailsArea.setCaretPosition(0);
    }

    private Book findBookById(int id) {
        for (Book b : bookService.findAllBooksLegacy()) {
            if (b.getItemId() == id) return b;
        }
        return null;
    }

    private void refreshBooksWithIterator(JTable table, String sortType) {
        List<Book> books = bookService.findAllBooksLegacy();

        if (books.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nu exista carti in biblioteca!", "Eroare", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BookCollection bookCollection = new BookCollection();
        for (Book book : books) {
            bookCollection.addBook(book);
        }

        BookIterator iterator = null;
        String sortMessage = "";

        switch (sortType) {
            case "title":
                iterator = bookCollection.createTitleIterator();
                sortMessage = "sortate dupa TITLU";
                break;
            case "author":
                iterator = bookCollection.createAuthorIterator();
                sortMessage = "sortate dupa AUTOR";
                break;
            case "year":
                iterator = bookCollection.createYearIterator();
                sortMessage = "sortate dupa AN";
                break;
        }

        if (iterator == null) return;

        List<Book> sortedBooks = new ArrayList<>();
        while (iterator.hasNext()) {
            sortedBooks.add(iterator.next());
        }

        BooksTableModel model = (BooksTableModel) table.getModel();
        model.updateWithSortedBooks(sortedBooks);

        JOptionPane.showMessageDialog(this,
                "Cartile au fost " + sortMessage + "\n" +
                        "Numar total: " + sortedBooks.size() + " carti",
                "Iterator Pattern",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAvailabilityDialog(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Te rugam sa selectezi o carte pentru a vedea disponibilitatea!",
                    "Nicio selectie",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int bookId = (int) table.getValueAt(selectedRow, 0);
        Book book = findBookById(bookId);
        if (book == null) return;

        Stock stock = stockService.getStock(book.getIsbn());

        int borrowed = 0;
        for (Loan loan : loanService.getActiveLoans()) {
            Book loanBook = loan.getBook();
            if (loanBook != null && loanBook.getItemId() == bookId) {
                borrowed++;
            }
        }

        if (stock != null) {
            int total = stock.getQuantity();
            int available = stock.getAvailableQuantity();
            int reserved = stock.getReservedQuantity();

            String message = String.format(
                    "DETALII CARTE:\n" +
                            "Titlu: %s\n" +
                            "Autor: %s\n\n" +
                            "STOC:\n" +
                            "Total carti: %d\n" +
                            "Disponibile: %d\n" +
                            "Rezervate: %d\n" +
                            "Imprumutate: %d",
                    book.getTitle(),
                    book.getAuthor().getName(),
                    total,
                    available,
                    reserved,
                    borrowed
            );

            JOptionPane.showMessageDialog(this, message, "Disponibilitate Carte", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Nu exista stoc pentru aceasta carte!", "Eroare", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createNewspapersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CREAM_50);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        panel.add(createNewspapersStatsPanel(), BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(CREAM_50);
        JLabel searchLabel = new JLabel("Cauta dupa titlu, publicatie...");
        searchLabel.setFont(GEORGIA_ITALIC);
        searchLabel.setForeground(TEXT_DARK);
        JTextField searchField = new JTextField(30);
        styleTextField(searchField);
        JButton searchBtn = createStyledButton("Cauta", FOREST_400, TEXT_DARK);
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setBackground(CREAM_50);
        JButton addNewspaperBtn = createStyledButton("Adauga Ziar", FOREST_500, TEXT_DARK);
        JButton refreshBtn = createStyledButton("Refresh", FOREST_500, TEXT_DARK);
        JButton sortByTitleBtn = createStyledButton("Sorteaza dupa Titlu", FOREST_400, TEXT_DARK);
        JButton sortByPublisherBtn = createStyledButton("Sorteaza dupa Editura", FOREST_400, TEXT_DARK);

        buttonPanel.add(addNewspaperBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(sortByTitleBtn);
        buttonPanel.add(sortByPublisherBtn);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(CREAM_50);
        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        JTable table = new JTable();
        table.setModel(new NewspapersTableModel());
        styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(CREAM_200),
                "Catalog ziare",
                TitledBorder.LEFT, TitledBorder.TOP,
                GEORGIA_BOLD, FOREST_600
        ));

        // Create details panel with nicer styling
        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBackground(CREAM_50);
        detailsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, CREAM_200),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        detailsPanel.setVisible(false);

        JTextArea detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(GEORGIA_PLAIN);
        detailsArea.setBackground(CREAM_100);
        detailsArea.setForeground(TEXT_DARK);
        detailsArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane detailsScroll = new JScrollPane(detailsArea);
        detailsScroll.setBorder(BorderFactory.createLineBorder(CREAM_200));
        detailsScroll.getViewport().setBackground(CREAM_100);

        JLabel detailsTitle = new JLabel("DETALII ZIAR");
        detailsTitle.setFont(GEORGIA_BOLD);
        detailsTitle.setForeground(FOREST_600);
        detailsTitle.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 0));

        detailsPanel.add(detailsTitle, BorderLayout.NORTH);
        detailsPanel.add(detailsScroll, BorderLayout.CENTER);

        // Use JSplitPane for resizable details panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, detailsPanel);
        splitPane.setBackground(CREAM_50);
        splitPane.setResizeWeight(0.7);
        splitPane.setDividerSize(8);
        splitPane.setDividerLocation(500);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(splitPane, BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    showNewspaperDetails(selectedRow, table, detailsArea);
                    detailsPanel.setVisible(true);
                } else {
                    detailsPanel.setVisible(false);
                }
            }
        });

        refreshBtn.addActionListener(e -> ((NewspapersTableModel) table.getModel()).refresh());
        addNewspaperBtn.addActionListener(e -> showAddNewspaperDialog(table));
        sortByTitleBtn.addActionListener(e -> refreshNewspapersWithIterator(table, "title"));
        sortByPublisherBtn.addActionListener(e -> refreshNewspapersWithIterator(table, "publisher"));

        searchBtn.addActionListener(e -> {
            String term = searchField.getText().toLowerCase();
            if (term.isEmpty()) {
                ((NewspapersTableModel) table.getModel()).refresh();
            } else {
                List<Newspaper> filtered = newspaperService.getAllItems().stream()
                        .filter(item -> item instanceof Newspaper)
                        .map(item -> (Newspaper) item)
                        .filter(n -> n.getTitle().toLowerCase().contains(term) ||
                                n.getPublisher().getName().toLowerCase().contains(term))
                        .toList();
                ((NewspapersTableModel) table.getModel()).updateWithSortedNewspapers(filtered);
            }
        });

        return panel;
    }

    private void showNewspaperDetails(int row, JTable table, JTextArea detailsArea) {
        int id = (int) table.getValueAt(row, 0);
        String title = (String) table.getValueAt(row, 1);
        String type = (String) table.getValueAt(row, 2);
        String publisher = (String) table.getValueAt(row, 3);
        String issn = (String) table.getValueAt(row, 4);

        StringBuilder details = new StringBuilder();
        details.append("Titlu: ").append(title).append("\n");
        details.append("Tip: ").append(type).append("\n");
        details.append("Editura: ").append(publisher).append("\n");
        details.append("ISSN: ").append(issn).append("\n");
        details.append("--------------------------------------------------\n");

        Newspaper newspaper = findNewspaperById(id);
        if (newspaper != null) {
            details.append("An publicare: ").append(newspaper.getPublicationDate()).append("\n");
            details.append("Numar pagini: ").append(newspaper.getPageCount()).append("\n");

            if (newspaper instanceof LocalNewspaper) {
                LocalNewspaper local = (LocalNewspaper) newspaper;
                details.append("\n--- INFORMATII LOCALE ---\n");
                details.append("Oras: ").append(local.getCity()).append("\n");
                details.append("Regiune: ").append(local.getRegion()).append("\n");
            } else if (newspaper instanceof NationalNewspaper) {
                NationalNewspaper national = (NationalNewspaper) newspaper;
                details.append("\n--- INFORMATII NATIONALE ---\n");
                details.append("Arie distributie: ").append(national.getDistributionArea()).append("\n");
                details.append("Orientare politica: ").append(national.getPoliticalOrientation()).append("\n");
            }

            Stock stock = stockService.getStock(newspaper.getIssn());
            if (stock != null) {
                details.append("\n--- STOC ---\n");
                details.append("Total exemplare: ").append(stock.getQuantity()).append("\n");
                details.append("Disponibile: ").append(stock.getAvailableQuantity()).append("\n");
            }
        }

        detailsArea.setText(details.toString());
        detailsArea.setCaretPosition(0);
    }

    private Newspaper findNewspaperById(int id) {
        for (LibraryItem item : newspaperService.getAllItems()) {
            if (item instanceof Newspaper && ((Newspaper) item).getItemId() == id) {
                return (Newspaper) item;
            }
        }
        return null;
    }

    private void refreshNewspapersWithIterator(JTable table, String sortType) {
        List<Newspaper> newspapers = newspaperService.getAllItems().stream()
                .filter(item -> item instanceof Newspaper)
                .map(item -> (Newspaper) item)
                .toList();

        if (newspapers.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nu exista ziare in biblioteca", "Eroare", JOptionPane.WARNING_MESSAGE);
            return;
        }

        NewspaperCollection newspaperCollection = new NewspaperCollection();
        for (Newspaper newspaper : newspapers) {
            newspaperCollection.addNewspaper(newspaper);
        }

        NewspaperIterator iterator = null;
        String sortMessage = "";

        switch (sortType) {
            case "title":
                iterator = newspaperCollection.createTitleIterator();
                sortMessage = "sortate dupa TITLU";
                break;
            case "publisher":
                iterator = newspaperCollection.createPublisherIterator();
                sortMessage = "sortate dupa EDITURA";
                break;
        }

        if (iterator == null) return;

        List<Newspaper> sortedNewspapers = new ArrayList<>();
        while (iterator.hasNext()) {
            sortedNewspapers.add(iterator.next());
        }

        NewspapersTableModel model = (NewspapersTableModel) table.getModel();
        model.updateWithSortedNewspapers(sortedNewspapers);

        JOptionPane.showMessageDialog(this,
                "Ziarele au fost " + sortMessage + "\n" +
                        "Numar total: " + sortedNewspapers.size() + " ziare",
                "Iterator",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel createMembersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CREAM_50);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(CREAM_50);
        JButton addMemberBtn = createStyledButton("Adauga Membru", FOREST_500, TEXT_DARK);
        JButton refreshBtn = createStyledButton("Refresh", FOREST_500, TEXT_DARK);
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
                return "Doar in sala";
            } else if (item instanceof RestrictedAccessDecorator ra) {
                return "Acces: " + ra.getRequiredLevel();
            } else if (item instanceof ApprovalRequiredDecorator ar) {
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

    private JPanel createLoansPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CREAM_50);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(CREAM_50);
        JButton createLoanBtn = createStyledButton("Creaza Imprumut", FOREST_500, TEXT_DARK);
        JButton returnLoanBtn = createStyledButton("Returneaza", FOREST_500, TEXT_DARK);
        JButton renewLoanBtn = createStyledButton("Prelungeste", FOREST_500, TEXT_DARK);
        JButton undoBtn = createStyledButton("Undo Ultimul Imprumut", FOREST_500, TEXT_DARK);
        JButton showHistoryBtn = createStyledButton("Istoric Imprumuturi", FOREST_500, TEXT_DARK);
        JButton refreshBtn = createStyledButton("Refresh", FOREST_500, TEXT_DARK);
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
        splitPane.setBackground(CREAM_50);
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
                        JOptionPane.showMessageDialog(this, "Ultimul imprumut a fost anulat cu succes", "Undo", JOptionPane.INFORMATION_MESSAGE);
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
        panel.setBackground(CREAM_50);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(CREAM_50);
        JButton createStandardBtn = createStyledButton("Pachet Standard", FOREST_500, TEXT_DARK);
        JButton createPremiumBtn = createStyledButton("Pachet Premium", FOREST_500, TEXT_DARK);
        JButton refreshBtn = createStyledButton("Refresh", FOREST_500, TEXT_DARK);
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
        panel.setBackground(CREAM_50);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(CREAM_50);

        JButton processReturnBtn = createStyledButton("Procesare Returnare", FOREST_500, TEXT_DARK);
        JButton showHistoryBtn = createStyledButton("Istoric Chitante", FOREST_500, TEXT_DARK);
        JButton clearHistoryBtn = createStyledButton("Goleste Istoric", FOREST_500, TEXT_DARK);

        buttonPanel.add(processReturnBtn);
        buttonPanel.add(showHistoryBtn);
        buttonPanel.add(clearHistoryBtn);

        JTextArea receiptArea = new JTextArea();
        receiptArea.setEditable(false);
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        receiptArea.setBackground(CREAM_100);
        receiptArea.setForeground(TEXT_DARK);

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
        panel.setBackground(CREAM_50);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(CREAM_50);
        JButton addEventBtn = createStyledButton("Adauga Eveniment", FOREST_500, TEXT_DARK);
        JButton addGroupBtn = createStyledButton("Adauga Grup", FOREST_500, TEXT_DARK);
        JButton addToGroupBtn = createStyledButton("Adauga Eveniment in Grup", FOREST_500, TEXT_DARK);
        JButton registerEventBtn = createStyledButton("Inregistrare Eveniment", FOREST_500, TEXT_DARK);
        JButton refreshBtn = createStyledButton("Refresh", FOREST_500, TEXT_DARK);
        buttonPanel.add(addEventBtn);
        buttonPanel.add(addGroupBtn);
        buttonPanel.add(addToGroupBtn);
        buttonPanel.add(registerEventBtn);
        buttonPanel.add(refreshBtn);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setBackground(CREAM_50);
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
        eventTree.setBackground(CREAM_100);
        eventTree.setForeground(TEXT_DARK);
        eventTree.setFont(GEORGIA_PLAIN);
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
        panel.setBackground(CREAM_50);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel buttonPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        buttonPanel.setBackground(CREAM_50);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton fullStatsBtn = createStyledButton("Statistici Complete", FOREST_500, TEXT_DARK);
        JButton bookStatsBtn = createStyledButton("Statistici Carti", FOREST_500, TEXT_DARK);
        JButton loanStatsBtn = createStyledButton("Statistici Imprumuturi", FOREST_500, TEXT_DARK);
        JButton memberStatsBtn = createStyledButton("Statistici Membri", FOREST_500, TEXT_DARK);
        JButton newspaperStatsBtn = createStyledButton("Statistici Ziare", FOREST_500, TEXT_DARK);
        JButton refreshBtn = createStyledButton("Refresh", FOREST_500, TEXT_DARK);
        JButton exportStatsBtn = createStyledButton("Exporta Statistici", AMBER, TEXT_DARK);
        JButton visitorDemoBtn = createStyledButton("Demo Visitor", FOREST_400, TEXT_DARK);

        buttonPanel.add(fullStatsBtn);
        buttonPanel.add(bookStatsBtn);
        buttonPanel.add(loanStatsBtn);
        buttonPanel.add(memberStatsBtn);
        buttonPanel.add(newspaperStatsBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(exportStatsBtn);
        buttonPanel.add(visitorDemoBtn);

        JTextArea statsArea = new JTextArea();
        statsArea.setEditable(false);
        statsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        statsArea.setBackground(CREAM_100);
        statsArea.setForeground(TEXT_DARK);
        statsArea.setBorder(BorderFactory.createLineBorder(CREAM_200));

        JScrollPane scrollPane = new JScrollPane(statsArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(CREAM_200),
                "Rezultate Statistici",
                TitledBorder.LEFT, TitledBorder.TOP,
                GEORGIA_BOLD, FOREST_600
        ));

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        fullStatsBtn.addActionListener(e -> statsArea.setText(statisticsMenu.getFullStatistics()));
        bookStatsBtn.addActionListener(e -> statsArea.setText(statisticsMenu.getBookStatistics()));
        loanStatsBtn.addActionListener(e -> statsArea.setText(statisticsMenu.getLoanStatistics()));
        memberStatsBtn.addActionListener(e -> statsArea.setText(statisticsMenu.getMemberStatistics()));
        newspaperStatsBtn.addActionListener(e -> statsArea.setText(statisticsMenu.getNewspaperStatistics()));
        refreshBtn.addActionListener(e -> statsArea.setText(statisticsMenu.getFullStatistics()));
        exportStatsBtn.addActionListener(e -> exportStatisticsToFile(statsArea.getText()));
        visitorDemoBtn.addActionListener(e -> showStatisticsDialog());

        statsArea.setText(statisticsMenu.getFullStatistics());

        return panel;
    }

    private JPanel createAuditPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CREAM_50);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(CREAM_50);
        JButton refreshBtn = createStyledButton("Refresh", FOREST_500, TEXT_DARK);
        JButton clearBtn = createStyledButton("Sterge Istoric", FOREST_500, TEXT_DARK);
        JButton exportBtn = createStyledButton("Exporta Log", AMBER, TEXT_DARK);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(clearBtn);
        buttonPanel.add(exportBtn);

        JTextArea logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logArea.setBackground(CREAM_100);
        logArea.setForeground(TEXT_DARK);
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(CREAM_200),
                "Jurnal audit - operatiuni recente",
                TitledBorder.LEFT, TitledBorder.TOP,
                GEORGIA_BOLD, FOREST_600
        ));

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        refreshBtn.addActionListener(e -> {
            logArea.setText(AuditLogger.getInstance().getRecentLogs());
            if (logArea.getText().isEmpty()) {
                logArea.setText("Nicio operatiune inregistrata.\n");
            }
        });

        clearBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Sigur stergeti istoricul logurilor din memorie?",
                    "Confirmare",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                AuditLogger.getInstance().clearRecentLogs();
                logArea.setText("Istoric log sters.\n");
                AuditLogger.getInstance().log("SISTEM", "Istoricul logurilor a fost sters");
            }
        });

        exportBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File("audit_log_export.txt"));
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try (PrintWriter writer = new PrintWriter(new FileWriter(fileChooser.getSelectedFile()))) {
                    writer.print(AuditLogger.getInstance().getRecentLogs());
                    JOptionPane.showMessageDialog(this, "Log exportat cu succes!");
                    AuditLogger.getInstance().log("EXPORT_LOG", "Log exportat in fisier: " + fileChooser.getSelectedFile().getName());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Eroare la export: " + ex.getMessage());
                }
            }
        });

        return panel;
    }

    private void exportStatisticsToFile(String statisticsText) {
        if (statisticsText == null || statisticsText.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nu exista statistici de exportat!", "Eroare", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] formats = {"CSV", "TXT"};
        int choice = JOptionPane.showOptionDialog(this,
                "Alege formatul de export:", "Export Statistici",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, formats, formats[0]);

        if (choice == -1) return;

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("statistici_export"));

        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        String path = fileChooser.getSelectedFile().getAbsolutePath();

        if (choice == 0) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(path + ".csv"))) {
                String[] lines = statisticsText.split("\n");
                for (String line : lines) {
                    writer.println(line.replace(" | ", ",").replace(":", ","));
                }
                JOptionPane.showMessageDialog(this, "Statistici exportate in CSV");
                AuditLogger.getInstance().log("EXPORT_STATISTICI", "Statistici exportate in CSV: " + path + ".csv");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Eroare la export: " + e.getMessage());
            }
        } else {
            try (PrintWriter writer = new PrintWriter(new FileWriter(path + ".txt"))) {
                writer.println(statisticsText);
                JOptionPane.showMessageDialog(this, "Statistici exportate in TXT");
                AuditLogger.getInstance().log("EXPORT_STATISTICI", "Statistici exportate in TXT: " + path + ".txt");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Eroare la export: " + e.getMessage());
            }
        }
    }

    private void styleTable(JTable table) {
        table.getTableHeader().setBackground(FOREST_600);
        table.getTableHeader().setForeground(TEXT_DARK);
        table.getTableHeader().setFont(GEORGIA_BOLD);
        table.setRowHeight(28);
        table.setBackground(CREAM_100);
        table.setForeground(TEXT_DARK);
        table.setFont(GEORGIA_PLAIN);
        table.setGridColor(CREAM_200);
        table.setShowGrid(true);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? CREAM_100 : CREAM_50);
                    setForeground(TEXT_DARK);
                } else {
                    setBackground(FOREST_400);
                    setForeground(CREAM_50);
                }
                return this;
            }
        });
    }

    class BooksTableModel extends AbstractTableModel implements com.example.tmppp_library_management.observer.BookObserver {
        private List<IBorrowable> books;
        private final String[] columns = {"ID", "TITLU", "AUTOR", "AN", "PRET"};

        public BooksTableModel() {
            refresh();
            bookService.attach(this);
        }

        public void refresh() {
            this.books = bookService.findAllBooks();
            fireTableDataChanged();
        }

        public void updateWithFilteredBooks(List<IBorrowable> filteredBooks) {
            this.books = filteredBooks;
            fireTableDataChanged();
        }

        public void updateWithSortedBooks(List<Book> sortedBooks) {
            this.books = new ArrayList<>(sortedBooks);
            fireTableDataChanged();
        }

        public void restoreOriginalOrder() {
            refresh();
        }

        @Override
        public void update(com.example.tmppp_library_management.observer.BookEvent event) {
            refresh();
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
                case 4 -> String.format("%.2f lei", book.getPrice());
                default -> "";
            };
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

        public void updateWithSortedNewspapers(List<Newspaper> sortedNewspapers) {
            this.newspapers = new ArrayList<>(sortedNewspapers);
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
                case 3 -> n.getPublisher().getName();
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
        dialog.getContentPane().setBackground(CREAM_50);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<Book> bookCombo = new JComboBox<>(simpleBooks.toArray(new Book[0]));
        bookCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof Book b) {
                    setText(b.getTitle() + " - " + b.getAuthor().getName() + " (ID: " + b.getItemId() + ")");
                } else {
                    setText(value != null ? value.toString() : "");
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
        bookPanel.setBackground(CREAM_50);
        JLabel bookLabel = new JLabel("Selecteaza carte:");
        bookLabel.setForeground(TEXT_DARK);
        bookPanel.add(bookLabel, BorderLayout.WEST);
        bookPanel.add(bookCombo, BorderLayout.CENTER);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        dialog.add(bookPanel, gbc);
        row++;

        JPanel typePanel = new JPanel(new BorderLayout());
        typePanel.setBackground(CREAM_50);
        JLabel typeLabel = new JLabel("Tip restrictie:");
        typeLabel.setForeground(TEXT_DARK);
        typePanel.add(typeLabel, BorderLayout.WEST);
        typePanel.add(restrictionTypeCombo, BorderLayout.CENTER);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        dialog.add(typePanel, gbc);
        row++;

        JPanel roomPanel = new JPanel(new BorderLayout());
        roomPanel.setBackground(CREAM_50);
        JLabel roomLabel = new JLabel("Sala de lectura:");
        roomLabel.setForeground(TEXT_DARK);
        roomPanel.add(roomLabel, BorderLayout.WEST);
        roomPanel.add(roomField, BorderLayout.CENTER);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        dialog.add(roomPanel, gbc);
        row++;

        JPanel accessPanel = new JPanel(new BorderLayout());
        accessPanel.setBackground(CREAM_50);
        JLabel accessLabel = new JLabel("Nivel acces:");
        accessLabel.setForeground(TEXT_DARK);
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
        buttonPanel.setBackground(CREAM_50);
        JButton saveBtn = createStyledButton("Salveaza", FOREST_500, TEXT_DARK);
        JButton cancelBtn = createStyledButton("Anuleaza", FOREST_500, TEXT_DARK);
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        saveBtn.addActionListener(e -> {
            try {
                Book selectedBook = (Book) bookCombo.getSelectedItem();
                if (selectedBook == null) throw new Exception("Selecteaza o carte");

                String restrictionType = (String) restrictionTypeCombo.getSelectedItem();
                IBorrowable updatedBook = null;

                switch (restrictionType) {
                    case "Fara restrictii" -> updatedBook = selectedBook;
                    case "Doar in sala de lectura" -> {
                        String room = roomField.getText();
                        if (room.isEmpty()) throw new Exception("Introduceti sala de lectura");
                        updatedBook = new ReadingRoomDecorator(selectedBook, room);
                    }
                    case "Acces restrictionat" -> {
                        MemberType level = (MemberType) accessLevelCombo.getSelectedItem();
                        updatedBook = new RestrictedAccessDecorator(selectedBook, level);
                    }
                    case "Necesita aprobare" -> {
                        updatedBook = new ApprovalRequiredDecorator(selectedBook);
                        if (currentLibrarian != null) {
                            ((ApprovalRequiredDecorator) updatedBook).approve(currentLibrarian.getUserName());
                        }
                    }
                }

                if (updatedBook != null) {
                    bookService.updateBook(selectedBook.getItemId(), updatedBook);
                    AuditLogger.getInstance().log("SET_RESTRICTII", "Carte: " + selectedBook.getTitle() + " (ID: " + selectedBook.getItemId() + ") - Restrictie noua: " + restrictionType);
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
        dialog.getContentPane().setBackground(CREAM_50);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CREAM_50);
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
        pagesLabel.setForeground(TEXT_DARK);
        pagesLabel.setFont(GEORGIA_PLAIN);
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
        pagesError.setFont(GEORGIA_ITALIC);
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
        publisherLabel.setForeground(TEXT_DARK);
        publisherLabel.setFont(GEORGIA_PLAIN);
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
        publisherError.setFont(GEORGIA_ITALIC);
        formGbc.gridx = 1;
        formGbc.gridy = row;
        formPanel.add(publisherError, formGbc);
        row++;

        JLabel priceLabel = new JLabel("Pret (lei):");
        priceLabel.setForeground(TEXT_DARK);
        priceLabel.setFont(GEORGIA_PLAIN);
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
        priceError.setFont(GEORGIA_ITALIC);
        formGbc.gridx = 1;
        formGbc.gridy = row;
        formPanel.add(priceError, formGbc);
        row++;

        JLabel typeLabel = new JLabel("Tip:");
        typeLabel.setForeground(TEXT_DARK);
        typeLabel.setFont(GEORGIA_PLAIN);
        formGbc.gridx = 0;
        formGbc.gridy = row;
        formPanel.add(typeLabel, formGbc);

        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Fantasy", "Romance"});
        styleComboBox(typeCombo);
        formGbc.gridx = 1;
        formPanel.add(typeCombo, formGbc);
        row++;

        JLabel levelLabel = new JLabel("Nivel romance (1-5):");
        levelLabel.setForeground(TEXT_DARK);
        levelLabel.setFont(GEORGIA_PLAIN);
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
        levelError.setFont(GEORGIA_ITALIC);
        formGbc.gridx = 1;
        formGbc.gridy = row;
        formPanel.add(levelError, formGbc);
        row++;

        JLabel tropesLabel = new JLabel("Tropi:");
        tropesLabel.setForeground(TEXT_DARK);
        tropesLabel.setFont(GEORGIA_PLAIN);
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
        stockLabel.setForeground(TEXT_DARK);
        stockLabel.setFont(GEORGIA_PLAIN);
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
        stockError.setFont(GEORGIA_ITALIC);
        formGbc.gridx = 1;
        formGbc.gridy = row;
        formPanel.add(stockError, formGbc);
        row++;

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(CREAM_50);
        JButton saveBtn = createStyledButton("Salveaza", FOREST_500, TEXT_DARK);
        JButton cancelBtn = createStyledButton("Anuleaza", FOREST_500, TEXT_DARK);
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
                        AuditLogger.getInstance().log("ADAUGA_CARTE", "Fantasy: " + title + " (ID: " + book.getItemId() + ", ISBN: " + isbn + ", Stoc: " + stockQuantity + ")");
                    } else {
                        RomanceBook book = librarianService.addRomanceBook(currentToken, (int)(System.currentTimeMillis() % 10000),
                                title, year, pages, author, isbn, publisher, level, tropes, price);
                        stockService.addStock(book.getIsbn(), stockQuantity);
                        AuditLogger.getInstance().log("ADAUGA_CARTE", "Romance: " + title + " (ID: " + book.getItemId() + ", ISBN: " + isbn + ", Stoc: " + stockQuantity + ", Nivel: " + level + ", Tropi: " + tropes + ")");
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
        dialog.getContentPane().setBackground(CREAM_50);
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
        cityLabel.setForeground(TEXT_DARK);
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(cityLabel, gbc);
        gbc.gridx = 1;
        dialog.add(cityField, gbc);
        row++;

        JLabel regionLabel = new JLabel("Regiune (pentru Local):");
        regionLabel.setForeground(TEXT_DARK);
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(regionLabel, gbc);
        gbc.gridx = 1;
        dialog.add(regionField, gbc);
        row++;

        JLabel areaLabel = new JLabel("Arie distributie (pentru National):");
        areaLabel.setForeground(TEXT_DARK);
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(areaLabel, gbc);
        gbc.gridx = 1;
        dialog.add(distributionAreaField, gbc);
        row++;

        JLabel orientationLabel = new JLabel("Orientare politica (pentru National):");
        orientationLabel.setForeground(TEXT_DARK);
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(orientationLabel, gbc);
        gbc.gridx = 1;
        dialog.add(politicalOrientationField, gbc);
        row++;

        cityLabel.setVisible(false);
        cityField.setVisible(false);
        regionLabel.setVisible(false);
        regionField.setVisible(false);
        areaLabel.setVisible(false);
        distributionAreaField.setVisible(false);
        orientationLabel.setVisible(false);
        politicalOrientationField.setVisible(false);

        typeCombo.addActionListener(e -> {
            String type = (String) typeCombo.getSelectedItem();
            boolean isLocal = "Local".equals(type);
            cityLabel.setVisible(isLocal);
            cityField.setVisible(isLocal);
            regionLabel.setVisible(isLocal);
            regionField.setVisible(isLocal);
            areaLabel.setVisible(!isLocal);
            distributionAreaField.setVisible(!isLocal);
            orientationLabel.setVisible(!isLocal);
            politicalOrientationField.setVisible(!isLocal);
            dialog.pack();
        });

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(CREAM_50);
        JButton saveBtn = createStyledButton("Salveaza", FOREST_500, TEXT_DARK);
        JButton cancelBtn = createStyledButton("Anuleaza", FOREST_500, TEXT_DARK);
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
                AuditLogger.getInstance().log("ADAUGA_ZIAR", newspaper.getTitle() + " (ID: " + newspaper.getItemId() + ", ISSN: " + issn + ", Tip: " + type + ")");
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
        String bookIsbn = "N/A";
        Book book = findBookById(bookId);
        if (book != null) {
            bookIsbn = book.getIsbn();
        }

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
                AuditLogger.getInstance().log("STERGE_CARTE", "Cartea \"" + bookTitle + "\" (ID: " + bookId + ", ISBN: " + bookIsbn + ") a fost stearsa");
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
        dialog.getContentPane().setBackground(CREAM_50);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CREAM_50);
        GridBagConstraints formGbc = new GridBagConstraints();
        formGbc.insets = new Insets(5, 10, 5, 10);
        formGbc.fill = GridBagConstraints.HORIZONTAL;

        ValidatedField nameField = new ValidatedField(formPanel, formGbc, 0,
                "Nume:", validationFactory.buildNameChain(), "Nume", FormatInputValidator.FormatType.NAME);

        ValidatedField emailField = new ValidatedField(formPanel, formGbc, 2,
                "Email:", validationFactory.buildEmailChain(), "Email", FormatInputValidator.FormatType.EMAIL);

        JLabel typeLabel = new JLabel("Tip membru:");
        typeLabel.setForeground(TEXT_DARK);
        typeLabel.setFont(GEORGIA_PLAIN);
        formGbc.gridx = 0;
        formGbc.gridy = 4;
        formPanel.add(typeLabel, formGbc);

        JComboBox<MemberType> typeCombo = new JComboBox<>(MemberType.values());
        styleComboBox(typeCombo);
        formGbc.gridx = 1;
        formPanel.add(typeCombo, formGbc);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(CREAM_50);
        JButton saveBtn = createStyledButton("Salveaza", FOREST_500, TEXT_DARK);
        JButton cancelBtn = createStyledButton("Anuleaza", FOREST_500, TEXT_DARK);
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
                    AuditLogger.getInstance().log("ADAUGA_MEMBRU", member.getUserName() + " (ID: " + member.getUserId() + ", Email: " + email + ", Tip: " + type + ", Membership: " + member.getMembershipNumber() + ")");
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
        comboBox.setBackground(CREAM_50);
        comboBox.setForeground(TEXT_DARK);
        comboBox.setFont(GEORGIA_PLAIN);
    }

    private void showCreateLoanDialog(JTable table) {
        JDialog dialog = new JDialog(this, "Creaza Imprumut", true);
        dialog.setSize(550, 400);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(CREAM_50);
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
                if (value instanceof Member m) {
                    setText(m.getUserName() + " (" + m.getMemberType() + ") - " +
                            m.getCurrentLoans() + "/" + m.getMaxBooks() + " imprumuturi | " +
                            "Membership: " + m.getMembershipNumber());
                } else {
                    setText(value != null ? value.toString() : "");
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

                        if (book instanceof FantasyBook) type = "[Fantasy] ";
                        else if (book instanceof RomanceBook) type = "[Romance] ";

                        if (value instanceof ReadingRoomDecorator) restrictions = " [DOAR IN SALA]";
                        else if (value instanceof RestrictedAccessDecorator) restrictions = " [Acces restrictionat]";
                        else if (value instanceof ApprovalRequiredDecorator) restrictions = " [Aprobare necesara]";

                        setText(String.format("%s%s - %s | Stoc: %d | Pret: %.2f lei%s",
                                type, book.getTitle(), book.getAuthor().getName(), stock, book.getPrice(), restrictions));
                    } else {
                        setText(value.toString());
                    }
                } else {
                    setText(value != null ? value.toString() : "");
                }
                return this;
            }
        });

        int row = 0;
        addFormRow(dialog, "Selecteaza membru:", memberCombo, gbc, row++);
        addFormRow(dialog, "Selecteaza carte:", bookCombo, gbc, row++);

        JPanel infoPanel = new JPanel(new GridLayout(3, 1));
        infoPanel.setBackground(CREAM_50);
        JLabel infoLabel1 = new JLabel("Nota: Membrii au limita de imprumuturi in functie de tipul lor.");
        JLabel infoLabel2 = new JLabel("Cartile cu restrictii pot necesita aprobare speciala.");
        JLabel infoLabel3 = new JLabel("Cartile cu 'DOAR IN SALA' NU pot fi imprumutate acasa.");
        infoLabel1.setForeground(TEXT_MUTED);
        infoLabel2.setForeground(TEXT_MUTED);
        infoLabel3.setForeground(Color.RED);
        infoLabel1.setFont(GEORGIA_ITALIC);
        infoLabel2.setFont(GEORGIA_ITALIC);
        infoLabel3.setFont(GEORGIA_BOLD);
        infoPanel.add(infoLabel1);
        infoPanel.add(infoLabel2);
        infoPanel.add(infoLabel3);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        dialog.add(infoPanel, gbc);
        row++;

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(CREAM_50);
        JButton saveBtn = createStyledButton("Creaza Imprumut", FOREST_500, TEXT_DARK);
        JButton cancelBtn = createStyledButton("Anuleaza", FOREST_500, TEXT_DARK);
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

                if (member == null || book == null) throw new Exception("Selecteaza membru si carte");

                if (book instanceof ReadingRoomDecorator) {
                    JOptionPane.showMessageDialog(dialog,
                            "Aceasta carte poate fi citita DOAR in sala de lectura!\nNu se poate crea un imprumut pentru a fi luata acasa.",
                            "Imprumut nepermis",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (book instanceof RestrictedAccessDecorator ra) {
                    if (ra.getRequiredLevel() != member.getMemberType()) {
                        JOptionPane.showMessageDialog(dialog,
                                "Nu aveti acces la aceasta carte!\nNivel necesar: " + ra.getRequiredLevel() +
                                        "\nNivelul dvs: " + member.getMemberType(),
                                "Acces restrictionat",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                if (book instanceof ApprovalRequiredDecorator ar && !ar.isApproved()) {
                    JOptionPane.showMessageDialog(dialog,
                            "Aceasta carte necesita aprobare speciala!\nContactati un bibliotecar.",
                            "Aprobare necesara",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (!member.canBorrow()) {
                    throw new Exception("Membrul a atins limita maxima de imprumuturi (" + member.getMaxBooks() + ")");
                }

                Book bookObj = getBookFromItem(book);
                if (bookObj != null) {
                    int stock = stockService.getStock(bookObj.getIsbn()) != null ?
                            stockService.getStock(bookObj.getIsbn()).getAvailableQuantity() : 0;
                    if (stock <= 0) throw new Exception("Cartea nu este disponibila (stoc epuizat)");
                }

                Loan loan = loanService.createLoan(member, book);
                if (loan != null) {
                    memberService.incrementLoans(member.getUserId());
                    AuditLogger.getInstance().log("CREARE_IMPRUMUT", member.getUserName() + " (ID: " + member.getUserId() + ") a imprumutat \"" + bookObj.getTitle() + "\" (ID: " + bookObj.getItemId() + "). Data returnare: " + loan.getReturnDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
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

        AuditLogger.getInstance().log("RETUR_CARTE", member.getUserName() + " (ID: " + member.getUserId() + ") a returnat \"" + book.getTitle() + "\" (ID: " + book.getItemId() + "). Penalizare: " + totalPenalty + " lei, Zile intarziere: " + daysLate);

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

            int extraDays = switch (choice) {
                case 0 -> 7;
                case 1 -> 14;
                case 2 -> 30;
                default -> 0;
            };
            if (extraDays == 0) return;

            loanService.renewLoan(loan, extraDays);
            AuditLogger.getInstance().log("PRELUNGIRE_IMPRUMUT", "Imprumutul cu ID " + loanId + " a fost prelungit cu " + extraDays + " zile");
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
        dialog.getContentPane().setBackground(CREAM_50);
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
                        if (book instanceof FantasyBook) type = "[Fantasy] ";
                        else if (book instanceof RomanceBook) type = "[Romance] ";
                        setText(type + book.getTitle() + " - " + book.getAuthor().getName() +
                                " | " + String.format("%.2f", book.getPrice()) + " lei");
                    } else {
                        setText(value.toString());
                    }
                } else {
                    setText(value != null ? value.toString() : "");
                }
                return this;
            }
        });

        int row = 0;
        addFormRow(dialog, "Alege cartea:", bookCombo, gbc, row++);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(CREAM_50);
        JButton saveBtn = createStyledButton("Creeaza", FOREST_500, TEXT_DARK);
        JButton cancelBtn = createStyledButton("Anuleaza", FOREST_500, TEXT_DARK);
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
                if (book == null) throw new Exception("Selecteaza o carte valida");

                GiftPackage gift = giftPackageService.createGiftPackage(type, book, "Cadou special");
                AuditLogger.getInstance().log("CREARE_PACHET_CADOU", (type.equals("premium") ? "Premium" : "Standard") + " - Carte: " + book.getTitle() + " (ID: " + book.getItemId() + "), Pret total: " + String.format("%.2f", gift.calculateTotalPrice()) + " lei");
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
                if (child instanceof SingleEvent event) {
                    eventsNotInGroups.remove(event);
                    String spotsInfo = event.getMaxParticipants() == -1 ? "Locuri: nelimitate" : "Locuri: " + event.getRegisteredParticipants() + "/" + event.getMaxParticipants();
                    groupNode.add(new DefaultMutableTreeNode(
                            "  " + event.getName() + " (" + event.getDate() + " - " +
                                    event.getLocation() + ") [" + spotsInfo + "]"
                    ));
                } else if (child instanceof EventGroup subGroup) {
                    addEventsToNode(groupNode, subGroup);
                }
            }
            root.add(groupNode);
        }

        if (!eventsNotInGroups.isEmpty()) {
            DefaultMutableTreeNode ungroupedNode = new DefaultMutableTreeNode("Evenimente fara grup");
            for (SingleEvent event : eventsNotInGroups) {
                String spotsInfo = event.getMaxParticipants() == -1 ? "Locuri: nelimitate" : "Locuri: " + event.getRegisteredParticipants() + "/" + event.getMaxParticipants();
                ungroupedNode.add(new DefaultMutableTreeNode(
                        event.getName() + " (" + event.getDate() + " - " +
                                event.getLocation() + ") [" + spotsInfo + "]"
                ));
            }
            root.add(ungroupedNode);
        }

        tree.setModel(new DefaultTreeModel(root));
    }

    private void addEventsToNode(DefaultMutableTreeNode node, EventGroup group) {
        DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(group.getName() + " (" + group.getEvents().size() + " evenimente)");
        for (EventComponent child : group.getEvents()) {
            if (child instanceof SingleEvent event) {
                String spotsInfo = event.getMaxParticipants() == -1 ? "Locuri: nelimitate" : "Locuri: " + event.getRegisteredParticipants() + "/" + event.getMaxParticipants();
                groupNode.add(new DefaultMutableTreeNode(
                        "  " + event.getName() + " (" + event.getDate() + " - " +
                                event.getLocation() + ") [" + spotsInfo + "]"
                ));
            } else if (child instanceof EventGroup subGroup) {
                addEventsToNode(groupNode, subGroup);
            }
        }
        node.add(groupNode);
    }

    private void showAddEventDialog(JTable eventsTable, JTree tree) {
        JDialog dialog = new JDialog(this, "Adauga Eveniment", true);
        dialog.setSize(500, 500);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(CREAM_50);
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
        unlimitedCheckBox.setBackground(CREAM_50);
        unlimitedCheckBox.setForeground(TEXT_DARK);

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
        capacityPanel.setBackground(CREAM_50);
        capacityPanel.add(unlimitedCheckBox, BorderLayout.WEST);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        dialog.add(capacityPanel, gbc);
        row++;

        addFormRow(dialog, "Numar locuri (daca este cazul):", maxParticipantsField, gbc, row++);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(CREAM_50);
        JButton saveBtn = createStyledButton("Salveaza", FOREST_500, TEXT_DARK);
        JButton cancelBtn = createStyledButton("Anuleaza", FOREST_500, TEXT_DARK);
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

                AuditLogger.getInstance().log("ADAUGA_EVENIMENT", event.getName() + " (ID: " + event.getId() + ", Data: " + date + ", Locatie: " + location + ", Tip: " + type + ", Locuri: " + (event.getMaxParticipants() == -1 ? "nelimitate" : event.getMaxParticipants()) + ")");
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
        dialog.getContentPane().setBackground(CREAM_50);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(20);
        styleTextField(nameField);

        int row = 0;
        addFormRow(dialog, "Nume grup:", nameField, gbc, row++);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(CREAM_50);
        JButton saveBtn = createStyledButton("Salveaza", FOREST_500, TEXT_DARK);
        JButton cancelBtn = createStyledButton("Anuleaza", FOREST_500, TEXT_DARK);
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
                AuditLogger.getInstance().log("ADAUGA_GRUP", "Grup: " + name);
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
            if (!alreadyInGroup) availableEvents.add(event);
        }

        if (availableEvents.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Toate evenimentele sunt deja in grupuri!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Adauga Eveniment in Grup", true);
        dialog.setSize(500, 300);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(CREAM_50);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<SingleEvent> eventCombo = new JComboBox<>(availableEvents.toArray(new SingleEvent[0]));
        JComboBox<EventGroup> groupCombo = new JComboBox<>(groups.toArray(new EventGroup[0]));

        eventCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof SingleEvent e) {
                    String spotsInfo = e.getMaxParticipants() == -1 ? "Locuri: nelimitate" : "Locuri: " + e.getRegisteredParticipants() + "/" + e.getMaxParticipants();
                    setText(e.getName() + " (" + e.getDate() + " - " + e.getLocation() + ") [" + spotsInfo + "]");
                } else {
                    setText(value != null ? value.toString() : "");
                }
                return this;
            }
        });

        groupCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof EventGroup g) {
                    setText(g.getName() + " (" + g.getEvents().size() + " evenimente)");
                } else {
                    setText(value != null ? value.toString() : "");
                }
                return this;
            }
        });

        int row = 0;
        addFormRow(dialog, "Selecteaza eveniment:", eventCombo, gbc, row++);
        addFormRow(dialog, "Selecteaza grup:", groupCombo, gbc, row++);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(CREAM_50);
        JButton saveBtn = createStyledButton("Adauga in Grup", FOREST_500, TEXT_DARK);
        JButton cancelBtn = createStyledButton("Anuleaza", FOREST_500, TEXT_DARK);
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
                AuditLogger.getInstance().log("ADAUGA_EVENIMENT_IN_GRUP", event.getName() + " (ID: " + event.getId() + ") adaugat in grupul " + group.getName());
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
        List<Member> members = memberService.getAllMembers();

        if (events.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nu exista evenimente in sistem!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (members.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nu exista membri in sistem!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Inregistrare la Eveniment", true);
        dialog.setSize(550, 350);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(CREAM_50);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<Member> memberCombo = new JComboBox<>(members.toArray(new Member[0]));
        JComboBox<SingleEvent> eventCombo = new JComboBox<>(events.toArray(new SingleEvent[0]));

        memberCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof Member m) {
                    setText(m.getUserName() + " (" + m.getMemberType() + ") - " +
                            m.getCurrentLoans() + "/" + m.getMaxBooks() + " imprumuturi");
                } else {
                    setText(value != null ? value.toString() : "");
                }
                return this;
            }
        });

        eventCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof SingleEvent e) {
                    String spotsInfo = e.getMaxParticipants() == -1 ? "Locuri nelimitate" : "Locuri disponibile: " + e.getAvailableSpots() + "/" + e.getMaxParticipants();
                    setText(e.getName() + " (" + e.getDate() + " - " + e.getLocation() + ") - " + spotsInfo);
                } else {
                    setText(value != null ? value.toString() : "");
                }
                return this;
            }
        });

        int row = 0;
        addFormRow(dialog, "Selecteaza membru:", memberCombo, gbc, row++);
        addFormRow(dialog, "Selecteaza eveniment:", eventCombo, gbc, row++);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(CREAM_50);
        JButton registerBtn = createStyledButton("Inregistreaza", FOREST_500, TEXT_DARK);
        JButton cancelBtn = createStyledButton("Anuleaza", FOREST_500, TEXT_DARK);
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
                    AuditLogger.getInstance().log("INREGISTRARE_EVENIMENT", member.getUserName() + " (ID: " + member.getUserId() + ") s-a inregistrat la " + event.getName() + " (ID: " + event.getId() + "). Locuri ramase: " + event.getAvailableSpots());
                    refreshEventTree(tree);
                    dialog.dispose();
                    JOptionPane.showMessageDialog(this,
                            "Inregistrare reusita!\n" + member.getUserName() + " a fost inregistrat la " + event.getName() +
                                    "\nLocuri ramase: " + event.getAvailableSpots(),
                            "Succes",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(dialog, "Inregistrare esuata!", "Eroare", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private void showProcessReturnDialog(JTextArea receiptArea) {
        JDialog dialog = new JDialog(this, "Procesare Returnare", true);
        dialog.setSize(600, 450);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(CREAM_50);
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
        bookLabel.setForeground(TEXT_DARK);
        bookLabel.setFont(GEORGIA_PLAIN);
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(bookLabel, gbc);
        gbc.gridx = 1;
        dialog.add(bookCombo, gbc);
        row++;

        JLabel memberLabel = new JLabel("Selecteaza membru:");
        memberLabel.setForeground(TEXT_DARK);
        memberLabel.setFont(GEORGIA_PLAIN);
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(memberLabel, gbc);
        gbc.gridx = 1;
        dialog.add(memberCombo, gbc);
        row++;

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(CREAM_50);
        JButton processBtn = createStyledButton("Proceseaza Returnare", FOREST_500, TEXT_DARK);
        JButton cancelBtn = createStyledButton("Anuleaza", FOREST_500, TEXT_DARK);
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
                JOptionPane.showMessageDialog(dialog, "Date invalide!", "Eroare", JOptionPane.ERROR_MESSAGE);
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

            AuditLogger.getInstance().log("RETUR_CARTE", member.getUserName() + " (ID: " + member.getUserId() + ") a returnat \"" + book.getTitle() + "\" (ID: " + book.getItemId() + "). Penalizare: " + penalty + " lei, Zile intarziere: " + daysLate);
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
        field.setBackground(CREAM_50);
        field.setForeground(TEXT_DARK);
        field.setFont(GEORGIA_PLAIN);
        field.setBorder(BorderFactory.createLineBorder(CREAM_200));
    }

    private void addFormRow(JDialog dialog, String label, JComponent field, GridBagConstraints gbc, int row) {
        JLabel lbl = new JLabel(label);
        lbl.setForeground(TEXT_DARK);
        lbl.setFont(GEORGIA_PLAIN);
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(lbl, gbc);
        gbc.gridx = 1;
        dialog.add(field, gbc);
    }

    public class MemberDisplay {
        private Member member;
        public MemberDisplay(Member member) { this.member = member; }
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
        public BookDisplay(Book book) { this.book = book; }
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

    private void showStatisticsDialog() {
        StatisticsVisitor visitor = new StatisticsVisitor();

        for (IBorrowable item : bookService.findAllBooks()) {
            if (item instanceof Book book) book.accept(visitor);
        }

        for (LibraryItem item : newspaperService.getAllItems()) {
            if (item instanceof Newspaper newspaper) newspaper.accept(visitor);
        }

        for (Loan loan : loanService.getAllLoans()) loan.accept(visitor);
        for (Member member : memberService.getAllMembers()) member.accept(visitor);

        StatisticsResult result = visitor.getResults();
        result.printStatistics();
        AuditLogger.getInstance().log("STATISTICS_VISITOR", "Statistici generate folosind Visitor Pattern");

        JOptionPane.showMessageDialog(this,
                "Statistici generate!\nVerificati consola pentru detalii",
                "Statistici",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void initRefreshMediator() {
        DashboardRefreshMediator mediator = DashboardRefreshMediator.getInstance();
        SwingUtilities.invokeLater(() -> {
            for (Component comp : contentPanel.getComponents()) {
                if (comp instanceof JPanel panel) findAndRegisterTables(panel, mediator);
            }
        });
    }

    private void findAndRegisterTables(JPanel panel, DashboardRefreshMediator mediator) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JScrollPane scroll && scroll.getViewport().getView() instanceof JTable table) {
                AbstractTableModel model = (AbstractTableModel) table.getModel();
                if (model instanceof BooksTableModel) mediator.setComponentA(model);
                else if (model instanceof ActiveLoansTableModel) {
                    mediator.setComponentB(model);
                    mediator.setComponentF(table);
                } else if (model instanceof ClosedLoansTableModel) mediator.setComponentD(model);
                else if (model instanceof MembersTableModel) mediator.setComponentC(model);
            } else if (comp instanceof JPanel subPanel) {
                findAndRegisterTables(subPanel, mediator);
            }
        }
    }
}

