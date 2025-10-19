package com.fynance;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

public class ExpenseAppGUI extends JFrame {

    private final UserDAO userDAO = new UserDAO();
    private final GroupDAO groupDAO = new GroupDAO();
    private final GroupMemberDAO groupMemberDAO = new GroupMemberDAO();
    private final ExpenseDAO expenseDAO = new ExpenseDAO();
    private final SettlementDAO settlementDAO = new SettlementDAO();
    private final PersonalTransactionDAO personalTransactionDAO = new PersonalTransactionDAO();
    private final BudgetDAO budgetDAO = new BudgetDAO();
    private final BalanceService balanceService = new BalanceService();
    private final SettlementService settlementService = new SettlementService();

    private final CardLayout cardLayout;
    private final JPanel contentPanel;
    private final ButtonGroup navButtonGroup = new ButtonGroup();
    private DefaultTableModel userTableModel, groupTableModel, expenseTableModel, savingsTableModel;
    private PieChartPanel pieChartPanel;
    private BarChartPanel barChartPanel;
    private JLabel budgetLabel, expenseLabel, balanceLabel;
    private DefaultListModel<Object> recentTransactionListModel;
    private final int currentUserId = 1;

    public ExpenseAppGUI() {
        setTitle("Fynance — Personal Finance");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.BACKGROUND_DARK);

        // Header
        add(createHeader(), BorderLayout.NORTH);

        // Sidebar
        add(createSidebar(), BorderLayout.WEST);

