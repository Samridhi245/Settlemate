package ui.screens;

import models.Group;
import ui.controllers.TransactionUiController;
import ui.navigation.ScreenIds;
import ui.navigation.ScreenNavigator;
import ui.style.Theme;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class TransactionHistoryPanel extends JPanel {
    private final TransactionUiController controller;
    private final ScreenNavigator navigator;
    private final JComboBox<Group> groupComboBox;
    private final JTable historyTable;

    public TransactionHistoryPanel(TransactionUiController controller, ScreenNavigator navigator) {
        this.controller = controller;
        this.navigator = navigator;
        Theme.styleRootPanel(this);
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.setBackground(Theme.BG);
        groupComboBox = new JComboBox<>();
        JButton loadButton = new JButton("Load Transactions");
        Theme.styleButton(loadButton);
        top.add(groupComboBox, BorderLayout.CENTER);
        top.add(loadButton, BorderLayout.EAST);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 10, 0);
        add(top, gbc);

        historyTable = new JTable();
        gbc.gridy = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        add(new JScrollPane(historyTable), gbc);

        JButton backButton = new JButton("Back");
        Theme.styleButton(backButton);
        gbc.gridy = 2;
        gbc.weighty = 0;
        gbc.insets = new Insets(10, 0, 0, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(backButton, gbc);

        loadButton.addActionListener(e -> loadHistory());
        backButton.addActionListener(e -> navigator.showScreen(ScreenIds.DASHBOARD));
    }

    public void refreshData() {
        DefaultComboBoxModel<Group> model = new DefaultComboBoxModel<>();
        for (Group group : controller.getAllGroups()) {
            model.addElement(group);
        }
        groupComboBox.setModel(model);
        historyTable.setModel(new javax.swing.table.DefaultTableModel());
    }

    private void loadHistory() {
        Group group = (Group) groupComboBox.getSelectedItem();
        if (group == null) {
            historyTable.setModel(new javax.swing.table.DefaultTableModel());
            return;
        }
        historyTable.setModel(controller.getHistoryTableModel(group.getGroupId()));
    }
}
