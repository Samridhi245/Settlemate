package ui.screens;

import models.Budget;
import models.User;
import ui.controllers.BudgetUiController;
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
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.JTable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.List;

public class BudgetPanel extends JPanel {
    private final BudgetUiController controller;
    
    private final JComboBox<User> userComboBox;
    private final JTextField categoryField;
    private final JComboBox<String> periodComboBox;
    private final JTextField amountField;
    private final JTable budgetTable;
    private final JPanel alertsPanel;

    public BudgetPanel(BudgetUiController controller, ScreenNavigator navigator) {
        this.controller = controller;
        Theme.styleRootPanel(this);
        setLayout(new GridLayout(1, 2, 16, 0));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // ---- LEFT: Set Budget ----
        JPanel leftCard = new JPanel(new GridBagLayout());
        leftCard.setBackground(Theme.CARD);
        leftCard.setBorder(Theme.cardBorder());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 8, 4);

        JLabel title = new JLabel("Set Budget");
        title.setFont(Theme.TITLE_FONT);
        gbc.gridy = 0;
        leftCard.add(title, gbc);

        gbc.gridy = 1;
        leftCard.add(new JLabel("User:"), gbc);
        userComboBox = new JComboBox<>();
        userComboBox.setFont(Theme.BODY_FONT);
        gbc.gridy = 2;
        leftCard.add(userComboBox, gbc);

        gbc.gridy = 3;
        leftCard.add(new JLabel("Category (e.g. Food, Travel):"), gbc);
        categoryField = new JTextField();
        categoryField.setFont(Theme.BODY_FONT);
        gbc.gridy = 4;
        leftCard.add(categoryField, gbc);

        gbc.gridy = 5;
        leftCard.add(new JLabel("Period:"), gbc);
        periodComboBox = new JComboBox<>(new String[]{"MONTHLY", "WEEKLY"});
        periodComboBox.setFont(Theme.BODY_FONT);
        gbc.gridy = 6;
        leftCard.add(periodComboBox, gbc);

        gbc.gridy = 7;
        leftCard.add(new JLabel("Budget Limit Amount:"), gbc);
        amountField = new JTextField();
        amountField.setFont(Theme.BODY_FONT);
        gbc.gridy = 8;
        leftCard.add(amountField, gbc);

        JButton saveBtn = new JButton("Save Budget");
        Theme.styleButton(saveBtn);
        gbc.gridy = 9;
        gbc.insets = new Insets(12, 4, 4, 4);
        leftCard.add(saveBtn, gbc);

        JButton backBtn = new JButton("Back to Dashboard");
        Theme.styleButton(backBtn);
        gbc.gridy = 10;
        gbc.insets = new Insets(4, 4, 4, 4);
        leftCard.add(backBtn, gbc);

        saveBtn.addActionListener(e -> saveBudget());
        backBtn.addActionListener(e -> navigator.showScreen(ScreenIds.DASHBOARD));
        userComboBox.addActionListener(e -> loadBudgets());

        // ---- RIGHT: View Budgets + Alerts ----
        JPanel rightCard = new JPanel(new BorderLayout(8, 12));
        rightCard.setBackground(Theme.CARD);
        rightCard.setBorder(Theme.cardBorder());

        JLabel viewTitle = new JLabel("Budget Status");
        viewTitle.setFont(Theme.TITLE_FONT);
        rightCard.add(viewTitle, BorderLayout.NORTH);

        budgetTable = new JTable();
        budgetTable.setFont(Theme.BODY_FONT);
        budgetTable.setRowHeight(28);
        budgetTable.setEnabled(false);
        rightCard.add(new JScrollPane(budgetTable), BorderLayout.CENTER);

        alertsPanel = new JPanel();
        alertsPanel.setLayout(new GridBagLayout());
        alertsPanel.setBackground(Theme.CARD);
        JScrollPane alertsScroll = new JScrollPane(alertsPanel);
        alertsScroll.setPreferredSize(new java.awt.Dimension(300, 120));
        alertsScroll.setBorder(BorderFactory.createTitledBorder("Alerts"));
        rightCard.add(alertsScroll, BorderLayout.SOUTH);

