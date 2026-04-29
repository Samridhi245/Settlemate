package ui.screens;

import controllers.AppController;
import models.Group;
import models.User;
import ui.controllers.GroupUiController;
import ui.navigation.ScreenIds;
import ui.navigation.ScreenNavigator;
import ui.style.Theme;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

public class CreateGroupPanel extends JPanel {
    private final AppController controller;
    private final GroupUiController groupUiController;
    private final ScreenNavigator navigator;

    // Create section
    private final JTextField createNameField;
    private final DefaultListModel<User> createMembersModel;
    private final JList<User> createMembersList;

    // Manage section
    private final JComboBox<Group> groupComboBox;
    private final JTextField updateNameField;
    private final DefaultListModel<User> updateMembersModel;
    private final JList<User> updateMembersList;

    public CreateGroupPanel(AppController controller, GroupUiController groupUiController, ScreenNavigator navigator) {
        this.controller = controller;
        this.groupUiController = groupUiController;
        this.navigator = navigator;
        Theme.styleRootPanel(this);
        setLayout(new GridLayout(1, 2, 16, 0));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // ---- LEFT: Create Group ----
        JPanel createPanel = new JPanel(new GridBagLayout());
        createPanel.setBackground(Theme.CARD);
        createPanel.setBorder(Theme.cardBorder());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 8, 4);

        JLabel createTitle = new JLabel("Create New Group");
        createTitle.setFont(Theme.TITLE_FONT);
        gbc.gridy = 0;
        createPanel.add(createTitle, gbc);

        gbc.gridy = 1;
        createPanel.add(new JLabel("Group Name:"), gbc);

        createNameField = new JTextField();
        createNameField.setFont(Theme.BODY_FONT);
        createNameField.setPreferredSize(new Dimension(260, 32));
        gbc.gridy = 2;
        createPanel.add(createNameField, gbc);

        gbc.gridy = 3;
        createPanel.add(new JLabel("Select Members (Ctrl+Click for multiple):"), gbc);

        createMembersModel = new DefaultListModel<>();
        createMembersList = new JList<>(createMembersModel);
        createMembersList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        createMembersList.setFont(Theme.BODY_FONT);
        createMembersList.setFixedCellHeight(30);
        JScrollPane createScroll = new JScrollPane(createMembersList);
        createScroll.setPreferredSize(new Dimension(260, 160));
        gbc.gridy = 4;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        createPanel.add(createScroll, gbc);

