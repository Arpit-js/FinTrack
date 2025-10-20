package com.fynance;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class AddExpenseDialog extends JDialog {

    private final ExpenseGroup group;
    private final UserDAO userDAO;
    private final GroupMemberDAO groupMemberDAO;
    private final ExpenseDAO expenseDAO;

    private JComboBox<User> paidByComboBox;
    private JTextField descriptionField;
    private JTextField amountField;
    private JTextField categoryField;
    private JComboBox<String> splitTypeComboBox;
    private JPanel splitDetailsPanel;

    private List<GroupMember> members;
    private List<JTextField> splitFields;

    public AddExpenseDialog(Frame owner, ExpenseGroup group, UserDAO userDAO, GroupMemberDAO groupMemberDAO, ExpenseDAO expenseDAO) {
        super(owner, "Add Expense to " + group.getGroupName(), true);
        this.group = group;
        this.userDAO = userDAO;
        this.groupMemberDAO = groupMemberDAO;
        this.expenseDAO = expenseDAO;
        this.members = groupMemberDAO.getMembersByGroup(group.getGroupId());
        this.splitFields = new ArrayList<>();

        setLayout(new BorderLayout());
        setSize(500, 600);
        setLocationRelativeTo(owner);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        descriptionField = new JTextField();
        formPanel.add(descriptionField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Amount:"), gbc);
        gbc.gridx = 1;
        amountField = new JTextField();
        formPanel.add(amountField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        categoryField = new JTextField();
        formPanel.add(categoryField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Paid By:"), gbc);
        gbc.gridx = 1;
        paidByComboBox = new JComboBox<>();
        for (GroupMember member : members) {
            paidByComboBox.addItem(userDAO.getUserById(member.getUserId()));
        }
        formPanel.add(paidByComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Split Type:"), gbc);
        gbc.gridx = 1;
        splitTypeComboBox = new JComboBox<>(new String[]{"Equally", "Unequally", "By Percentage"});
        splitTypeComboBox.addActionListener(e -> updateSplitDetailsPanel());
        formPanel.add(splitTypeComboBox, gbc);

        add(formPanel, BorderLayout.NORTH);

        splitDetailsPanel = new JPanel(new GridBagLayout());
        add(new JScrollPane(splitDetailsPanel), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveExpense());
        buttonPanel.add(saveButton);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        updateSplitDetailsPanel();
    }

    private void updateSplitDetailsPanel() {
        splitDetailsPanel.removeAll();
        splitFields.clear();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        for (GroupMember member : members) {
            User user = userDAO.getUserById(member.getUserId());
            splitDetailsPanel.add(new JLabel(user.getName()), gbc);

            gbc.gridx = 1;
            JTextField field = new JTextField(10);
            splitFields.add(field);
            splitDetailsPanel.add(field, gbc);

            gbc.gridy++;
            gbc.gridx = 0;
        }

        revalidate();
        repaint();
    }

    private void saveExpense() {
        try {
            String description = descriptionField.getText();
            double amount = Double.parseDouble(amountField.getText());
            String category = categoryField.getText();
            User payer = (User) paidByComboBox.getSelectedItem();

            Expense expense = new Expense(group.getGroupId(), payer.getUserId(), amount, description, category);
            List<ExpenseSplit> splits = new ArrayList<>();
            String splitType = (String) splitTypeComboBox.getSelectedItem();

            if ("Equally".equals(splitType)) {
                double splitAmount = amount / members.size();
                for (GroupMember member : members) {
                    splits.add(new ExpenseSplit(0, member.getUserId(), splitAmount, null));
                }
            } else if ("Unequally".equals(splitType)) {
                double total = 0;
                for (int i = 0; i < members.size(); i++) {
                    double splitAmount = Double.parseDouble(splitFields.get(i).getText());
                    total += splitAmount;
                    splits.add(new ExpenseSplit(0, members.get(i).getUserId(), splitAmount, null));
                }
                if (Math.abs(total - amount) > 0.01) {
                    JOptionPane.showMessageDialog(this, "The sum of splits must equal the total amount.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else if ("By Percentage".equals(splitType)) {
                double totalPercentage = 0;
                for (int i = 0; i < members.size(); i++) {
                    double percentage = Double.parseDouble(splitFields.get(i).getText());
                    totalPercentage += percentage;
                    double splitAmount = amount * (percentage / 100.0);
                    splits.add(new ExpenseSplit(0, members.get(i).getUserId(), splitAmount, percentage));
                }
                if (Math.abs(totalPercentage - 100) > 0.01) {
                    JOptionPane.showMessageDialog(this, "The sum of percentages must be 100.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            expenseDAO.addExpenseWithSplits(expense, splits);
            dispose();
        } catch (NumberFormatException | SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error saving expense: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