        add(leftCard);
        add(rightCard);
    }

    public void refreshData() {
        DefaultComboBoxModel<User> model = new DefaultComboBoxModel<>();
        // Default to current user
        User current = controller.getCurrentUser();
        for (User u : controller.getAllUsers()) {
            model.addElement(u);
        }
        userComboBox.setModel(model);
        if (current != null) {
            userComboBox.setSelectedItem(current);
        }
        loadBudgets();
    }

    private void saveBudget() {
        try {
            User user = (User) userComboBox.getSelectedItem();
            if (user == null) throw new IllegalArgumentException("Select a user.");
            String category = categoryField.getText().trim();
            if (category.isEmpty()) throw new IllegalArgumentException("Category cannot be empty.");
            String period = (String) periodComboBox.getSelectedItem();
            double amount = Double.parseDouble(amountField.getText().trim());

            controller.setBudget(user.getUserId(), category, period, amount);
            JOptionPane.showMessageDialog(this, "Budget saved for " + user.getName()
                    + " - " + category + " (" + period + "): " + String.format("%.2f", amount));
            categoryField.setText("");
            amountField.setText("");
            loadBudgets();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid amount.", "Invalid input", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Save failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadBudgets() {
        User user = (User) userComboBox.getSelectedItem();
        if (user == null) return;

        List<Budget> budgets = controller.getBudgetsByUser(user.getUserId());

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Category", "Period", "Limit", "Spent", "Remaining", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (Budget b : budgets) {
            double spent = controller.getSpentForBudget(b);
            double remaining = b.getLimitAmount() - spent;
            double pct = b.getLimitAmount() > 0 ? (spent / b.getLimitAmount()) * 100 : 0;
            String status;
            if (pct > 100) status = "!! EXCEEDED";
            else if (pct >= 80) status = "! WARNING";
            else status = "OK";

            model.addRow(new Object[]{
                    b.getCategory(),
                    b.getPeriod(),
                    String.format("%.2f", b.getLimitAmount()),
                    String.format("%.2f", spent),
                    String.format("%.2f", remaining),
                    status
            });
        }
        budgetTable.setModel(model);

        // Color status column
        budgetTable.getColumnModel().getColumn(5).setCellRenderer(
                new javax.swing.table.DefaultTableCellRenderer() {
                    @Override
                    public java.awt.Component getTableCellRendererComponent(
                            JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                        String v = value == null ? "" : value.toString();
                        if (v.contains("EXCEEDED")) setForeground(new Color(200, 30, 30));
                        else if (v.contains("WARNING")) setForeground(new Color(200, 120, 0));
                        else setForeground(new Color(34, 139, 34));
                        return this;
                    }
                });

        // Alerts
        alertsPanel.removeAll();
        List<String> alerts = controller.getBudgetAlerts(user.getUserId());
        GridBagConstraints agbc = new GridBagConstraints();
        agbc.gridx = 0; agbc.weightx = 1;
        agbc.fill = GridBagConstraints.HORIZONTAL;
        agbc.insets = new Insets(2, 4, 2, 4);
        if (alerts.isEmpty()) {
            agbc.gridy = 0;
            JLabel ok = new JLabel("All budgets within limits.");
            ok.setForeground(new Color(34, 139, 34));
            ok.setFont(Theme.BODY_FONT);
            alertsPanel.add(ok, agbc);
        } else {
            for (int i = 0; i < alerts.size(); i++) {
                agbc.gridy = i;
                JLabel alert = new JLabel(alerts.get(i));
                alert.setFont(Theme.BODY_FONT.deriveFont(12f));
                alert.setForeground(alerts.get(i).startsWith("ALERT") ? new Color(200, 30, 30) : new Color(200, 120, 0));
                alertsPanel.add(alert, agbc);
            }
        }
        alertsPanel.revalidate();
        alertsPanel.repaint();
    }
}
