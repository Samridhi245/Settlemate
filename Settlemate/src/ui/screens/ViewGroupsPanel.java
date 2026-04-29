package ui.screens;

import controllers.AppController;
import models.Expense;
import models.Group;
import ui.navigation.ScreenIds;
import ui.navigation.ScreenNavigator;
import ui.style.Theme;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.List;

public class ViewGroupsPanel extends JPanel {
    private final AppController controller;
    private final ScreenNavigator navigator;
    private final DefaultListModel<Group> groupListModel;
    private final JList<Group> groupList;
    private final JLabel groupInfoLabel;
    private final JTable membersTable;
    private final JTable expensesTable;

    public ViewGroupsPanel(AppController controller, ScreenNavigator navigator) {
        this.controller = controller;
        this.navigator = navigator;
        Theme.styleRootPanel(this);
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // Title
        JLabel title = new JLabel("View Groups");
        title.setFont(Theme.TITLE_FONT);
        add(title, BorderLayout.NORTH);

        // Left: group list
        JPanel leftPanel = new JPanel(new BorderLayout(0, 8));
        leftPanel.setBackground(Theme.CARD);
        leftPanel.setBorder(Theme.cardBorder());
        JLabel groupsLabel = new JLabel("Your Groups");
        groupsLabel.setFont(Theme.BODY_FONT.deriveFont(java.awt.Font.BOLD));
        leftPanel.add(groupsLabel, BorderLayout.NORTH);
        groupListModel = new DefaultListModel<>();
        groupList = new JList<>(groupListModel);
        groupList.setFont(Theme.BODY_FONT);
        groupList.setFixedCellHeight(32);
        groupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        leftPanel.add(new JScrollPane(groupList), BorderLayout.CENTER);

        // Right: group details
        JPanel rightPanel = new JPanel(new GridLayout(2, 1, 0, 12));
        rightPanel.setBackground(Theme.BG);

        // Members card
        JPanel membersCard = new JPanel(new BorderLayout(0, 6));
        membersCard.setBackground(Theme.CARD);
        membersCard.setBorder(Theme.cardBorder());
        groupInfoLabel = new JLabel("Select a group");
        groupInfoLabel.setFont(Theme.BODY_FONT.deriveFont(java.awt.Font.BOLD, 15f));
        membersCard.add(groupInfoLabel, BorderLayout.NORTH);
        membersTable = new JTable();
        membersTable.setFont(Theme.BODY_FONT);
        membersTable.setRowHeight(28);
        membersTable.setEnabled(false);
        membersCard.add(new JScrollPane(membersTable), BorderLayout.CENTER);

        // Expenses card
        JPanel expensesCard = new JPanel(new BorderLayout(0, 6));
        expensesCard.setBackground(Theme.CARD);
        expensesCard.setBorder(Theme.cardBorder());
        JLabel expLabel = new JLabel("Expenses");
        expLabel.setFont(Theme.BODY_FONT.deriveFont(java.awt.Font.BOLD, 15f));
        expensesCard.add(expLabel, BorderLayout.NORTH);
        expensesTable = new JTable();
        expensesTable.setFont(Theme.BODY_FONT);
        expensesTable.setRowHeight(28);
        expensesTable.setEnabled(false);
        expensesCard.add(new JScrollPane(expensesTable), BorderLayout.CENTER);

        rightPanel.add(membersCard);
        rightPanel.add(expensesCard);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        centerPanel.setBackground(Theme.BG);
        centerPanel.add(leftPanel);
        centerPanel.add(rightPanel);
        add(centerPanel, BorderLayout.CENTER);

        // Back button
        JButton backBtn = new JButton("Back to Dashboard");
        Theme.styleButton(backBtn);
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Theme.BG);
        footer.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        footer.add(backBtn, BorderLayout.WEST);
        add(footer, BorderLayout.SOUTH);

        groupList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadGroupDetails();
        });
        backBtn.addActionListener(e -> navigator.showScreen(ScreenIds.DASHBOARD));
    }

    public void refreshData() {
        groupListModel.clear();
        for (Group g : controller.getGroupsForCurrentUser()) {
            groupListModel.addElement(g);
        }
        membersTable.setModel(new DefaultTableModel());
        expensesTable.setModel(new DefaultTableModel());
        groupInfoLabel.setText("Select a group");
        if (groupListModel.size() > 0) {
            groupList.setSelectedIndex(0);
        }
    }

    private void loadGroupDetails() {
        Group group = groupList.getSelectedValue();
        if (group == null) return;

        groupInfoLabel.setText("Members of: " + group.getGroupName()
                + "  (" + group.getMemberUserIds().size() + " members)");

        // Members table
        DefaultTableModel mModel = new DefaultTableModel(new Object[]{"Member Name", "User ID"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (String uid : group.getMemberUserIds()) {
            mModel.addRow(new Object[]{ controller.getUserName(uid), uid });
        }
        membersTable.setModel(mModel);

        // Expenses table
        List<Expense> expenses = controller.getExpensesByGroup(group.getGroupId());
        DefaultTableModel eModel = new DefaultTableModel(
                new Object[]{"Description", "Amount", "Paid By", "Split", "Category", "Date"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Expense e : expenses) {
            eModel.addRow(new Object[]{
                    e.getDescription(),
                    String.format("%.2f", e.getAmount()),
                    controller.getUserName(e.getPaidByUserId()),
                    e.getSplitType().name(),
                    e.getCategory(),
                    e.getDate().toString()
            });
        }
        expensesTable.setModel(eModel);
    }
}
