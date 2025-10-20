package com.fynance;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class UserManagementPanel extends JPanel {

    private final UserDAO userDAO;
    private final GroupDAO groupDAO;
    private final GroupMemberDAO groupMemberDAO;

    private JTable userTable;
    private DefaultTableModel userTableModel;
    private JList<String> userGroupsList;
    private DefaultListModel<String> userGroupsListModel;
    private JLabel userDetailsLabel;

    public UserManagementPanel(UserDAO userDAO, GroupDAO groupDAO, GroupMemberDAO groupMemberDAO) {
        this.userDAO = userDAO;
        this.groupDAO = groupDAO;
        this.groupMemberDAO = groupMemberDAO;

        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.BACKGROUND_DARK);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- Main User Table (Left/Center) ---
        RoundedPanel tablePanel = new RoundedPanel(15);
        tablePanel.setBackground(UITheme.PRIMARY_NAVY);
        tablePanel.setLayout(new BorderLayout(10, 10));
        tablePanel.setBorder(BorderFactory.createTitledBorder("All Users"));

        userTableModel = new DefaultTableModel(new String[]{"ID", "Name", "Email"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable = new JTable(userTableModel);
        styleTable(userTable);

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.getViewport().setBackground(UITheme.PRIMARY_NAVY);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // --- Action Buttons ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setOpaque(false);
        JButton addUserButton = new JButton("Add User");
        JButton editUserButton = new JButton("Edit User");
        JButton deleteUserButton = new JButton("Delete User");
        JButton assignToGroupButton = new JButton("Assign to Group");

        addUserButton.addActionListener(e -> addUserAction());
        editUserButton.addActionListener(e -> editUserAction());
        deleteUserButton.addActionListener(e -> deleteUserAction());
        assignToGroupButton.addActionListener(e -> assignToGroupAction());

        buttonPanel.add(addUserButton);
        buttonPanel.add(editUserButton);
        buttonPanel.add(deleteUserButton);
        buttonPanel.add(assignToGroupButton);
        tablePanel.add(buttonPanel, BorderLayout.SOUTH);

        // --- User Details Panel (Right) ---
        RoundedPanel detailsPanel = new RoundedPanel(15);
        detailsPanel.setBackground(UITheme.PRIMARY_NAVY);
        detailsPanel.setLayout(new BorderLayout(10, 10));
        detailsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        detailsPanel.setPreferredSize(new Dimension(300, 0));

        userDetailsLabel = new JLabel("Select a user to see details");
        userDetailsLabel.setFont(UITheme.FONT_HEADER.deriveFont(16f));
        userDetailsLabel.setForeground(UITheme.ACCENT_PINK);
        userDetailsLabel.setBorder(new EmptyBorder(5, 5, 10, 5));
        detailsPanel.add(userDetailsLabel, BorderLayout.NORTH);

        userGroupsListModel = new DefaultListModel<>();
        userGroupsList = new JList<>(userGroupsListModel);
        userGroupsList.setBackground(UITheme.PRIMARY_NAVY);
        userGroupsList.setForeground(UITheme.TEXT_LIGHT_GRAY);
        JScrollPane detailsScrollPane = new JScrollPane(userGroupsList);
        detailsScrollPane.setBorder(BorderFactory.createTitledBorder("Member Of"));
        detailsPanel.add(detailsScrollPane, BorderLayout.CENTER);

        // Add a selection listener to the table to update the details view
        userTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateUserDetails();
            }
        });

        // --- Main Layout ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tablePanel, detailsPanel);
        splitPane.setDividerLocation(800);
        splitPane.setResizeWeight(0.7);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        add(splitPane, BorderLayout.CENTER);

        refreshUserTable();
    }

    private void styleTable(JTable table) {
        table.getTableHeader().setDefaultRenderer(new ExpenseAppGUI.ModernTableHeaderRenderer(table.getTableHeader().getDefaultRenderer()));
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(UITheme.PRIMARY_NAVY);
        table.setForeground(UITheme.TEXT_LIGHT_GRAY);
        table.setFont(UITheme.FONT_MAIN);
    }

    private void refreshUserTable() {
        int selectedRow = userTable.getSelectedRow();
        userTableModel.setRowCount(0);
        List<User> users = userDAO.getAllUsers();
        for (User user : users) {
            userTableModel.addRow(new Object[]{user.getUserId(), user.getName(), user.getEmail()});
        }
        if (selectedRow >= 0 && selectedRow < userTable.getRowCount()) {
            userTable.setRowSelectionInterval(selectedRow, selectedRow);
        }
    }

    private void updateUserDetails() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow >= 0) {
            int userId = (int) userTableModel.getValueAt(selectedRow, 0);
            String name = (String) userTableModel.getValueAt(selectedRow, 1);
            userDetailsLabel.setText("Details for " + name);

            List<ExpenseGroup> groups = groupMemberDAO.getGroupsForUser(userId);
            userGroupsListModel.clear();
            if (groups.isEmpty()) {
                userGroupsListModel.addElement("Not a member of any group.");
            } else {
                groups.forEach(g -> userGroupsListModel.addElement(g.getGroupName()));
            }
        } else {
            userDetailsLabel.setText("Select a user to see details");
            userGroupsListModel.clear();
        }
    }

    private void addUserAction() {
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        Object[] message = {"Name:", nameField, "Email:", emailField};

        int option = JOptionPane.showConfirmDialog(this, message, "Add New User", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText();
            String email = emailField.getText();
            if (!name.trim().isEmpty() && !email.trim().isEmpty()) {
                userDAO.addUser(new User(name, email));
                refreshUserTable();
                JOptionPane.showMessageDialog(this, "User added successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Name and email cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editUserAction() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user to edit.", "No User Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (int) userTableModel.getValueAt(selectedRow, 0);
        String currentName = (String) userTableModel.getValueAt(selectedRow, 1);
        String currentEmail = (String) userTableModel.getValueAt(selectedRow, 2);

        JTextField nameField = new JTextField(currentName);
        JTextField emailField = new JTextField(currentEmail);
        Object[] message = {"Name:", nameField, "Email:", emailField};

        int option = JOptionPane.showConfirmDialog(this, message, "Edit User", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String newName = nameField.getText();
            String newEmail = emailField.getText();
            if (!newName.trim().isEmpty() && !newEmail.trim().isEmpty()) {
                userDAO.updateUser(new User(userId, newName, newEmail));
                refreshUserTable();
                JOptionPane.showMessageDialog(this, "User updated successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Name and email cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteUserAction() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.", "No User Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int userId = (int) userTableModel.getValueAt(selectedRow, 0);
        String name = (String) userTableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete '" + name + "'?\nThis will remove them from all groups and associated expenses.", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            userDAO.deleteUser(userId);
            refreshUserTable();
            updateUserDetails();
        }
    }

    private void assignToGroupAction() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user to assign to groups.", "No User Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int userId = (int) userTableModel.getValueAt(selectedRow, 0);
        String name = (String) userTableModel.getValueAt(selectedRow, 1);

        List<ExpenseGroup> allGroups = groupDAO.getAllGroups();
        List<ExpenseGroup> userGroups = groupMemberDAO.getGroupsForUser(userId);
        List<Integer> userGroupIds = userGroups.stream().map(ExpenseGroup::getGroupId).collect(Collectors.toList());

        JPanel panel = new JPanel(new GridLayout(0, 1));
        List<JCheckBox> checkBoxes = new java.util.ArrayList<>();
        for (ExpenseGroup group : allGroups) {
            JCheckBox checkBox = new JCheckBox(group.getGroupName());
            checkBox.setSelected(userGroupIds.contains(group.getGroupId()));
            checkBoxes.add(checkBox);
            panel.add(checkBox);
        }

        int result = JOptionPane.showConfirmDialog(this, new JScrollPane(panel), "Assign " + name + " to Groups", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            for (int i = 0; i < allGroups.size(); i++) {
                JCheckBox checkBox = checkBoxes.get(i);
                ExpenseGroup group = allGroups.get(i);
                int groupId = group.getGroupId();
                boolean isMember = userGroupIds.contains(groupId);

                if (checkBox.isSelected() && !isMember) {
                    groupMemberDAO.addMemberToGroup(groupId, userId);
                } else if (!checkBox.isSelected() && isMember) {
                    groupMemberDAO.removeMemberFromGroup(groupId, userId);
                }
            }
            updateUserDetails();
        }
    }
}
