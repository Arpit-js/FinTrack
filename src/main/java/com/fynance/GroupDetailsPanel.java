package com.fynance;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class GroupDetailsPanel extends JPanel {

    private final ExpenseAppGUI mainFrame;
    private final UserDAO userDAO;
    private final GroupDAO groupDAO;
    private final GroupMemberDAO groupMemberDAO;
    private final ExpenseDAO expenseDAO;
    private final BalanceService balanceService;
    private final SettlementService settlementService;

    private ExpenseGroup currentGroup;
    private JTabbedPane tabbedPane;
    private JTable expensesTable;
    private DefaultTableModel expensesTableModel;
    private JTextArea balancesArea;
    private JList<User> membersList;
    private DefaultListModel<User> membersListModel;

    public GroupDetailsPanel(ExpenseAppGUI mainFrame, UserDAO userDAO, GroupDAO groupDAO, GroupMemberDAO groupMemberDAO, ExpenseDAO expenseDAO, BalanceService balanceService, SettlementService settlementService) {
        this.mainFrame = mainFrame;
        this.userDAO = userDAO;
        this.groupDAO = groupDAO;
        this.groupMemberDAO = groupMemberDAO;
        this.expenseDAO = expenseDAO;
        this.balanceService = balanceService;
        this.settlementService = settlementService;

        setLayout(new BorderLayout());
        setOpaque(false);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_MAIN);
        tabbedPane.setBackground(UITheme.PRIMARY_NAVY);
        tabbedPane.setForeground(UITheme.TEXT_LIGHT_GRAY);

        tabbedPane.addTab("Expenses", createExpensesPanel());
        tabbedPane.addTab("Balances", createBalancesPanel());
        tabbedPane.addTab("Members", createMembersPanel());

        RoundedPanel mainCard = new RoundedPanel(15);
        mainCard.setLayout(new BorderLayout());
        mainCard.setBackground(UITheme.PRIMARY_NAVY);
        mainCard.add(tabbedPane, BorderLayout.CENTER);

        add(mainCard, BorderLayout.CENTER);
    }

    private JPanel createExpensesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        expensesTableModel = new DefaultTableModel(new String[]{"ID", "Description", "Amount", "Paid By", "Category"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        expensesTable = new JTable(expensesTableModel);
        JScrollPane scrollPane = new JScrollPane(expensesTable);

        expensesTable.getTableHeader().setDefaultRenderer(new ExpenseAppGUI.ModernTableHeaderRenderer(expensesTable.getTableHeader().getDefaultRenderer()));
        expensesTable.setRowHeight(30);
        expensesTable.setShowGrid(false);
        expensesTable.setIntercellSpacing(new Dimension(0, 0));
        expensesTable.setBackground(UITheme.PRIMARY_NAVY);
        expensesTable.setForeground(UITheme.TEXT_LIGHT_GRAY);
        expensesTable.setFont(UITheme.FONT_MAIN);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(UITheme.PRIMARY_NAVY);

        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        JButton addExpenseButton = new JButton("Add Expense");
        JButton editExpenseButton = new JButton("Edit Expense");
        JButton deleteExpenseButton = new JButton("Delete Expense");

        addExpenseButton.addActionListener(e -> addExpenseAction());
        editExpenseButton.addActionListener(e -> editExpenseAction());
        deleteExpenseButton.addActionListener(e -> deleteExpenseAction());

        buttonPanel.add(addExpenseButton);
        buttonPanel.add(editExpenseButton);
        buttonPanel.add(deleteExpenseButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createBalancesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        balancesArea = new JTextArea();
        balancesArea.setEditable(false);
        balancesArea.setBackground(UITheme.PRIMARY_NAVY);
        balancesArea.setForeground(UITheme.TEXT_LIGHT_GRAY);
        balancesArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        balancesArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(balancesArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createMembersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        membersListModel = new DefaultListModel<>();
        membersList = new JList<>(membersListModel);
        membersList.setBackground(UITheme.PRIMARY_NAVY);
        membersList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JPanel cellPanel = new JPanel(new BorderLayout());
                cellPanel.setBorder(new EmptyBorder(8, 12, 8, 12));

                if (value instanceof User user) {
                    ExpenseAppGUI.CircleAvatar avatar = new ExpenseAppGUI.CircleAvatar(32, UITheme.ACCENT_BLUE_GRAY, user.getName().substring(0, 1).toUpperCase());
                    JLabel nameLabel = new JLabel(String.format("<html><b>%s</b><br><font color='#8EB69B'>%s</font></html>", user.getName(), user.getEmail()));
                    nameLabel.setFont(UITheme.FONT_MAIN);
                    nameLabel.setBorder(new EmptyBorder(0, 10, 0, 0));

                    cellPanel.add(avatar, BorderLayout.WEST);
                    cellPanel.add(nameLabel, BorderLayout.CENTER);
                }

                if (isSelected) {
                    cellPanel.setBackground(UITheme.BACKGROUND_DARK);
                } else {
                    cellPanel.setBackground(UITheme.PRIMARY_NAVY);
                }
                return cellPanel;
            }
        });

        JScrollPane scrollPane = new JScrollPane(membersList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        JButton addMemberButton = new JButton("Add Member");
        JButton removeMemberButton = new JButton("Remove Member");
        addMemberButton.addActionListener(e -> addMemberAction());
        removeMemberButton.addActionListener(e -> removeMemberAction());
        buttonPanel.add(addMemberButton);
        buttonPanel.add(removeMemberButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    public void loadGroup(ExpenseGroup group) {
        this.currentGroup = group;
        refreshExpenses();
        refreshBalances();
        refreshMembers();
    }

    public void refreshGroupList() {
        // This method can be used to refresh the list of groups in the main frame
    }

    private void addExpenseAction() {
        if (currentGroup == null) {
            JOptionPane.showMessageDialog(this, "Please select a group first.", "No Group Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        AddExpenseDialog dialog = new AddExpenseDialog(mainFrame, currentGroup, userDAO, groupMemberDAO, expenseDAO);
        dialog.setVisible(true);
        refreshExpenses();
        refreshBalances();
    }

    private void editExpenseAction() {
        int selectedRow = expensesTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an expense to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int expenseId = (int) expensesTableModel.getValueAt(selectedRow, 0);
        Expense expense = expenseDAO.getExpenseById(expenseId);

        JTextField amountField = new JTextField(String.valueOf(expense.getAmount()));
        JTextField descriptionField = new JTextField(expense.getDescription());
        JTextField categoryField = new JTextField(expense.getCategory());
        Object[] message = {"Amount:", amountField, "Description:", descriptionField, "Category:", categoryField};
        int option = JOptionPane.showConfirmDialog(this, message, "Edit Expense " + expenseId, JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                expense.setAmount(Double.parseDouble(amountField.getText()));
                expense.setDescription(descriptionField.getText());
                expense.setCategory(categoryField.getText());
                expenseDAO.updateExpense(expense);
                refreshExpenses();
                refreshBalances();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid amount format.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteExpenseAction() {
        int selectedRow = expensesTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an expense to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int expenseId = (int) expensesTableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this expense?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            expenseDAO.deleteExpense(expenseId);
            refreshExpenses();
            refreshBalances();
        }
    }

    private void addMemberAction() {
        if (currentGroup == null) {
            return;
        }
        List<User> allUsers = userDAO.getAllUsers();
        List<GroupMember> groupMembers = groupMemberDAO.getMembersByGroup(currentGroup.getGroupId());
        List<Integer> memberIds = groupMembers.stream().map(GroupMember::getUserId).toList();

        List<User> nonMembers = allUsers.stream()
                .filter(u -> !memberIds.contains(u.getUserId()))
                .toList();

        if (nonMembers.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All users are already in this group.", "No Users to Add", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        User[] usersArray = nonMembers.toArray(new User[0]);
        User toAdd = (User) JOptionPane.showInputDialog(this, "Select a user to add to the group:",
                "Add Member", JOptionPane.QUESTION_MESSAGE, null, usersArray, usersArray[0]);

        if (toAdd != null) {
            groupMemberDAO.addMemberToGroup(currentGroup.getGroupId(), toAdd.getUserId());
            refreshMembers();
        }
    }

    private void removeMemberAction() {
        if (currentGroup == null) {
            return;
        }
        User selectedUser = membersList.getSelectedValue();
        if (selectedUser == null) {
            JOptionPane.showMessageDialog(this, "Please select a member to remove.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove " + selectedUser.getName() + " from the group?", "Confirm Removal", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            groupMemberDAO.removeMemberFromGroup(currentGroup.getGroupId(), selectedUser.getUserId());
            refreshMembers();
            refreshBalances();
        }
    }

    public void refreshExpenses() {
        if (currentGroup == null) {
            return;
        }
        expensesTableModel.setRowCount(0);
        List<Expense> expenses = expenseDAO.getExpensesByGroup(currentGroup.getGroupId());
        for (Expense expense : expenses) {
            User payer = userDAO.getUserById(expense.getPaidByUserId());
            expensesTableModel.addRow(new Object[]{
                expense.getExpenseId(),
                expense.getDescription(),
                String.format("₹%.2f", expense.getAmount()),
                payer != null ? payer.getName() : "Unknown",
                expense.getCategory()
            });
        }
    }

    public void refreshBalances() {
        if (currentGroup == null) {
            return;
        }
        Map<Integer, Double> balances = balanceService.calculateBalances(currentGroup.getGroupId());
        StringBuilder sb = new StringBuilder("--- Current Balances ---\n");
        balances.forEach((userId, balance) -> {
            User u = userDAO.getUserById(userId);
            String name = (u != null) ? u.getName() : "Unknown";
            if (balance > 0.01) {
                sb.append(String.format("%-20s is owed   ₹%8.2f\n", name, balance));
            } else if (balance < -0.01) {
                sb.append(String.format("%-20s owes     ₹%8.2f\n", name, -balance));
            }
        });
        sb.append("\n--- Suggested Settlements ---\n");
        List<SettlementService.Transaction> transactions = settlementService.settleUp(balances, userDAO);
        if (transactions.isEmpty()) {
            sb.append("Everyone is settled up!");
        } else {
            transactions.forEach(t -> sb.append(t.toString()).append("\n"));
        }
        balancesArea.setText(sb.toString());
    }
    // In GroupDetailsPanel.java

    public void clearPanel() {
        this.currentGroup = null;
        expensesTableModel.setRowCount(0);
        balancesArea.setText("");
        membersListModel.clear();
    }
    // In GroupDetailsPanel.java

    public ExpenseGroup getCurrentGroup() {
        return this.currentGroup;
    }

    public void refreshMembers() {
        if (currentGroup == null) {
            return;
        }
        membersListModel.clear();
        List<GroupMember> members = groupMemberDAO.getMembersByGroup(currentGroup.getGroupId());
        for (GroupMember member : members) {
            User user = userDAO.getUserById(member.getUserId());
            if (user != null) {
                membersListModel.addElement(user);
            }
        }
    }
}
