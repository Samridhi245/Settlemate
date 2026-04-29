package ui.screens;

import controllers.AppController;
import models.Expense;
import models.SplitType;
import ui.controllers.ExpenseUiController;
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
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplitExpensePanel extends JPanel {
    private final ExpenseUiController controller;
    private final ScreenNavigator navigator;
    private final JPanel fieldsPanel;
    private final Map<String, JTextField> fieldByUserId;
    private final JLabel titleLabel;
    private final JLabel statusLabel;
    private final JTable expensesTable;
    private final JComboBox<models.Group> groupComboBox;

    public SplitExpensePanel(ExpenseUiController controller, ScreenNavigator navigator) {
        this.controller = controller;
        this.navigator = navigator;
        this.fieldByUserId = new HashMap<>();
        Theme.styleRootPanel(this);
        setLayout(new GridLayout(1, 2, 16, 0));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // ---- LEFT: Pending split entry ----
        JPanel leftPanel = new JPanel(new BorderLayout(8, 8));
        leftPanel.setBackground(Theme.CARD);
        leftPanel.setBorder(Theme.cardBorder());

        titleLabel = new JLabel("Split Expense");
        titleLabel.setFont(Theme.TITLE_FONT);
        leftPanel.add(titleLabel, BorderLayout.NORTH);

        statusLabel = new JLabel("<html><i>To split an expense, go to Add Expense,<br>choose EXACT or PERCENTAGE split type,<br>fill in the details and click Continue.</i></html>");
        statusLabel.setFont(Theme.BODY_FONT);
        statusLabel.setForeground(Theme.MUTED);

        fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBackground(Theme.CARD);

        JScrollPane fieldsScroll = new JScrollPane(fieldsPanel);
        fieldsScroll.setBorder(null);
        leftPanel.add(fieldsScroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new GridLayout(1, 2, 8, 8));
        footer.setBackground(Theme.CARD);
        JButton submitButton = new JButton("Submit Split");
        Theme.styleButton(submitButton);
        JButton backButton = new JButton("Back");
        Theme.styleButton(backButton);
        footer.add(submitButton);
        footer.add(backButton);
        leftPanel.add(footer, BorderLayout.SOUTH);

        submitButton.addActionListener(e -> submit());
        backButton.addActionListener(e -> navigator.showScreen(ScreenIds.ADD_EXPENSE));

        // ---- RIGHT: Existing expenses viewer ----
        JPanel rightPanel = new JPanel(new BorderLayout(8, 8));
        rightPanel.setBackground(Theme.CARD);
        rightPanel.setBorder(Theme.cardBorder());

        JLabel viewTitle = new JLabel("Expenses by Group");
        viewTitle.setFont(Theme.TITLE_FONT);
        rightPanel.add(viewTitle, BorderLayout.NORTH);

        JPanel groupRow = new JPanel(new BorderLayout(8, 8));
        groupRow.setBackground(Theme.CARD);
        groupComboBox = new JComboBox<>();
        JButton loadBtn = new JButton("Load");
        Theme.styleButton(loadBtn);
        groupRow.add(new JLabel("Group: "), BorderLayout.WEST);
        groupRow.add(groupComboBox, BorderLayout.CENTER);
        groupRow.add(loadBtn, BorderLayout.EAST);
        rightPanel.add(groupRow, BorderLayout.NORTH);

        expensesTable = new JTable();
        expensesTable.setFont(Theme.BODY_FONT);
        expensesTable.setRowHeight(28);
        rightPanel.add(new JScrollPane(expensesTable), BorderLayout.CENTER);

        loadBtn.addActionListener(e -> loadExpenses());

        add(leftPanel);
        add(rightPanel);
    }

    public void refreshData() {
        // Refresh group combo
        DefaultComboBoxModel<models.Group> model = new DefaultComboBoxModel<>();
        for (models.Group g : controller.getAllGroups()) {
            model.addElement(g);
        }
        groupComboBox.setModel(model);

        // Refresh split fields
        fieldsPanel.removeAll();
        fieldByUserId.clear();

        AppController.ExpenseDraft draft = controller.getPendingSplitExpense();
        if (draft == null) {
            titleLabel.setText("Split Expense");
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(12, 8, 12, 8);
            fieldsPanel.add(statusLabel, gbc);
        } else {
            String label = draft.splitType == SplitType.EXACT ? "Amount" : "Percentage (%)";
            titleLabel.setText("Split: " + draft.description + " (" + draft.splitType.name() + ")");

            List<String> memberIds = controller.getMemberIdsByGroup(draft.groupId);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 8, 6, 8);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1;

            // Show total amount hint
            JLabel hint = new JLabel("Total: " + String.format("%.2f", draft.amount)
                    + (draft.splitType == SplitType.PERCENTAGE ? "  |  Percentages must sum to 100" : "  |  Amounts must sum to total"));
            hint.setFont(Theme.BODY_FONT);
            hint.setForeground(Theme.MUTED);
            gbc.gridy = 0;
            gbc.gridx = 0;
            gbc.gridwidth = 2;
            fieldsPanel.add(hint, gbc);
            gbc.gridwidth = 1;

            int row = 1;
            for (String memberId : memberIds) {
                gbc.gridy = row;
                gbc.gridx = 0;
                gbc.weightx = 0.5;
                fieldsPanel.add(new JLabel(controller.getUserName(memberId) + " - " + label), gbc);
                JTextField field = new JTextField();
                field.setFont(Theme.BODY_FONT);
                fieldByUserId.put(memberId, field);
                gbc.gridx = 1;
                gbc.weightx = 0.5;
                fieldsPanel.add(field, gbc);
                row++;
            }
        }
        fieldsPanel.revalidate();
        fieldsPanel.repaint();

        loadExpenses();
    }

    private void loadExpenses() {
        models.Group group = (models.Group) groupComboBox.getSelectedItem();
        if (group == null) {
            expensesTable.setModel(new DefaultTableModel());
            return;
        }
        DefaultTableModel tableModel = new DefaultTableModel(
                new Object[]{"Description", "Amount", "Paid By", "Split Type", "Category", "Date"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        for (Expense exp : controller.getExpensesByGroup(group.getGroupId())) {
            tableModel.addRow(new Object[]{
                    exp.getDescription(),
                    String.format("%.2f", exp.getAmount()),
                    controller.getUserName(exp.getPaidByUserId()),
                    exp.getSplitType().name(),
                    exp.getCategory(),
                    exp.getDate().toString()
            });
        }
        expensesTable.setModel(tableModel);
    }

    private List<Expense> getExpensesForGroup(String groupId) {
        return controller.getExpensesByGroup(groupId);
    }

    private void submit() {
        AppController.ExpenseDraft draft = controller.getPendingSplitExpense();
        if (draft == null) {
            JOptionPane.showMessageDialog(this, "No pending split expense. Go to Add Expense first.",
                    "Nothing to submit", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Map<String, Double> splitDetails = new HashMap<>();
            for (Map.Entry<String, JTextField> entry : fieldByUserId.entrySet()) {
                String text = entry.getValue().getText().trim();
                if (text.isEmpty()) throw new IllegalArgumentException("Fill in all fields.");
                splitDetails.put(entry.getKey(), Double.parseDouble(text));
            }
            controller.submitPendingSplitExpense(splitDetails);
            JOptionPane.showMessageDialog(this, "Split expense added successfully.");
            navigator.showScreen(ScreenIds.DASHBOARD);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Split save failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
