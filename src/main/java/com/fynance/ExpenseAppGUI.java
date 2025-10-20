package com.fynance;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import com.formdev.flatlaf.FlatDarkLaf;
import com.fynance.analytics.AnalyticsPanel;

public class ExpenseAppGUI extends JFrame {

    // DAOs and Services
    private final UserDAO userDAO;
    private final GroupDAO groupDAO;
    private final GroupMemberDAO groupMemberDAO;
    private final ExpenseDAO expenseDAO;
    private final PersonalTransactionDAO personalTransactionDAO;
    private final BudgetDAO budgetDAO;
    private final CategoryDAO categoryDAO;
    private final BalanceService balanceService;
    private final SettlementService settlementService;

    // Main UI Components
    private final CardLayout cardLayout;
    private final JPanel contentPanel;
    private final ButtonGroup navButtonGroup = new ButtonGroup();
    private final int currentUserId = 1;

    // Models for dynamic data
    private DefaultListModel<Object> recentTransactionListModel;
    private DefaultListModel<String> duesListModel;
    private DefaultListModel<ExpenseGroup> groupListModel;

    private GroupDetailsPanel groupDetailsPanel;
    private PieChartPanel pieChartPanel;
    private BarChartPanel barChartPanel;
    private AnalyticsPanel analyticsPanel;
    private JLabel budgetLabel, expenseLabel, balanceLabel;

