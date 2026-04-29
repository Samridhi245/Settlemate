package ui.screens;

import controllers.AppController;
import models.Group;
import models.SettlementTransaction;
import ui.charts.PieChartPanel;
import ui.charts.SettlementGraphPanel;
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.List;
import java.util.Map;

public class DashboardPanel extends JPanel {
    private final AppController controller;
    private final JLabel welcomeLabel;
    private final JLabel totalExpenseLabel;
    private final JComboBox<Group> groupComboBox;
    private final JTable owesTable;
    private final PieChartPanel pieChartPanel;
    private final SettlementGraphPanel settlementGraphPanel;

    public DashboardPanel(AppController controller, ui.navigation.ScreenNavigator navigator) {
        this.controller = controller;
        Theme.styleRootPanel(this);
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // Header
        JPanel header = new JPanel(new BorderLayout(8, 8));
        header.setBackground(Theme.BG);
        welcomeLabel = new JLabel("Welcome");
        welcomeLabel.setFont(Theme.TITLE_FONT);
        header.add(welcomeLabel, BorderLayout.WEST);
        totalExpenseLabel = new JLabel("Total Expenses: 0.00");
        totalExpenseLabel.setFont(Theme.BODY_FONT.deriveFont(16f));
        header.add(totalExpenseLabel, BorderLayout.EAST);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 10, 0);
        add(header, gbc);

        // Center: left = group selector + owes table, right = charts
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 12, 12));
        centerPanel.setBackground(Theme.BG);

        JPanel leftPanel = new JPanel(new BorderLayout(8, 8));
        leftPanel.setBackground(Theme.CARD);
        leftPanel.setBorder(Theme.cardBorder());

        JPanel groupSelectPanel = new JPanel(new BorderLayout(8, 8));
        groupSelectPanel.setBackground(Theme.CARD);
        groupComboBox = new JComboBox<>();
        groupComboBox.setFont(Theme.BODY_FONT);
        JButton refreshButton = new JButton("Refresh");
        Theme.styleButton(refreshButton);
        groupSelectPanel.add(new JLabel("Group: "), BorderLayout.WEST);
        groupSelectPanel.add(groupComboBox, BorderLayout.CENTER);
        groupSelectPanel.add(refreshButton, BorderLayout.EAST);
        leftPanel.add(groupSelectPanel, BorderLayout.NORTH);

        owesTable = new JTable();
        owesTable.setFont(Theme.BODY_FONT);
        owesTable.setRowHeight(28);
        owesTable.setEnabled(false);
        leftPanel.add(new JScrollPane(owesTable), BorderLayout.CENTER);

        JLabel owesHint = new JLabel("  Who owes whom (suggested settlements)");
        owesHint.setFont(Theme.BODY_FONT.deriveFont(11f));
        owesHint.setForeground(Theme.MUTED);
        leftPanel.add(owesHint, BorderLayout.SOUTH);

        JPanel rightPanel = new JPanel(new GridLayout(2, 1, 8, 8));
        rightPanel.setBackground(Theme.BG);
        pieChartPanel = new PieChartPanel();
        pieChartPanel.setBorder(Theme.cardBorder());
        settlementGraphPanel = new SettlementGraphPanel();
        settlementGraphPanel.setBorder(Theme.cardBorder());
        rightPanel.add(pieChartPanel);
        rightPanel.add(settlementGraphPanel);

        centerPanel.add(leftPanel);
        centerPanel.add(rightPanel);

        gbc.gridy = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        add(centerPanel, gbc);

        refreshButton.addActionListener(e -> updateVisuals());
        groupComboBox.addActionListener(e -> updateVisuals());
    }

    public void refreshData() {
        welcomeLabel.setText(controller.getCurrentUser() == null
                ? "Welcome"
                : "Welcome, " + controller.getCurrentUser().getName());

        DefaultComboBoxModel<Group> model = new DefaultComboBoxModel<>();
        for (Group group : controller.getAllGroups()) {
            model.addElement(group);
        }
        groupComboBox.setModel(model);
        updateVisuals();
    }

    private void updateVisuals() {
        Group group = (Group) groupComboBox.getSelectedItem();
        if (group == null) {
            totalExpenseLabel.setText("Total Expenses: 0.00");
            owesTable.setModel(new DefaultTableModel());
            pieChartPanel.setValues(java.util.Collections.emptyMap());
            settlementGraphPanel.setData(java.util.Collections.emptyList(), controller.getUserNameMap());
            return;
        }

        double total = controller.getTotalExpensesAmount(group.getGroupId());
        totalExpenseLabel.setText("Total Expenses: " + String.format("%.2f", total));

        List<SettlementTransaction> settlements = controller.getSettlementSuggestions(group.getGroupId());
        DefaultTableModel owesModel = new DefaultTableModel(new Object[]{"From", "To", "Amount"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (SettlementTransaction s : settlements) {
            owesModel.addRow(new Object[]{
                    controller.getUserName(s.getFromUserId()),
                    controller.getUserName(s.getToUserId()),
                    String.format("%.2f", s.getAmount())
            });
        }
        owesTable.setModel(owesModel);

        Map<String, Double> categoryTotals = controller.getCategoryTotals(group.getGroupId());
        pieChartPanel.setValues(categoryTotals);
        settlementGraphPanel.setData(settlements, controller.getUserNameMap());
    }
}