        JButton createBtn = new JButton("Create Group");
        Theme.styleButton(createBtn);
        gbc.gridy = 5;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 4, 4, 4);
        createPanel.add(createBtn, gbc);

        JButton backBtn = new JButton("Back to Dashboard");
        Theme.styleButton(backBtn);
        gbc.gridy = 6;
        createPanel.add(backBtn, gbc);

        createBtn.addActionListener(e -> createGroup());
        backBtn.addActionListener(e -> navigator.showScreen(ScreenIds.DASHBOARD));

        // ---- RIGHT: Manage Group ----
        JPanel managePanel = new JPanel(new GridBagLayout());
        managePanel.setBackground(Theme.CARD);
        managePanel.setBorder(Theme.cardBorder());

        GridBagConstraints mgbc = new GridBagConstraints();
        mgbc.gridx = 0;
        mgbc.weightx = 1;
        mgbc.fill = GridBagConstraints.HORIZONTAL;
        mgbc.insets = new Insets(4, 4, 8, 4);

        JLabel manageTitle = new JLabel("Manage Existing Group");
        manageTitle.setFont(Theme.TITLE_FONT);
        mgbc.gridy = 0;
        managePanel.add(manageTitle, mgbc);

        mgbc.gridy = 1;
        managePanel.add(new JLabel("Select Group:"), mgbc);

        groupComboBox = new JComboBox<>();
        groupComboBox.setFont(Theme.BODY_FONT);
        mgbc.gridy = 2;
        managePanel.add(groupComboBox, mgbc);

        mgbc.gridy = 3;
        managePanel.add(new JLabel("New Name:"), mgbc);

        updateNameField = new JTextField();
        updateNameField.setFont(Theme.BODY_FONT);
        updateNameField.setPreferredSize(new Dimension(260, 32));
        mgbc.gridy = 4;
        managePanel.add(updateNameField, mgbc);

        mgbc.gridy = 5;
        managePanel.add(new JLabel("Update Members (Ctrl+Click for multiple):"), mgbc);

        updateMembersModel = new DefaultListModel<>();
        updateMembersList = new JList<>(updateMembersModel);
        updateMembersList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        updateMembersList.setFont(Theme.BODY_FONT);
        updateMembersList.setFixedCellHeight(30);
        JScrollPane updateScroll = new JScrollPane(updateMembersList);
        updateScroll.setPreferredSize(new Dimension(260, 120));
        mgbc.gridy = 6;
        mgbc.weighty = 1;
        mgbc.fill = GridBagConstraints.BOTH;
        managePanel.add(updateScroll, mgbc);

        JPanel manageButtons = new JPanel(new GridLayout(1, 2, 8, 0));
        manageButtons.setBackground(Theme.CARD);
        JButton updateBtn = new JButton("Update Group");
        JButton deleteBtn = new JButton("Delete Group");
        Theme.styleButton(updateBtn);
        Theme.styleButton(deleteBtn);
        deleteBtn.setBackground(new java.awt.Color(220, 53, 69));
        manageButtons.add(updateBtn);
        manageButtons.add(deleteBtn);
        mgbc.gridy = 7;
        mgbc.weighty = 0;
        mgbc.fill = GridBagConstraints.HORIZONTAL;
        mgbc.insets = new Insets(10, 4, 4, 4);
        managePanel.add(manageButtons, mgbc);

        // Load group details when selection changes
        groupComboBox.addActionListener(e -> loadSelectedGroup());
        updateBtn.addActionListener(e -> updateGroup());
        deleteBtn.addActionListener(e -> deleteGroup());

        add(createPanel);
        add(managePanel);
    }

    public void refreshData() {
        // Refresh create members list
        createMembersModel.clear();
        for (User user : groupUiController.getAllUsers()) {
            createMembersModel.addElement(user);
        }

        // Refresh update members list
        updateMembersModel.clear();
        for (User user : groupUiController.getAllUsers()) {
            updateMembersModel.addElement(user);
        }

        // Refresh group combo
        DefaultComboBoxModel<Group> model = new DefaultComboBoxModel<>();
        for (Group group : groupUiController.getAllGroups()) {
            model.addElement(group);
        }
        groupComboBox.setModel(model);
        loadSelectedGroup();
    }

    private void loadSelectedGroup() {
        Group group = (Group) groupComboBox.getSelectedItem();
        if (group == null) return;
        updateNameField.setText(group.getGroupName());
        // Pre-select current members
        for (int i = 0; i < updateMembersModel.size(); i++) {
            User user = updateMembersModel.get(i);
            if (group.getMemberUserIds().contains(user.getUserId())) {
                updateMembersList.addSelectionInterval(i, i);
            } else {
                updateMembersList.removeSelectionInterval(i, i);
            }
        }
    }

    private void createGroup() {
        try {
            String name = createNameField.getText().trim();
            if (name.isEmpty()) throw new IllegalArgumentException("Group name cannot be empty.");
            List<String> memberIds = new ArrayList<>();
            for (User user : createMembersList.getSelectedValuesList()) {
                memberIds.add(user.getUserId());
            }
            if (controller.getCurrentUser() != null && !memberIds.contains(controller.getCurrentUser().getUserId())) {
                memberIds.add(controller.getCurrentUser().getUserId());
            }
            Group group = groupUiController.createGroup(name, memberIds);
            JOptionPane.showMessageDialog(this, "Group created: " + group.getGroupName());
            createNameField.setText("");
            createMembersList.clearSelection();
            refreshData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Create group failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateGroup() {
        Group group = (Group) groupComboBox.getSelectedItem();
        if (group == null) {
            JOptionPane.showMessageDialog(this, "Select a group to update.", "No group selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            String newName = updateNameField.getText().trim();
            if (newName.isEmpty()) throw new IllegalArgumentException("Group name cannot be empty.");
            List<String> memberIds = new ArrayList<>();
            for (User user : updateMembersList.getSelectedValuesList()) {
                memberIds.add(user.getUserId());
            }
            if (memberIds.isEmpty()) throw new IllegalArgumentException("Select at least one member.");
            groupUiController.updateGroup(group.getGroupId(), newName, memberIds);
            JOptionPane.showMessageDialog(this, "Group updated successfully.");
            refreshData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Update failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteGroup() {
        Group group = (Group) groupComboBox.getSelectedItem();
        if (group == null) {
            JOptionPane.showMessageDialog(this, "Select a group to delete.", "No group selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete group '" + group.getGroupName() + "'? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                groupUiController.deleteGroup(group.getGroupId());
                JOptionPane.showMessageDialog(this, "Group deleted.");
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Delete failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