    public ExpenseAppGUI() {
        userDAO = new UserDAO();
        groupDAO = new GroupDAO();
        groupMemberDAO = new GroupMemberDAO();
        expenseDAO = new ExpenseDAO();
        personalTransactionDAO = new PersonalTransactionDAO();
        budgetDAO = new BudgetDAO();
        categoryDAO = new CategoryDAO();
        balanceService = new BalanceService();
        settlementService = new SettlementService();

        setTitle("Fynance — Personal Finance");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1600, 900);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.BACKGROUND_DARK);

        add(createHeader(), BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UITheme.BACKGROUND_DARK);

        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        contentPanel.add(createDashboardPanel(), "DASHBOARD");

        analyticsPanel = new AnalyticsPanel();
        contentPanel.add(analyticsPanel, "ANALYTICS");

        contentPanel.add(createGroupsPanel(), "GROUPS");
        contentPanel.add(new UserManagementPanel(userDAO, groupDAO, groupMemberDAO), "USERS");
        contentPanel.add(createSetupPanel(), "SETTINGS");

        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(12, 20, 12, 20));
        header.setBackground(UITheme.BACKGROUND_DARK);

        JLabel title = new JLabel("Fynance");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.ACCENT_PINK);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        User currentUser = userDAO.getUserById(currentUserId);
        String userName = (currentUser != null) ? currentUser.getName() : "User";
        String initials = (currentUser != null && !currentUser.getName().isEmpty()) ? currentUser.getName().substring(0, 1).toUpperCase() : "?";

        JLabel userLabel = new JLabel(userName);
        userLabel.setFont(UITheme.FONT_MAIN);
        userLabel.setForeground(UITheme.TEXT_LIGHT_GRAY);
        CircleAvatar avatar = new CircleAvatar(36, UITheme.ACCENT_BLUE_GRAY, initials);
        right.add(userLabel);
        right.add(avatar);

        header.add(title, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    public static class CircleAvatar extends JLabel {

        private final int size;
        private final Color bg;
        private final String text;

        public CircleAvatar(int size, Color bg, String text) {
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
        sidebar.setBackground(UITheme.BACKGROUND_DARK);
        sidebar.setBorder(new EmptyBorder(18, 14, 18, 14));
        sidebar.setPreferredSize(new Dimension(240, 0));

        // Ensure there is only one of each button
        addNavButton("Dashboard", "DASHBOARD", sidebar, true);
        addNavButton("Analytics", "ANALYTICS", sidebar, false);
        addNavButton("Groups", "GROUPS", sidebar, false);
        addNavButton("Users", "USERS", sidebar, false);
        addNavButton("Settings", "SETTINGS", sidebar, false);

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private void addNavButton(String title, String cardName, JPanel parent, boolean selected) {
        JToggleButton btn = new JToggleButton(title);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(UITheme.FONT_MAIN);
        btn.setForeground(UITheme.TEXT_LIGHT_GRAY);
        btn.setBackground(UITheme.BACKGROUND_DARK);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(6, 10, 6, 10));
        btn.setSelected(selected);
        navButtonGroup.add(btn);
        btn.addChangeListener(e -> {
            if (btn.isSelected()) {
                btn.setBackground(UITheme.PRIMARY_NAVY);
                btn.setForeground(UITheme.ACCENT_PINK);
            } else {
                btn.setBackground(UITheme.BACKGROUND_DARK);
                btn.setForeground(UITheme.TEXT_LIGHT_GRAY);
            }
        });
        btn.addActionListener(e -> cardLayout.show(contentPanel, cardName));
        parent.add(btn);
        parent.add(Box.createRigidArea(new Dimension(0, 8)));
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(UITheme.BACKGROUND_DARK);

        JPanel topPanel = new JPanel(new GridLayout(1, 4, 20, 20));
        topPanel.setOpaque(false);
        budgetLabel = new JLabel("₹0.00");
        expenseLabel = new JLabel("₹0.00");
        balanceLabel = new JLabel("₹0.00");
        topPanel.add(createBudgetCard("Total Budget", budgetLabel));
        topPanel.add(createMetricCard("Current Expenses", expenseLabel));
        topPanel.add(createMetricCard("Remaining Budget", balanceLabel));

        JButton addExpenseButton = new JButton("Add Personal Expense");
        addExpenseButton.addActionListener(e -> addPersonalTransactionAction());
        RoundedPanel addExpensePanel = new RoundedPanel(15);
        addExpensePanel.setLayout(new GridLayout(1, 1));
        addExpensePanel.add(addExpenseButton);
        topPanel.add(addExpensePanel);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 20));
        centerPanel.setOpaque(false);

        JPanel leftColumn = new JPanel(new BorderLayout(10, 10));
        leftColumn.setOpaque(false);
        recentTransactionListModel = new DefaultListModel<>();
        JList<Object> recentTransactionList = new JList<>(recentTransactionListModel);
        TransactionCellRenderer renderer = new TransactionCellRenderer(this);
        recentTransactionList.setCellRenderer(renderer);
        recentTransactionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = recentTransactionList.locationToIndex(e.getPoint());
                if (index != -1) {
                    Object item = recentTransactionList.getModel().getElementAt(index);
                    if (item instanceof PersonalTransaction t) {
                        Component c = renderer.getListCellRendererComponent(recentTransactionList, item, index, false, false);
                        Rectangle bounds = recentTransactionList.getCellBounds(index, index);
                        c.setBounds(bounds);
                        Point translatedPoint = new Point(e.getX() - bounds.x, e.getY() - bounds.y);
                        if (renderer.getEditButtonBounds().contains(translatedPoint)) {
                            editTransactionAction(t);
                        } else if (renderer.getDeleteButtonBounds().contains(translatedPoint)) {
                            deleteTransactionAction(t);
                        }
                    }
                }
            }
        });
        JScrollPane transactionScrollPane = new JScrollPane(recentTransactionList);
        transactionScrollPane.setBorder(BorderFactory.createTitledBorder("Recent Activity"));
        transactionScrollPane.getViewport().setBackground(UITheme.PRIMARY_NAVY);
        leftColumn.add(transactionScrollPane, BorderLayout.CENTER);

        centerPanel.add(leftColumn);

        JPanel rightColumn = new JPanel(new GridLayout(2, 1, 20, 20));
        rightColumn.setOpaque(false);
        pieChartPanel = new PieChartPanel();
        RoundedPanel pieChartContainer = new RoundedPanel(15);
        pieChartContainer.setLayout(new BorderLayout());
        pieChartContainer.setBackground(UITheme.PRIMARY_NAVY);
        pieChartContainer.setBorder(BorderFactory.createTitledBorder("Expense Categories"));
        pieChartContainer.add(pieChartPanel, BorderLayout.CENTER);
        rightColumn.add(pieChartContainer);

        duesListModel = new DefaultListModel<>();
        JList<String> duesList = new JList<>(duesListModel);
        duesList.setBackground(UITheme.PRIMARY_NAVY);
        duesList.setForeground(UITheme.TEXT_LIGHT_GRAY);
        JScrollPane duesScrollPane = new JScrollPane(duesList);
        RoundedPanel duesPanel = new RoundedPanel(15);
        duesPanel.setLayout(new BorderLayout());
        duesPanel.setBackground(UITheme.PRIMARY_NAVY);
        duesPanel.setBorder(BorderFactory.createTitledBorder("Group Dues Summary"));
        duesPanel.add(duesScrollPane, BorderLayout.CENTER);
        rightColumn.add(duesPanel);

        centerPanel.add(rightColumn);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);

        refreshDashboard();
        return panel;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel) {
        RoundedPanel card = new RoundedPanel(15);
        card.setBackground(UITheme.PRIMARY_NAVY);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.FONT_MAIN);
        titleLabel.setForeground(UITheme.TEXT_LIGHT_GRAY);
        valueLabel.setFont(UITheme.FONT_HEADER);
        valueLabel.setForeground(UITheme.ACCENT_PINK);
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createBudgetCard(String title, JLabel valueLabel) {
        JPanel card = createMetricCard(title, valueLabel);
        JButton editButton = new JButton("Edit");
        editButton.addActionListener(e -> setBudgetAction());
        editButton.setFocusPainted(false);
        editButton.setBorderPainted(false);
        card.add(editButton, BorderLayout.EAST);
        return card;
    }

    private JPanel createGroupsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(UITheme.BACKGROUND_DARK);

        RoundedPanel groupListPanel = new RoundedPanel(15);
        groupListPanel.setBackground(UITheme.PRIMARY_NAVY);
        groupListPanel.setLayout(new BorderLayout());
        groupListPanel.setBorder(BorderFactory.createTitledBorder("Groups"));

        groupListModel = new DefaultListModel<>();
        JList<ExpenseGroup> groupList = new JList<>(groupListModel);
        groupList.setBackground(UITheme.PRIMARY_NAVY);
        groupList.setForeground(UITheme.TEXT_LIGHT_GRAY);
        groupList.setFont(UITheme.FONT_MAIN);
        groupList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ExpenseGroup group) {
                    label.setText(group.getGroupName());
                    label.setBorder(new EmptyBorder(10, 15, 10, 15));
                }
                if (isSelected) {
                    label.setBackground(UITheme.BACKGROUND_DARK);
                    label.setForeground(UITheme.ACCENT_PINK);
                } else {
                    label.setBackground(UITheme.PRIMARY_NAVY);
                    label.setForeground(UITheme.TEXT_LIGHT_GRAY);
                }
                return label;
            }
        });
        refreshGroupListOnGroupsPage();

        JScrollPane groupListScrollPane = new JScrollPane(groupList);
        groupListScrollPane.setBorder(BorderFactory.createEmptyBorder());
        groupListPanel.add(groupListScrollPane, BorderLayout.CENTER);

        JPanel groupActionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        groupActionsPanel.setOpaque(false);
        JButton addGroupButton = new JButton("Add");
        JButton editGroupButton = new JButton("Edit");
        JButton deleteGroupButton = new JButton("Delete");
        groupActionsPanel.add(addGroupButton);
        groupActionsPanel.add(editGroupButton);
        groupActionsPanel.add(deleteGroupButton);
        groupListPanel.add(groupActionsPanel, BorderLayout.SOUTH);

        groupDetailsPanel = new GroupDetailsPanel(this, userDAO, groupDAO, groupMemberDAO, expenseDAO, balanceService, settlementService);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, groupListPanel, groupDetailsPanel);
        splitPane.setDividerLocation(250);
        splitPane.setDividerSize(5);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        panel.add(splitPane, BorderLayout.CENTER);

        addGroupButton.addActionListener(e -> {
            String groupName = JOptionPane.showInputDialog(this, "Enter new group name:", "Add Group", JOptionPane.PLAIN_MESSAGE);
            if (groupName != null && !groupName.trim().isEmpty()) {
                groupDAO.addGroup(new ExpenseGroup(groupName));
                refreshGroupListOnGroupsPage();
            }
        });

        editGroupButton.addActionListener(e -> {
            ExpenseGroup selectedGroup = groupList.getSelectedValue();
            if (selectedGroup == null) {
                JOptionPane.showMessageDialog(this, "Please select a group to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String newName = JOptionPane.showInputDialog(this, "Enter new name for the group:", selectedGroup.getGroupName());
            if (newName != null && !newName.trim().isEmpty()) {
                selectedGroup.setGroupName(newName);
                groupDAO.updateGroup(selectedGroup);
                refreshGroupListOnGroupsPage();
            }
        });

        deleteGroupButton.addActionListener(e -> {
            ExpenseGroup selectedGroup = groupList.getSelectedValue();
            if (selectedGroup == null) {
                JOptionPane.showMessageDialog(this, "Please select a group to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete the group '" + selectedGroup.getGroupName() + "'?\nAll associated expenses and data will be permanently removed.",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                groupDAO.deleteGroup(selectedGroup.getGroupId());
                refreshGroupListOnGroupsPage();
                groupDetailsPanel.clearPanel();
            }
        });

        groupList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                ExpenseGroup selectedGroup = groupList.getSelectedValue();
                groupDetailsPanel.loadGroup(selectedGroup);
            }
        });

        return panel;
    }

    private JPanel createSetupPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(UITheme.BACKGROUND_DARK);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        RoundedPanel appearancePanel = new RoundedPanel(15);
        appearancePanel.setBackground(UITheme.PRIMARY_NAVY);
        appearancePanel.setLayout(new BoxLayout(appearancePanel, BoxLayout.Y_AXIS));
        appearancePanel.setBorder(BorderFactory.createTitledBorder("Appearance"));
        appearancePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel themeLabel = new JLabel("Theme");
        themeLabel.setFont(UITheme.FONT_BOLD);
        themeLabel.setForeground(UITheme.TEXT_LIGHT_GRAY);
        themeLabel.setBorder(new EmptyBorder(10, 10, 5, 10));

        JRadioButton lightThemeButton = new JRadioButton("Light");
        JRadioButton darkThemeButton = new JRadioButton("Dark");
        lightThemeButton.setOpaque(false);
        darkThemeButton.setOpaque(false);
        lightThemeButton.setForeground(UITheme.TEXT_LIGHT_GRAY);
        darkThemeButton.setForeground(UITheme.TEXT_LIGHT_GRAY);

        ButtonGroup themeGroup = new ButtonGroup();
        themeGroup.add(lightThemeButton);
        themeGroup.add(darkThemeButton);

        if (UIManager.getLookAndFeel() instanceof FlatDarkLaf) {
            darkThemeButton.setSelected(true);
        } else {
            lightThemeButton.setSelected(true);
        }

        lightThemeButton.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                UITheme.applyLightTheme();
                UITheme.updateRootPane(this);
            }
        });

        darkThemeButton.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                UITheme.applyDarkTheme();
                UITheme.updateRootPane(this);
            }
        });

        JPanel themeRadioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        themeRadioPanel.setOpaque(false);
        themeRadioPanel.add(lightThemeButton);
        themeRadioPanel.add(darkThemeButton);
        themeRadioPanel.setBorder(new EmptyBorder(0, 5, 10, 5));

        appearancePanel.add(themeLabel);
        appearancePanel.add(themeRadioPanel);
        appearancePanel.add(Box.createVerticalGlue());

        RoundedPanel categoryPanel = new RoundedPanel(15);
        categoryPanel.setBackground(UITheme.PRIMARY_NAVY);
        categoryPanel.setLayout(new BorderLayout(10, 10));
        categoryPanel.setBorder(BorderFactory.createTitledBorder("Manage Categories"));

        DefaultListModel<Category> categoryListModel = new DefaultListModel<>();
        JList<Category> categoryList = new JList<>(categoryListModel);
        categoryList.setBackground(UITheme.PRIMARY_NAVY);
        categoryList.setForeground(UITheme.TEXT_LIGHT_GRAY);

        Runnable refreshCategories = () -> {
            categoryListModel.clear();
            categoryDAO.getAllCategories().forEach(categoryListModel::addElement);
        };
        refreshCategories.run();

        categoryPanel.add(new JScrollPane(categoryList), BorderLayout.CENTER);

        JPanel categoryButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        categoryButtons.setOpaque(false);
        JButton addCatBtn = new JButton("Add");
        JButton editCatBtn = new JButton("Edit");
        JButton deleteCatBtn = new JButton("Delete");
        categoryButtons.add(addCatBtn);
        categoryButtons.add(editCatBtn);
        categoryButtons.add(deleteCatBtn);
        categoryPanel.add(categoryButtons, BorderLayout.SOUTH);

        addCatBtn.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Enter category name:", "Add Category", JOptionPane.PLAIN_MESSAGE);
            if (name != null && !name.trim().isEmpty()) {
                try {
                    categoryDAO.addCategory(name);
                    refreshCategories.run();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Category already exists or database error.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        editCatBtn.addActionListener(e -> {
            Category selected = categoryList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Please select a category to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String newName = JOptionPane.showInputDialog(this, "Enter new name:", selected.getName());
            if (newName != null && !newName.trim().isEmpty()) {
                selected.setName(newName);
                try {
                    categoryDAO.updateCategory(selected);
                    refreshCategories.run();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error updating category.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        deleteCatBtn.addActionListener(e -> {
            Category selected = categoryList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Please select a category to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (categoryDAO.isCategoryInUse(selected.getCategoryId())) {
                JOptionPane.showMessageDialog(this, "Cannot delete category as it is currently in use.", "Deletion Blocked", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "Delete category '" + selected.getName() + "'?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    categoryDAO.deleteCategory(selected.getCategoryId());
                    refreshCategories.run();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error deleting category.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, appearancePanel, categoryPanel);
        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(0.2);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);

        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    public void refreshDashboard() {
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
            balanceLabel.setForeground(remaining >= 0 ? UITheme.ACCENT_PINK : Color.RED);
        } else {
            budgetLabel.setText("Not Set");
            balanceLabel.setText("N/A");
        }

        recentTransactionListModel.clear();
        List<PersonalTransaction> allTransactions = personalTransactionDAO.getAllTransactionsForUser(currentUserId);
        allTransactions.sort(Comparator.comparing(PersonalTransaction::getTransactionDate).reversed());
        Map<LocalDate, List<PersonalTransaction>> groupedByDate = allTransactions.stream()
                .collect(Collectors.groupingBy(PersonalTransaction::getTransactionDate, LinkedHashMap::new, Collectors.toList()));
        for (Map.Entry<LocalDate, List<PersonalTransaction>> entry : groupedByDate.entrySet()) {
            recentTransactionListModel.addElement(entry.getKey());
            entry.getValue().forEach(recentTransactionListModel::addElement);
        }

        Map<String, Double> expenseData = monthlyTransactions.stream().filter(t -> "EXPENSE".equals(t.getType()))
                .collect(Collectors.groupingBy(t -> {
                    Category c = categoryDAO.getCategoryById(t.getCategoryId());
                    return c != null ? c.getName() : "Uncategorized";
                }, Collectors.summingDouble(PersonalTransaction::getAmount)));
        pieChartPanel.setData(expenseData);

        duesListModel.clear();
        List<ExpenseGroup> groups = groupDAO.getAllGroups();
        for (ExpenseGroup group : groups) {
            Map<Integer, Double> balances = balanceService.calculateBalances(group.getGroupId());
            balances.forEach((userId, balance) -> {
                User u = userDAO.getUserById(userId);
                String name = (u != null) ? u.getName() : "Unknown";
                if (balance > 0.01 && userId != currentUserId) {
                    duesListModel.addElement(String.format("%s owes you ₹%.2f in '%s'", name, balance, group.getGroupName()));
                } else if (balance < -0.01 && userId == currentUserId) {
                    balances.forEach((otherUserId, otherBalance) -> {
                        if (otherBalance > 0.01) {
                            User creditor = userDAO.getUserById(otherUserId);
                            if (creditor != null) {
                                duesListModel.addElement(String.format("You owe %s ₹%.2f in '%s'", creditor.getName(), -balance, group.getGroupName()));
                            }
                        }
                    });
                }
            });
        }
        if (duesListModel.isEmpty()) {
            duesListModel.addElement("All group dues are settled!");
        }

        if (barChartPanel != null) {
            Map<String, Double> historicalData = personalTransactionDAO.getMonthlyExpenseSummary(currentUserId, 6);
            barChartPanel.setData(historicalData);
        }
    }

    public void addPersonalTransactionAction() {
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"EXPENSE", "INCOME", "SAVING"});
        JTextField amountField = new JTextField();
        JTextArea notesArea = new JTextArea(3, 20);

        JComboBox<Category> categoryComboBox = new JComboBox<>();
        DefaultComboBoxModel<Category> categoryModel = new DefaultComboBoxModel<>();
        categoryDAO.getAllCategories().forEach(categoryModel::addElement);
        categoryComboBox.setModel(categoryModel);

        Object[] message = {
            "Type:", typeCombo,
            "Amount:", amountField,
            "Category:", categoryComboBox,
            "Notes:", new JScrollPane(notesArea)
        };
        int option = JOptionPane.showConfirmDialog(this, message, "Add Personal Transaction", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String type = (String) typeCombo.getSelectedItem();
                double amount = Double.parseDouble(amountField.getText());
                Category selectedCategory = (Category) categoryComboBox.getSelectedItem();
                String notes = notesArea.getText();
                if (amount <= 0 || selectedCategory == null) {
                    throw new IllegalArgumentException("Invalid input");
                }

                PersonalTransaction trans = new PersonalTransaction(currentUserId, type, amount, selectedCategory.getCategoryId(), LocalDate.now(), notes);
                personalTransactionDAO.addTransaction(trans);
                refreshDashboard();
                analyticsPanel.refreshAnalyticsData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input. Please check amount and category.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void editTransactionAction(PersonalTransaction transaction) {
        JTextField amountField = new JTextField(String.valueOf(transaction.getAmount()));
        JTextArea notesArea = new JTextArea(transaction.getNotes(), 3, 20);

        JComboBox<Category> categoryComboBox = new JComboBox<>();
        DefaultComboBoxModel<Category> categoryModel = new DefaultComboBoxModel<>();
        List<Category> categories = categoryDAO.getAllCategories();
        categories.forEach(categoryModel::addElement);
        categoryComboBox.setModel(categoryModel);

        for (Category category : categories) {
            if (category.getCategoryId() == transaction.getCategoryId()) {
                categoryComboBox.setSelectedItem(category);
                break;
            }
        }

        Object[] message = {"Amount:", amountField, "Category:", categoryComboBox, "Notes:", new JScrollPane(notesArea)};
        int option = JOptionPane.showConfirmDialog(this, message, "Edit Transaction", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                double amount = Double.parseDouble(amountField.getText());
                Category selectedCategory = (Category) categoryComboBox.getSelectedItem();
                if (amount <= 0 || selectedCategory == null) {
                    throw new IllegalArgumentException("Invalid input");
                }
                transaction.setAmount(amount);
                transaction.setCategoryId(selectedCategory.getCategoryId());
                transaction.setNotes(notesArea.getText());
                personalTransactionDAO.updateTransaction(transaction);
                refreshDashboard();
                analyticsPanel.refreshAnalyticsData();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "Invalid input.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void deleteTransactionAction(PersonalTransaction transaction) {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this transaction?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            personalTransactionDAO.deleteTransaction(transaction.getTransactionId());
            refreshDashboard();
            analyticsPanel.refreshAnalyticsData();
        }
    }

    private void setBudgetAction() {
        String budgetStr = JOptionPane.showInputDialog(this, "Enter budget for the current month:");
        if (budgetStr != null && !budgetStr.trim().isEmpty()) {
            try {
                double budgetAmount = Double.parseDouble(budgetStr);
                Budget budget = new Budget(currentUserId, LocalDate.now().getMonthValue(), LocalDate.now().getYear(), budgetAmount);
                budgetDAO.setBudget(budget);
                refreshDashboard();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid budget amount.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void refreshGroupListOnGroupsPage() {
        if (groupListModel == null) {
            return;
        }
        ExpenseGroup selected = groupDetailsPanel != null ? groupDetailsPanel.getCurrentGroup() : null;
        groupListModel.clear();
        List<ExpenseGroup> groups = groupDAO.getAllGroups();
        groups.forEach(groupListModel::addElement);

        if (selected != null) {
            for (int i = 0; i < groupListModel.size(); i++) {
                if (groupListModel.get(i).getGroupId() == selected.getGroupId()) {
                    @SuppressWarnings("unchecked")
                    JList<ExpenseGroup> groupList = (JList<ExpenseGroup>) ((JScrollPane) ((RoundedPanel) ((JSplitPane) contentPanel.getComponent(2)).getLeftComponent()).getComponent(0)).getViewport().getView();
                    groupList.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    public static void main(String[] args) {
        UITheme.applyLightTheme();
        SwingUtilities.invokeLater(() -> new ExpenseAppGUI().setVisible(true));
    }

    class TransactionCellRenderer extends DefaultListCellRenderer {

        private final JPanel panel;
        private final JLabel categoryLabel, amountLabel, dateLabel;
        private final JButton editButton, deleteButton;
        private final JPanel buttonsPanel;
        private final Rectangle editButtonBounds = new Rectangle();
        private final Rectangle deleteButtonBounds = new Rectangle();

        public TransactionCellRenderer(ExpenseAppGUI gui) {
            panel = new JPanel(new BorderLayout(15, 0));
            panel.setBorder(new EmptyBorder(10, 15, 10, 15));
            categoryLabel = new JLabel();
            categoryLabel.setFont(UITheme.FONT_MAIN);
            categoryLabel.setForeground(UITheme.TEXT_LIGHT_GRAY);
            amountLabel = new JLabel();
            amountLabel.setFont(UITheme.FONT_BOLD);
            dateLabel = new JLabel();
            dateLabel.setFont(UITheme.FONT_BOLD);
            dateLabel.setForeground(UITheme.ACCENT_PINK);
            buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            buttonsPanel.setOpaque(false);
            editButton = createStyledButton("Edit");
            deleteButton = createStyledButton("Delete");
            buttonsPanel.add(editButton);
            buttonsPanel.add(deleteButton);
        }

        private JButton createStyledButton(String text) {
            JButton button = new JButton(text);
            button.setFont(UITheme.FONT_MAIN.deriveFont(12f));
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.ACCENT_BLUE_GRAY),
                    new EmptyBorder(4, 8, 4, 8)
            ));
            button.setBackground(UITheme.PRIMARY_NAVY);
            button.setForeground(UITheme.TEXT_LIGHT_GRAY);
            return button;
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            panel.removeAll();
            panel.setBackground(isSelected ? UITheme.BACKGROUND_DARK : UITheme.PRIMARY_NAVY);

            if (value instanceof PersonalTransaction t) {
                Category category = categoryDAO.getCategoryById(t.getCategoryId());
                String categoryName = (category != null) ? category.getName() : "Uncategorized";

                categoryLabel.setText(String.format("<html><body style='width: 200px'><b>%s</b><br><font color='#8EB69B'>%s</font></body></html>", categoryName, t.getNotes()));
                String sign = "";
                Color amountColor = UITheme.TEXT_LIGHT_GRAY;
                switch (t.getType()) {
                    case "INCOME" -> {
                        amountColor = UITheme.ACCENT_PINK;
                        sign = "+";
                    }
                    case "EXPENSE" -> {
                        amountColor = UITheme.ACCENT_BLUE;
                        sign = "-";
                    }
                    case "SAVING" ->
                        amountColor = UITheme.ACCENT_BLUE_GRAY;
                }
                amountLabel.setText(String.format("%s ₹%.2f", sign, t.getAmount()));
                amountLabel.setForeground(amountColor);

                panel.add(amountLabel, BorderLayout.WEST);
                panel.add(categoryLabel, BorderLayout.CENTER);
                panel.add(buttonsPanel, BorderLayout.EAST);

                Dimension bpSize = buttonsPanel.getPreferredSize();
                int pWidth = list.getWidth() > 0 ? list.getWidth() : 500;
                int pHeight = panel.getPreferredSize().height;
                int bpX = pWidth - bpSize.width - 15;
                int bpY = (pHeight - bpSize.height) / 2;
                Dimension editSize = editButton.getPreferredSize();
                Dimension deleteSize = deleteButton.getPreferredSize();
                editButtonBounds.setBounds(bpX, bpY, editSize.width, editSize.height);
                deleteButtonBounds.setBounds(bpX + editSize.width + 8, bpY, deleteSize.width, deleteSize.height);

            } else if (value instanceof LocalDate date) {
                dateLabel.setText(date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy, EEEE")));
                panel.setBackground(UITheme.BACKGROUND_DARK);
                panel.add(dateLabel, BorderLayout.CENTER);
            }
            return panel;
        }

        public Rectangle getEditButtonBounds() {
            return editButtonBounds;
        }

        public Rectangle getDeleteButtonBounds() {
            return deleteButtonBounds;
        }
    }

    static class ModernTableHeaderRenderer extends JPanel implements TableCellRenderer {

        private final TableCellRenderer originalRenderer;

        public ModernTableHeaderRenderer(TableCellRenderer originalRenderer) {
            this.originalRenderer = originalRenderer;
            setLayout(new BorderLayout());
            setOpaque(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component component = originalRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (component instanceof JLabel label) {
                label.setBorder(new EmptyBorder(5, 10, 5, 10));
                label.setFont(UITheme.FONT_BOLD);
                label.setForeground(UITheme.ACCENT_PINK);
            }
            return component;
        }
    }
}
