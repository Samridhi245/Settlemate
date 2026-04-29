package ui.screens;

import models.Group;
import models.SettlementTransaction;
import ui.controllers.SettlementUiController;
import ui.navigation.ScreenIds;
import ui.navigation.ScreenNavigator;
import ui.style.Theme;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.List;

public class SettleDebtsPanel extends JPanel {
    private final SettlementUiController controller;
    private final ScreenNavigator navigator;
    private final JComboBox<Group> groupComboBox;
    private final JTable settlementTable;
    private final JLabel infoLabel;
    private final JTextField partialAmountField;

    // Store raw data for individual settle
    private List<SettlementTransaction> currentSettlements;

    public SettleDebtsPanel(SettlementUiController controller, ScreenNavigator navigator) {
        this.controller = controller;
        this.navigator = navigator;
        Theme.styleRootPanel(this);
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel title = new JLabel("Settle Debts");
        title.setFont(Theme.TITLE_FONT);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 6, 0);
        add(title, gbc);

        JLabel hint = new JLabel("<html><i>Select a row to settle individually or partially. " +
                "Or use 'Settle All' to clear all debts at once.</i></html>");
        hint.setFont(Theme.BODY_FONT.deriveFont(12f));
        hint.setForeground(Theme.MUTED);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 10, 0);
        add(hint, gbc);

        // Group selector
        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.setBackground(Theme.BG);
        groupComboBox = new JComboBox<>();
        groupComboBox.setFont(Theme.BODY_FONT);
        JButton calculateButton = new JButton("Calculate");
        Theme.styleButton(calculateButton);
        top.add(new JLabel("Group: "), BorderLayout.WEST);
        top.add(groupComboBox, BorderLayout.CENTER);
        top.add(calculateButton, BorderLayout.EAST);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 8, 0);
        add(top, gbc);

        // Info label
        infoLabel = new JLabel(" ");
        infoLabel.setFont(Theme.BODY_FONT.deriveFont(java.awt.Font.BOLD));
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 4, 0);
        add(infoLabel, gbc);

        // Table — selectable rows
        settlementTable = new JTable();
        settlementTable.setFont(Theme.BODY_FONT);
        settlementTable.setRowHeight(30);
        settlementTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gbc.gridy = 4;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 10, 0);
        add(new JScrollPane(settlementTable), gbc);

        // Partial amount row
        JPanel partialRow = new JPanel(new BorderLayout(8, 8));
        partialRow.setBackground(Theme.BG);
        JLabel partialLabel = new JLabel("Amount to settle (leave blank for full):");
        partialLabel.setFont(Theme.BODY_FONT);
        partialAmountField = new JTextField();
        partialAmountField.setFont(Theme.BODY_FONT);
        partialRow.add(partialLabel, BorderLayout.WEST);
        partialRow.add(partialAmountField, BorderLayout.CENTER);
        gbc.gridy = 5;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 8, 0);
        add(partialRow, gbc);

        // Footer buttons
        JPanel footer = new JPanel(new GridLayout(1, 3, 8, 8));
        footer.setBackground(Theme.BG);
        JButton settleSelectedBtn = new JButton("Settle Selected Row");
        JButton settleAllBtn = new JButton("Settle All");
        JButton backButton = new JButton("Back");
        Theme.styleButton(settleSelectedBtn);
        Theme.styleButton(settleAllBtn);
        Theme.styleButton(backButton);
        footer.add(settleSelectedBtn);
        footer.add(settleAllBtn);
        footer.add(backButton);
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 0, 0);
        add(footer, gbc);

        calculateButton.addActionListener(e -> loadSettlements());
        groupComboBox.addActionListener(e -> loadSettlements());
        settleSelectedBtn.addActionListener(e -> settleSelected());
        settleAllBtn.addActionListener(e -> settleAll());
        backButton.addActionListener(e -> navigator.showScreen(ScreenIds.DASHBOARD));
    }

    public void refreshData() {
        DefaultComboBoxModel<Group> model = new DefaultComboBoxModel<>();
        for (Group group : controller.getAllGroups()) {
            model.addElement(group);
        }
        groupComboBox.setModel(model);
        settlementTable.setModel(new DefaultTableModel());
        infoLabel.setText(" ");
        loadSettlements();
    }

    private void loadSettlements() {
        Group group = (Group) groupComboBox.getSelectedItem();
        if (group == null) {
            settlementTable.setModel(new DefaultTableModel());
            infoLabel.setText(" ");
            currentSettlements = null;
            return;
        }

        currentSettlements = controller.getSettlements(group.getGroupId());
        DefaultTableModel model = controller.getSettlementsTableModel(group.getGroupId());
        settlementTable.setModel(model);

        int rows = model.getRowCount();
        if (rows == 0) {
            infoLabel.setText("<html><font color='green'>All debts settled - no payments needed.</font></html>");
        } else {
            infoLabel.setText("<html><font color='#cc6600'>" + rows + " payment(s) pending.</font></html>");
        }
    }

    private void settleSelected() {
        int row = settlementTable.getSelectedRow();
        if (row < 0 || currentSettlements == null || row >= currentSettlements.size()) {
            JOptionPane.showMessageDialog(this, "Select a row from the table first.", "No row selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Group group = (Group) groupComboBox.getSelectedItem();
        if (group == null) return;

        SettlementTransaction tx = currentSettlements.get(row);
        double fullAmount = tx.getAmount();
        double amountToSettle;

        String input = partialAmountField.getText().trim();
        if (input.isEmpty()) {
            amountToSettle = fullAmount;
        } else {
            try {
                amountToSettle = Double.parseDouble(input);
                if (amountToSettle <= 0) throw new NumberFormatException();
                if (amountToSettle > fullAmount) {
                    JOptionPane.showMessageDialog(this,
                            "Amount cannot exceed the full debt of " + String.format("%.2f", fullAmount),
                            "Invalid amount", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid positive amount.", "Invalid input", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        String fromName = controller.getUserName(tx.getFromUserId());
        String toName = controller.getUserName(tx.getToUserId());
        int confirm = JOptionPane.showConfirmDialog(this,
                fromName + " pays " + toName + ": " + String.format("%.2f", amountToSettle)
                        + (amountToSettle < fullAmount ? "\n(Partial - remaining: " + String.format("%.2f", fullAmount - amountToSettle) + ")" : " (Full)"),
                "Confirm Settlement", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                controller.settleIndividual(group.getGroupId(), tx.getFromUserId(), tx.getToUserId(), amountToSettle);
                JOptionPane.showMessageDialog(this, "Settlement recorded.");
                partialAmountField.setText("");
                loadSettlements();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void settleAll() {
        Group group = (Group) groupComboBox.getSelectedItem();
        if (group == null) {
            JOptionPane.showMessageDialog(this, "Select a group first.", "No group", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (currentSettlements == null || currentSettlements.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No debts to settle.", "Nothing to settle", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Settle ALL " + currentSettlements.size() + " payment(s) and clear all balances?",
                "Confirm Settle All", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int count = controller.settleDebts(group.getGroupId());
                JOptionPane.showMessageDialog(this, count + " payment(s) recorded. All balances cleared.");
                loadSettlements();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