        // Content (cards, charts)
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UITheme.BACKGROUND_DARK);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        contentPanel.add(createDashboardPanel(), "DASHBOARD");
        contentPanel.add(createExpensesPanel(), "EXPENSES");
        contentPanel.add(createSavingsPanel(), "SAVINGS");
        contentPanel.add(createGroupsPanel(), "GROUPS");
        contentPanel.add(createSetupPanel(), "SETTINGS");

        add(contentPanel, BorderLayout.CENTER);

        UITheme.applyTheme();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(12, 20, 12, 20));
        header.setBackground(UITheme.BACKGROUND_DARK);

        // Left: app title
        JLabel title = new JLabel("Fynance");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.ACCENT_PINK);

        // center: search
        JPanel center = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        center.setOpaque(false);
        JTextField search = new JTextField(30);
        search.setPreferredSize(new Dimension(420, 34));
        search.setBackground(UITheme.SURFACE);
        search.setForeground(UITheme.TEXT_LIGHT);
        search.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        center.add(search);

        // right: profile / quick actions
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        JLabel userLabel = new JLabel("Esther H.");
        userLabel.setFont(UITheme.FONT_MAIN);
        userLabel.setForeground(UITheme.TEXT_LIGHT);
        CircleAvatar avatar = new CircleAvatar(36, UITheme.ACCENT_BLUE_GRAY, "E");
        right.add(userLabel);
        right.add(avatar);

        header.add(title, BorderLayout.WEST);
        header.add(center, BorderLayout.CENTER);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    // small avatar component
    static class CircleAvatar extends JLabel {

        private final int size;
        private final Color bg;
        private final String text;

        CircleAvatar(int size, Color bg, String text) {
            this.size = size;
            this.bg = bg;
            this.text = text;
            setPreferredSize(new Dimension(size, size));
            setHorizontalAlignment(SwingConstants.CENTER);
            setVerticalAlignment(SwingConstants.CENTER);
            setForeground(UITheme.BACKGROUND_DARK);
            setFont(UITheme.FONT_BOLD.deriveFont(14f));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillOval(0, 0, size, size);
                g2.setColor(getForeground());
                FontMetrics fm = g2.getFontMetrics();
                int tw = fm.stringWidth(text);
                int th = fm.getAscent();
                g2.drawString(text, (size - tw) / 2, (size + th) / 2 - 3);
            } finally {
                g2.dispose();
            }
        }
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.PRIMARY_NAVY);
        sidebar.setBorder(new EmptyBorder(18, 14, 18, 14));
        sidebar.setPreferredSize(new Dimension(220, 0));

        // small logo area
        JLabel logo = new JLabel("FYNANCE");
        logo.setFont(UITheme.FONT_TITLE);
        logo.setForeground(UITheme.ACCENT_PINK);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(logo);
        sidebar.add(Box.createRigidArea(new Dimension(0, 18)));

        addNavButton("Dashboard", "DASHBOARD", sidebar, true, UITheme.ACCENT_PINK);
        addNavButton("Expenses", "EXPENSES", sidebar, false, null);
        addNavButton("Analytics", "SAVINGS", sidebar, false, null);
        addNavButton("Budget", "GROUPS", sidebar, false, null);
        addNavButton("Settings", "SETTINGS", sidebar, false, null);

        sidebar.add(Box.createVerticalGlue());

        // small footer call-to-action
        RoundedPanel upgrade = new RoundedPanel(12, false);
        upgrade.setLayout(new BorderLayout());
        upgrade.setBackground(Color.decode("#1E2A2D"));
        upgrade.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JLabel upText = new JLabel("<html><b>Upgrade</b> to PREMIUM<br><small>extra features & export</small></html>");
        upText.setForeground(UITheme.TEXT_MUTED);
        upText.setBorder(new EmptyBorder(10, 10, 10, 10));
        upgrade.add(upText, BorderLayout.CENTER);

        sidebar.add(upgrade);

        return sidebar;
    }

    private void addNavButton(String title, String cardName, JPanel parent, boolean selected, Color forcedSelectedForeground) {
        JToggleButton btn = new JToggleButton(title);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(UITheme.FONT_MAIN);
        btn.setForeground(UITheme.TEXT_LIGHT_GRAY);
        btn.setBackground(UITheme.PRIMARY_NAVY);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(6, 10, 6, 10));
        btn.setSelected(selected);

        navButtonGroup.add(btn);

        // state listener
        btn.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (btn.isSelected()) {
                    btn.setBackground(UITheme.ACCENT_BLUE_GRAY.darker());
                    btn.setForeground(forcedSelectedForeground != null ? forcedSelectedForeground : UITheme.TEXT_LIGHT);
                } else {
                    btn.setBackground(UITheme.PRIMARY_NAVY);
                    btn.setForeground(UITheme.TEXT_LIGHT_GRAY);
                }
            }
        });

        btn.addActionListener(e -> cardLayout.show(contentPanel, cardName));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!btn.isSelected()) {
                    btn.setBackground(UITheme.NAV_HOVER);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!btn.isSelected()) {
                    btn.setBackground(UITheme.PRIMARY_NAVY);
                }
            }
        });

        parent.add(btn);
        parent.add(Box.createRigidArea(new Dimension(0, 8)));
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(18, 18));
        panel.setBackground(UITheme.BACKGROUND_DARK);

        // Top: metrics
        JPanel metricsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        metricsRow.setOpaque(false);

        budgetLabel = new JLabel("₹0.00");
        expenseLabel = new JLabel("₹0.00");
        balanceLabel = new JLabel("₹0.00");

        metricsRow.add(createMetricCard("Total Budget", budgetLabel, UITheme.ACCENT_PINK));
        metricsRow.add(createMetricCard("Current Expenses", expenseLabel, UITheme.ACCENT_MAROON));
        metricsRow.add(createMetricCard("Remaining", balanceLabel, UITheme.ACCENT_PINK));

        JButton setBudgetButton = createIconButton("Set Budget");
        setBudgetButton.addActionListener(e -> setBudgetAction());
        RoundedPanel budgetBtnWrap = new RoundedPanel(12);
        budgetBtnWrap.setLayout(new FlowLayout(FlowLayout.CENTER));
        budgetBtnWrap.setBackground(UITheme.CARD_BG);
        budgetBtnWrap.add(setBudgetButton);
        budgetBtnWrap.setPreferredSize(new Dimension(160, 84));
        metricsRow.add(budgetBtnWrap);

        // Center: left column (recent + small stats) and right column (charts)
        JPanel center = new JPanel(new GridLayout(1, 2, 18, 0));
        center.setOpaque(false);

        // left column
        JPanel leftCol = new JPanel();
        leftCol.setOpaque(false);
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));

        // recent activity card
        RoundedPanel recentCard = new RoundedPanel(14);
        recentCard.setLayout(new BorderLayout());
        recentCard.setBackground(UITheme.CARD_BG);
        recentCard.setPreferredSize(new Dimension(0, 360));
        recentCard.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel recTitle = new JLabel("Recent Activity");
        recTitle.setFont(UITheme.FONT_BOLD);
        recTitle.setForeground(UITheme.TEXT_LIGHT);
        recentCard.add(recTitle, BorderLayout.NORTH);

        recentTransactionListModel = new DefaultListModel<>();
        JList<Object> recentList = new JList<>(recentTransactionListModel);
        recentList.setCellRenderer(new TransactionCellRenderer());
        JScrollPane recentScroll = new JScrollPane(recentList);
        recentScroll.setBorder(BorderFactory.createEmptyBorder());
        recentCard.add(recentScroll, BorderLayout.CENTER);

        leftCol.add(recentCard);
        leftCol.add(Box.createRigidArea(new Dimension(0, 18)));

        // small stats (profit, orders...)
        JPanel statsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        statsRow.setOpaque(false);
        statsRow.add(createSmallStat("Total Sales", "₹34,654"));
        statsRow.add(createSmallStat("Total Purchase", "₹26,235"));
        statsRow.add(createSmallStat("Profit", "₹44,721"));
        leftCol.add(statsRow);

        // right column: charts
        JPanel rightCol = new JPanel();
        rightCol.setOpaque(false);
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));

        pieChartPanel = new PieChartPanel();
        RoundedPanel pieWrap = new RoundedPanel(14);
        pieWrap.setBackground(UITheme.CARD_BG);
        pieWrap.setLayout(new BorderLayout());
        pieWrap.setPreferredSize(new Dimension(0, 260));
        pieWrap.add(new JLabel("Expense Categories", SwingConstants.LEFT), BorderLayout.NORTH);
        pieWrap.add(pieChartPanel, BorderLayout.CENTER);

        barChartPanel = new BarChartPanel();
        RoundedPanel barWrap = new RoundedPanel(14);
        barWrap.setBackground(UITheme.CARD_BG);
        barWrap.setLayout(new BorderLayout());
        barWrap.setPreferredSize(new Dimension(0, 220));
        JLabel barTitle = new JLabel("Monthly Expense Analysis");
        barTitle.setForeground(UITheme.TEXT_LIGHT);
        barWrap.add(barTitle, BorderLayout.NORTH);
        barWrap.add(barChartPanel, BorderLayout.CENTER);

        rightCol.add(pieWrap);
        rightCol.add(Box.createRigidArea(new Dimension(0, 18)));
        rightCol.add(barWrap);

        center.add(leftCol);
        center.add(rightCol);

        panel.add(metricsRow, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);

        refreshDashboard();
        return panel;
    }

    private RoundedPanel createMetricCard(String title, JLabel valueLabel, Color accent) {
        RoundedPanel card = new RoundedPanel(14);
        card.setBackground(UITheme.CARD_BG);
        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(260, 84));
        card.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.FONT_MAIN);
        titleLabel.setForeground(UITheme.TEXT_MUTED);

        valueLabel.setFont(UITheme.FONT_HEADER);
        valueLabel.setForeground(accent != null ? accent : UITheme.ACCENT_PINK);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JButton createIconButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(UITheme.FONT_MAIN);
        btn.setBackground(UITheme.ACCENT_PINK.darker());
        btn.setForeground(UITheme.BACKGROUND_DARK);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(140, 36));
        return btn;
    }

    private JPanel createExpensesPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(UITheme.BACKGROUND_DARK);
        expenseTableModel = new DefaultTableModel(new String[]{"Date", "Category", "Amount", "Notes"}, 0);
        JTable expenseTable = new JTable(expenseTableModel);
        expenseTable.setFillsViewportHeight(true);
        expenseTable.setShowGrid(false);
        expenseTable.setRowHeight(28);
        expenseTable.setForeground(UITheme.TEXT_LIGHT);
        expenseTable.setBackground(UITheme.SURFACE);
        JScrollPane sp = new JScrollPane(expenseTable);
        sp.setBorder(BorderFactory.createEmptyBorder());
        p.add(sp, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        JButton addBtn = new JButton("Add New Expense");
        addBtn.setBackground(UITheme.ACCENT_PINK);
        addBtn.setForeground(UITheme.BACKGROUND_DARK);
        addBtn.setFocusPainted(false);
        addBtn.addActionListener(e -> {
            addPersonalTransactionAction();
            refreshExpensesPanel();
        });
        bottom.add(addBtn);
        p.add(bottom, BorderLayout.SOUTH);

        refreshExpensesPanel();
        return p;
    }

    private JPanel createSavingsPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(UITheme.BACKGROUND_DARK);
        savingsTableModel = new DefaultTableModel(new String[]{"Date", "Category", "Amount", "Notes"}, 0);
        JTable t = new JTable(savingsTableModel);
        t.setFillsViewportHeight(true);
        t.setShowGrid(false);
        t.setRowHeight(28);
        t.setForeground(UITheme.TEXT_LIGHT);
        t.setBackground(UITheme.SURFACE);
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(BorderFactory.createEmptyBorder());
        p.add(sp, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        JButton add = new JButton("Add New Saving");
        add.setBackground(UITheme.ACCENT_BLUE_GRAY);
        add.setForeground(UITheme.TEXT_LIGHT);
        add.setFocusPainted(false);
        add.addActionListener(e -> {
            addPersonalTransactionAction();
            refreshSavingsPanel();
        });
        bottom.add(add);
        p.add(bottom, BorderLayout.SOUTH);

        refreshSavingsPanel();
        return p;
    }

    private JPanel createGroupsPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(UITheme.BACKGROUND_DARK);

        JTextArea ta = new JTextArea("Select an action to manage groups.");
        ta.setEditable(false);
        ta.setBackground(UITheme.SURFACE);
        ta.setForeground(UITheme.TEXT_LIGHT);
        ta.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.add(new JScrollPane(ta), BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btns.setOpaque(false);
        JButton addExpenseButton = new JButton("Add Group Expense");
        stylePrimaryButton(addExpenseButton);
        JButton editExpenseButton = new JButton("Edit Expense");
        stylePrimaryButton(editExpenseButton);
        JButton deleteExpenseButton = new JButton("Delete Expense");
        stylePrimaryButton(deleteExpenseButton);
        JButton recordPaymentButton = new JButton("Record Payment");
        stylePrimaryButton(recordPaymentButton);
        JButton viewBalancesButton = new JButton("View Balances");
        stylePrimaryButton(viewBalancesButton);
        JButton transactionHistoryButton = new JButton("Group History");
        stylePrimaryButton(transactionHistoryButton);

        btns.add(addExpenseButton);
        btns.add(editExpenseButton);
        btns.add(deleteExpenseButton);
        btns.add(recordPaymentButton);
        btns.add(viewBalancesButton);
        btns.add(transactionHistoryButton);
        p.add(btns, BorderLayout.SOUTH);

        addExpenseButton.addActionListener(e -> addGroupExpenseAction(ta));
        editExpenseButton.addActionListener(e -> editExpenseAction(ta));
        deleteExpenseButton.addActionListener(e -> deleteExpenseAction(ta));
        recordPaymentButton.addActionListener(e -> recordPaymentAction(ta));
        viewBalancesButton.addActionListener(e -> viewBalancesAction(ta));
        transactionHistoryButton.addActionListener(e -> viewTransactionHistoryAction(ta));

        return p;
    }

    private void stylePrimaryButton(JButton b) {
        b.setBackground(UITheme.CARD_BG);
        b.setForeground(UITheme.TEXT_LIGHT);
        b.setFocusPainted(false);
    }

    private JPanel createSetupPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 18, 18));
        panel.setBackground(UITheme.BACKGROUND_DARK);

        RoundedPanel userPanel = new RoundedPanel(12);
        userPanel.setLayout(new BorderLayout());
        userPanel.setBackground(UITheme.CARD_BG);
        userPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        userPanel.add(new JLabel("Manage Users"), BorderLayout.NORTH);

        userTableModel = new DefaultTableModel(new String[]{"ID", "Name", "Email"}, 0);
        JTable userTable = new JTable(userTableModel);
        userTable.setFillsViewportHeight(true);
        userTable.setBackground(UITheme.SURFACE);
        userTable.setForeground(UITheme.TEXT_LIGHT);
        userPanel.add(new JScrollPane(userTable), BorderLayout.CENTER);

        JButton addUser = new JButton("Add New User");
        addUser.addActionListener(this::addUserAction);
        JPanel upBtn = new JPanel();
        upBtn.setOpaque(false);
        upBtn.add(addUser);
        userPanel.add(upBtn, BorderLayout.SOUTH);

        RoundedPanel groupPanel = new RoundedPanel(12);
        groupPanel.setLayout(new BorderLayout());
        groupPanel.setBackground(UITheme.CARD_BG);
        groupPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        groupPanel.add(new JLabel("Manage Groups"), BorderLayout.NORTH);

        groupTableModel = new DefaultTableModel(new String[]{"ID", "Group Name"}, 0);
        JTable groupTable = new JTable(groupTableModel);
        groupTable.setFillsViewportHeight(true);
        groupTable.setBackground(UITheme.SURFACE);
        groupTable.setForeground(UITheme.TEXT_LIGHT);
        groupPanel.add(new JScrollPane(groupTable), BorderLayout.CENTER);

        JPanel gpBtn = new JPanel();
        gpBtn.setOpaque(false);
        JButton addGroupButton = new JButton("Add New Group");
        addGroupButton.addActionListener(this::addGroupAction);
        JButton addUserToGroupButton = new JButton("Add User to Group");
        addUserToGroupButton.addActionListener(this::addUserToGroupAction);
        gpBtn.add(addGroupButton);
        gpBtn.add(addUserToGroupButton);
        groupPanel.add(gpBtn, BorderLayout.SOUTH);

        panel.add(userPanel);
        panel.add(groupPanel);

        refreshUserTable();
        refreshGroupTable();
        return panel;
    }

    private JPanel createSmallStat(String title, String value) {
        RoundedPanel p = new RoundedPanel(10);
        p.setBackground(UITheme.CARD_BG);
        p.setPreferredSize(new Dimension(220, 84));
        p.setLayout(new BorderLayout());
        JLabel t = new JLabel(title);
        t.setFont(UITheme.FONT_MAIN);
        t.setForeground(UITheme.TEXT_MUTED);
        JLabel v = new JLabel(value);
        v.setFont(UITheme.FONT_BOLD);
        v.setForeground(UITheme.ACCENT_PINK);
        p.add(t, BorderLayout.NORTH);
        p.add(v, BorderLayout.CENTER);
        return p;
    }

    private void refreshDashboard() {
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();

        List<PersonalTransaction> monthlyTransactions = personalTransactionDAO.getTransactionsForUserByMonth(currentUserId, month, year);
        double totalExpenses = monthlyTransactions.stream().filter(t -> "EXPENSE".equals(t.getType())).mapToDouble(PersonalTransaction::getAmount).sum();
        expenseLabel.setText(String.format("₹%.2f", totalExpenses));

        Budget currentBudget = budgetDAO.getBudgetForUser(currentUserId, month, year);
        if (currentBudget != null) {
            double remaining = currentBudget.getBudgetAmount() - totalExpenses;
            budgetLabel.setText(String.format("₹%.2f", currentBudget.getBudgetAmount()));
            balanceLabel.setText(String.format("₹%.2f", remaining));
            balanceLabel.setForeground(remaining >= 0 ? UITheme.ACCENT_PINK : UITheme.ACCENT_MAROON);
        } else {
            budgetLabel.setText("Not Set");
            balanceLabel.setText("N/A");
        }

        recentTransactionListModel.clear();
        List<PersonalTransaction> allTransactions = personalTransactionDAO.getAllTransactionsForUser(currentUserId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        allTransactions.stream().limit(20).forEach(t -> {
            recentTransactionListModel.addElement(String.format("%s — %s ₹%.2f", t.getTransactionDate().format(formatter), t.getCategory(), t.getAmount()));
        });

        Map<String, Double> expenseData = monthlyTransactions.stream().filter(t -> "EXPENSE".equals(t.getType()))
                .collect(Collectors.groupingBy(PersonalTransaction::getCategory, Collectors.summingDouble(PersonalTransaction::getAmount)));
        pieChartPanel.setData(expenseData);

        Map<String, Double> historicalData = personalTransactionDAO.getMonthlyExpenseSummary(currentUserId, 6);
        barChartPanel.setData(historicalData);

        refreshExpensesPanel();
        refreshSavingsPanel();
    }

    private void refreshExpensesPanel() {
        if (expenseTableModel == null) {
            return;
        }
        expenseTableModel.setRowCount(0);
        List<PersonalTransaction> transactions = personalTransactionDAO.getAllTransactionsForUser(currentUserId);
        transactions.stream().filter(t -> "EXPENSE".equals(t.getType())).forEach(t -> expenseTableModel.addRow(new Object[]{t.getTransactionDate(), t.getCategory(), t.getAmount(), t.getNotes()}));
    }

    private void refreshSavingsPanel() {
        if (savingsTableModel == null) {
            return;
        }
        savingsTableModel.setRowCount(0);
        List<PersonalTransaction> transactions = personalTransactionDAO.getAllTransactionsForUser(currentUserId);
        transactions.stream().filter(t -> "SAVING".equals(t.getType())).forEach(t -> savingsTableModel.addRow(new Object[]{t.getTransactionDate(), t.getCategory(), t.getAmount(), t.getNotes()}));
    }

    private void addPersonalTransactionAction() {
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"EXPENSE", "INCOME", "SAVING"});
        JTextField amountField = new JTextField();
        JTextField categoryField = new JTextField();
        JTextArea notesArea = new JTextArea(3, 20);
        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.setBackground(UITheme.BACKGROUND_DARK);
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        panel.add(new JLabel("Type:"));
        panel.add(typeCombo);
        panel.add(new JLabel("Amount:"));
        panel.add(amountField);
        panel.add(new JLabel("Category:"));
        panel.add(categoryField);
        panel.add(new JLabel("Notes:"));
        panel.add(new JScrollPane(notesArea));
        int option = JOptionPane.showConfirmDialog(this, panel, "Add Personal Transaction", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String type = (String) typeCombo.getSelectedItem();
                double amount = Double.parseDouble(amountField.getText());
                String category = categoryField.getText();
                String notes = notesArea.getText();
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, "Amount must be positive.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                PersonalTransaction trans = new PersonalTransaction(currentUserId, type, amount, category, LocalDate.now(), notes);
                personalTransactionDAO.addTransaction(trans);
                refreshDashboard();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void setBudgetAction() {
        String budgetStr = JOptionPane.showInputDialog(this, "Enter budget for the current month:");
        if (budgetStr != null && !budgetStr.trim().isEmpty()) {
            try {
                double budgetAmount = Double.parseDouble(budgetStr);
                LocalDate today = LocalDate.now();
                Budget budget = new Budget(currentUserId, today.getMonthValue(), today.getYear(), budgetAmount);
                budgetDAO.setBudget(budget);
                refreshDashboard();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid budget amount.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // GROUP / USER actions kept identical to original - no change to logic
    private void addUserAction(ActionEvent e) {
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        Object[] message = {"Name:", nameField, "Email:", emailField};
        int option = JOptionPane.showConfirmDialog(this, message, "Add New User", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText();
            String email = emailField.getText();
            if (!name.isEmpty() && !email.isEmpty()) {
                userDAO.addUser(new User(name, email));
                refreshUserTable();
                JOptionPane.showMessageDialog(this, "User added successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Name and email are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addGroupAction(ActionEvent e) {
        String groupName = JOptionPane.showInputDialog(this, "Enter group name:");
        if (groupName != null && !groupName.trim().isEmpty()) {
            groupDAO.addGroup(new ExpenseGroup(groupName));
            refreshGroupTable();
        }
    }

    private void addUserToGroupAction(ActionEvent e) {
        JTextField groupIdField = new JTextField();
        JTextField userIdField = new JTextField();
        Object[] message = {"Group ID:", groupIdField, "User ID:", userIdField};
        int option = JOptionPane.showConfirmDialog(this, message, "Add User to Group", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                int groupId = Integer.parseInt(groupIdField.getText());
                int userId = Integer.parseInt(userIdField.getText());
                groupMemberDAO.addMemberToGroup(groupId, userId);
                JOptionPane.showMessageDialog(this, "User added to group successfully!");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid ID.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addGroupExpenseAction(JTextArea displayArea) {
        JTextField groupIdField = new JTextField();
        JTextField payerIdField = new JTextField();
        JTextField amountField = new JTextField();
        JTextField descriptionField = new JTextField();
        JTextField categoryField = new JTextField();
        Object[] message = {"Group ID:", groupIdField, "Payer User ID:", payerIdField, "Amount:", amountField, "Description:", descriptionField, "Category:", categoryField};
        int option = JOptionPane.showConfirmDialog(this, message, "Add Group Expense", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                int groupId = Integer.parseInt(groupIdField.getText());
                int payerId = Integer.parseInt(payerIdField.getText());
                double amount = Double.parseDouble(amountField.getText());
                String description = descriptionField.getText();
                String category = categoryField.getText();
                Expense expense = new Expense(groupId, payerId, amount, description, category);
                List<GroupMember> members = groupMemberDAO.getMembersByGroup(groupId);
                if (members.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "This group has no members.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                List<ExpenseSplit> splits = new ArrayList<>();
                double perHead = Math.round((amount / members.size()) * 100.0) / 100.0;
                double totalAssigned = 0.0;
                for (int i = 0; i < members.size(); i++) {
                    GroupMember gm = members.get(i);
                    double assigned = (i == members.size() - 1) ? Math.round((amount - totalAssigned) * 100.0) / 100.0 : perHead;
                    totalAssigned += assigned;
                    splits.add(new ExpenseSplit(0, gm.getUserId(), assigned, null));
                }
                expenseDAO.addExpenseWithSplits(expense, splits);
                displayArea.setText("Group expense added successfully!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editExpenseAction(JTextArea displayArea) {
        String expenseIdStr = JOptionPane.showInputDialog(this, "Enter ID of group expense to edit:");
        if (expenseIdStr == null || expenseIdStr.trim().isEmpty()) {
            return;
        }
        try {
            int expenseId = Integer.parseInt(expenseIdStr);
            Expense expense = expenseDAO.getExpenseById(expenseId);
            if (expense == null) {
                JOptionPane.showMessageDialog(this, "Expense not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JTextField amountField = new JTextField(String.valueOf(expense.getAmount()));
            JTextField descriptionField = new JTextField(expense.getDescription());
            JTextField categoryField = new JTextField(expense.getCategory());
            Object[] message = {"Amount:", amountField, "Description:", descriptionField, "Category:", categoryField};
            int option = JOptionPane.showConfirmDialog(this, message, "Edit Expense " + expenseId, JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                expense.setAmount(Double.parseDouble(amountField.getText()));
                expense.setDescription(descriptionField.getText());
                expense.setCategory(categoryField.getText());
                expenseDAO.updateExpense(expense);
                displayArea.setText("Expense " + expenseId + " updated successfully!");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid ID.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteExpenseAction(JTextArea displayArea) {
        String expenseIdStr = JOptionPane.showInputDialog(this, "Enter ID of group expense to delete:");
        if (expenseIdStr == null || expenseIdStr.trim().isEmpty()) {
            return;
        }
        try {
            int expenseId = Integer.parseInt(expenseIdStr);
            int confirm = JOptionPane.showConfirmDialog(this, "Delete expense " + expenseId + "?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                expenseDAO.deleteExpense(expenseId);
                displayArea.setText("Expense " + expenseId + " deleted successfully!");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid ID.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void recordPaymentAction(JTextArea displayArea) {
        JTextField groupIdField = new JTextField();
        JTextField fromUserIdField = new JTextField();
        JTextField toUserIdField = new JTextField();
        JTextField amountField = new JTextField();
        Object[] message = {"Group ID:", groupIdField, "From User ID (Payer):", fromUserIdField, "To User ID (Receiver):", toUserIdField, "Amount:", amountField};
        int option = JOptionPane.showConfirmDialog(this, message, "Record Group Settlement Payment", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                int groupId = Integer.parseInt(groupIdField.getText());
                int fromUserId = Integer.parseInt(fromUserIdField.getText());
                int toUserId = Integer.parseInt(toUserIdField.getText());
                double amount = Double.parseDouble(amountField.getText());
                Settlement settlement = new Settlement(groupId, fromUserId, toUserId, amount);
                settlementDAO.addSettlement(settlement);
                displayArea.setText("Group payment recorded successfully.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid input.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void viewBalancesAction(JTextArea displayArea) {
        String groupIdStr = JOptionPane.showInputDialog(this, "Enter Group ID to view balances:");
        if (groupIdStr != null && !groupIdStr.trim().isEmpty()) {
            try {
                int groupId = Integer.parseInt(groupIdStr);
                Map<Integer, Double> balances = balanceService.calculateBalances(groupId);
                StringBuilder sb = new StringBuilder("--- Current Balances ---\n");
                balances.forEach((userId, balance) -> {
                    User u = userDAO.getUserById(userId);
                    String name = (u != null) ? u.getName() : "Unknown";
                    if (balance > 0.01) {
                        sb.append(String.format("%s is owed ₹%.2f\n", name, balance));
                    } else if (balance < -0.01) {
                        sb.append(String.format("%s owes ₹%.2f\n", name, -balance));
                    } else {
                        sb.append(String.format("%s is settled up.\n", name));
                    }
                });
                sb.append("\n--- Suggested Settlements ---\n");
                List<SettlementService.Transaction> transactions = settlementService.settleUp(balances, userDAO);
                if (transactions.isEmpty()) {
                    sb.append("Everyone is settled up!");
                } else {
                    transactions.forEach(t -> sb.append(t.toString()).append("\n"));
                }
                displayArea.setText(sb.toString());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid Group ID.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void viewTransactionHistoryAction(JTextArea displayArea) {
        String groupIdStr = JOptionPane.showInputDialog(this, "Enter Group ID for history:");
        if (groupIdStr != null && !groupIdStr.trim().isEmpty()) {
            try {
                int groupId = Integer.parseInt(groupIdStr);
                List<Expense> expenses = expenseDAO.getExpensesByGroup(groupId);
                StringBuilder sb = new StringBuilder("--- Transaction History for Group " + groupId + " ---\n");
                if (expenses.isEmpty()) {
                    sb.append("No transactions found.");
                } else {
                    expenses.forEach(ex -> {
                        User payer = userDAO.getUserById(ex.getPaidByUserId());
                        String payerName = (payer != null) ? payer.getName() : "Unknown";
                        sb.append(String.format("ID: %d, Payer: %s, Amount: ₹%.2f, Desc: %s, Category: %s\n", ex.getExpenseId(), payerName, ex.getAmount(), ex.getDescription(), ex.getCategory()));
                    });
                }
                displayArea.setText(sb.toString());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid Group ID.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshUserTable() {
        if (userTableModel == null) {
            return;
        }
        userTableModel.setRowCount(0);
        List<User> users = userDAO.getAllUsers();
        users.forEach(u -> userTableModel.addRow(new Object[]{u.getUserId(), u.getName(), u.getEmail()}));
    }

    private void refreshGroupTable() {
        if (groupTableModel == null) {
            return;
        }
        groupTableModel.setRowCount(0);
        List<ExpenseGroup> groups = groupDAO.getAllGroups();
        groups.forEach(g -> groupTableModel.addRow(new Object[]{g.getGroupId(), g.getGroupName()}));
    }

    public static void main(String[] args) {
        UITheme.applyTheme();
        SwingUtilities.invokeLater(() -> {
            ExpenseAppGUI gui = new ExpenseAppGUI();
            gui.setVisible(true);
        });
    }

    // Small custom renderer for list items (kept simple)
    static class TransactionCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            lbl.setOpaque(true);
            lbl.setBorder(new EmptyBorder(6, 8, 6, 8));
            lbl.setBackground(isSelected ? UITheme.NAV_HOVER : UITheme.CARD_BG);
            lbl.setForeground(UITheme.TEXT_LIGHT);
            return lbl;
        }
    }
}
