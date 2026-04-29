package ui.screens;

import models.Group;
import ui.controllers.SettlementUiController;
import ui.navigation.ScreenIds;
import ui.navigation.ScreenNavigator;
import ui.style.Theme;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class ViewBalancesPanel extends JPanel {
    private final SettlementUiController controller;
    private final ScreenNavigator navigator;
    private final JComboBox<Group> groupComboBox;
    private final JTable balanceTable;
    private final JTable settlementsTable;
    private final JLabel fairnessLabel;
    private final JLabel totalLabel;

    public ViewBalancesPanel(SettlementUiController controller, ScreenNavigator navigator) {
        this.controller = controller;
        this.navigator = navigator;
        Theme.styleRootPanel(this);
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // Title
        JLabel title = new JLabel("View Balances & Settlements");
        title.setFont(Theme.TITLE_FONT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 12, 0);
        add(title, gbc);

        // Group selector row
        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.setBackground(Theme.BG);
        groupComboBox = new JComboBox<>();
        groupComboBox.setFont(Theme.BODY_FONT);
        JButton loadButton = new JButton("Load Balances");
        Theme.styleButton(loadButton);
        top.add(new JLabel("Select Group: "), BorderLayout.WEST);
        top.add(groupComboBox, BorderLayout.CENTER);
        top.add(loadButton, BorderLayout.EAST);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 10, 0);
        add(top, gbc);

        // Stats row
        JPanel statsRow = new JPanel(new GridLayout(1, 2, 16, 0));
        statsRow.setBackground(Theme.BG);
        fairnessLabel = new JLabel("Fairness Score: -");
        fairnessLabel.setFont(Theme.BODY_FONT);
        totalLabel = new JLabel("Total Expenses: -");
        totalLabel.setFont(Theme.BODY_FONT);
        statsRow.add(fairnessLabel);
        statsRow.add(totalLabel);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 10, 0);
        add(statsRow, gbc);

        // Two tables side by side
        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        tablesPanel.setBackground(Theme.BG);

        // Net balances table
        JPanel leftCard = new JPanel(new BorderLayout(0, 8));
        leftCard.setBackground(Theme.CARD);
        leftCard.setBorder(Theme.cardBorder());
        JLabel balTitle = new JLabel("Net Balances");
        balTitle.setFont(Theme.BODY_FONT.deriveFont(java.awt.Font.BOLD, 15f));
        leftCard.add(balTitle, BorderLayout.NORTH);
        balanceTable = new JTable() {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (col == 1 && getModel().getValueAt(row, col) != null) {
                    String val = getModel().getValueAt(row, col).toString();
                    try {
                        double v = Double.parseDouble(val);
                        c.setForeground(v >= 0 ? new Color(34, 139, 34) : new Color(200, 30, 30));
                    } catch (NumberFormatException e) {
                        c.setForeground(Theme.TEXT);
                    }
                } else {
                    c.setForeground(Theme.TEXT);
                }
                return c;
            }
        };
        balanceTable.setFont(Theme.BODY_FONT);
        balanceTable.setRowHeight(28);
        balanceTable.setEnabled(false);
        leftCard.add(new JScrollPane(balanceTable), BorderLayout.CENTER);
        JLabel balHint = new JLabel("<html><i>Green = owed money &nbsp; Red = owes money</i></html>");
        balHint.setFont(Theme.BODY_FONT.deriveFont(11f));
        balHint.setForeground(Theme.MUTED);
        leftCard.add(balHint, BorderLayout.SOUTH);

        // Suggested settlements table
        JPanel rightCard = new JPanel(new BorderLayout(0, 8));
        rightCard.setBackground(Theme.CARD);
        rightCard.setBorder(Theme.cardBorder());
        JLabel setTitle = new JLabel("Suggested Settlements");
        setTitle.setFont(Theme.BODY_FONT.deriveFont(java.awt.Font.BOLD, 15f));
        rightCard.add(setTitle, BorderLayout.NORTH);
        settlementsTable = new JTable();
        settlementsTable.setFont(Theme.BODY_FONT);
        settlementsTable.setRowHeight(28);
        settlementsTable.setEnabled(false);
        rightCard.add(new JScrollPane(settlementsTable), BorderLayout.CENTER);
        JLabel setHint = new JLabel("<html><i>Minimum transactions to settle all debts</i></html>");
        setHint.setFont(Theme.BODY_FONT.deriveFont(11f));
        setHint.setForeground(Theme.MUTED);
        rightCard.add(setHint, BorderLayout.SOUTH);

        tablesPanel.add(leftCard);
        tablesPanel.add(rightCard);

        gbc.gridy = 3;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 10, 0);
        add(tablesPanel, gbc);

        // Back button
        JButton backButton = new JButton("Back to Dashboard");
        Theme.styleButton(backButton);
        gbc.gridy = 4;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 0, 0, 0);
        add(backButton, gbc);

        loadButton.addActionListener(e -> loadBalances());
        backButton.addActionListener(e -> navigator.showScreen(ScreenIds.DASHBOARD));
    }

    public void refreshData() {
        DefaultComboBoxModel<Group> model = new DefaultComboBoxModel<>();
        for (Group group : controller.getAllGroups()) {
            model.addElement(group);
        }
        groupComboBox.setModel(model);
        balanceTable.setModel(new DefaultTableModel());
        settlementsTable.setModel(new DefaultTableModel());
        fairnessLabel.setText("Fairness Score: -");
        totalLabel.setText("Total Expenses: -");
    }

    private void loadBalances() {
        Group group = (Group) groupComboBox.getSelectedItem();
        if (group == null) return;

        balanceTable.setModel(controller.getBalancesTableModel(group.getGroupId()));
        settlementsTable.setModel(controller.getSettlementsTableModel(group.getGroupId()));

        double score = controller.getFairnessScore(group.getGroupId());
        String scoreColor = score >= 75 ? "green" : score >= 40 ? "orange" : "red";
        fairnessLabel.setText("<html>Fairness Score: <b><font color='" + scoreColor + "'>"
                + String.format("%.1f", score) + "/100</font></b></html>");
    }
}
