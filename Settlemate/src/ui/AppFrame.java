package ui;

import controllers.AppController;
import ui.controllers.AuthUiController;
import ui.controllers.BudgetUiController;
import ui.controllers.ExpenseUiController;
import ui.controllers.GroupUiController;
import ui.controllers.SettlementUiController;
import ui.controllers.TransactionUiController;
import ui.navigation.ScreenIds;
import ui.navigation.ScreenNavigator;
import ui.screens.AddExpensePanel;
import ui.screens.BudgetPanel;
import ui.screens.CreateGroupPanel;
import ui.screens.DashboardPanel;
import ui.screens.LoginRegisterPanel;
import ui.screens.SettleDebtsPanel;
import ui.screens.SplitExpensePanel;
import ui.screens.TransactionHistoryPanel;
import ui.screens.ViewBalancesPanel;
import ui.style.Theme;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import java.awt.CardLayout;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class AppFrame extends JFrame implements ScreenNavigator {
    private final AppController controller;
    private final CardLayout rootLayout;
    private final JPanel rootPanel;
    private final CardLayout contentLayout;
    private final JPanel contentPanel;
    private final LoginRegisterPanel loginPanel;
    private final DashboardPanel dashboardPanel;
    private final CreateGroupPanel createGroupPanel;
    private final AddExpensePanel addExpensePanel;
    private final SplitExpensePanel splitExpensePanel;
    private final ViewBalancesPanel viewBalancesPanel;
    private final SettleDebtsPanel settleDebtsPanel;
    private final TransactionHistoryPanel transactionHistoryPanel;
    private final BudgetPanel budgetPanel;

    public AppFrame(AppController controller) {
        this.controller = controller;
        this.rootLayout = new CardLayout();
        this.rootPanel = new JPanel(rootLayout);
        this.contentLayout = new CardLayout();
        this.contentPanel = new JPanel(contentLayout);
        Theme.styleRootPanel(contentPanel);
        AuthUiController authUiController = new AuthUiController(controller);
        GroupUiController groupUiController = new GroupUiController(controller);
        ExpenseUiController expenseUiController = new ExpenseUiController(controller);
        SettlementUiController settlementUiController = new SettlementUiController(controller);
        TransactionUiController transactionUiController = new TransactionUiController(controller);
        BudgetUiController budgetUiController = new BudgetUiController(controller);

        this.loginPanel = new LoginRegisterPanel(authUiController, this);
        this.dashboardPanel = new DashboardPanel(controller, this);
        this.createGroupPanel = new CreateGroupPanel(controller, groupUiController, this);
        this.addExpensePanel = new AddExpensePanel(expenseUiController, this);
        this.splitExpensePanel = new SplitExpensePanel(expenseUiController, this);
        this.viewBalancesPanel = new ViewBalancesPanel(settlementUiController, this);
        this.settleDebtsPanel = new SettleDebtsPanel(settlementUiController, this);
        this.transactionHistoryPanel = new TransactionHistoryPanel(transactionUiController, this);
        this.budgetPanel = new BudgetPanel(budgetUiController, this);

        expenseUiController.addExpenseUpdatedListener(() -> {
            viewBalancesPanel.refreshData();
            settleDebtsPanel.refreshData();
            transactionHistoryPanel.refreshData();
        });

        contentPanel.add(dashboardPanel, ScreenIds.DASHBOARD);
        contentPanel.add(createGroupPanel, ScreenIds.CREATE_GROUP);
        contentPanel.add(addExpensePanel, ScreenIds.ADD_EXPENSE);
        contentPanel.add(splitExpensePanel, ScreenIds.SPLIT_EXPENSE);
        contentPanel.add(viewBalancesPanel, ScreenIds.VIEW_BALANCES);
        contentPanel.add(settleDebtsPanel, ScreenIds.SETTLE_DEBTS);
        contentPanel.add(transactionHistoryPanel, ScreenIds.TRANSACTION_HISTORY);
        contentPanel.add(budgetPanel, ScreenIds.BUDGET);

        JPanel shellPanel = new JPanel(new BorderLayout());
        shellPanel.setBackground(Theme.BG);
        shellPanel.add(buildSidebar(), BorderLayout.WEST);
        shellPanel.add(contentPanel, BorderLayout.CENTER);

        rootPanel.add(loginPanel, ScreenIds.LOGIN);
        rootPanel.add(shellPanel, "shell");

        setTitle("SettleMate - Swing");
        setSize(1200, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        add(rootPanel);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    controller.saveData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AppFrame.this, "Failed to save data: " + ex.getMessage(),
                            "Save Error", JOptionPane.ERROR_MESSAGE);
                }
                dispose();
            }
        });
    }

    @Override
    public void showScreen(String screenId) {
        refreshScreen(screenId);
        if (ScreenIds.LOGIN.equals(screenId)) {
            rootLayout.show(rootPanel, ScreenIds.LOGIN);
        } else {
            rootLayout.show(rootPanel, "shell");
            contentLayout.show(contentPanel, screenId);
        }
    }

    private void refreshScreen(String screenId) {
        if (ScreenIds.DASHBOARD.equals(screenId)) {
            dashboardPanel.refreshData();
        } else if (ScreenIds.CREATE_GROUP.equals(screenId)) {
            createGroupPanel.refreshData();
        } else if (ScreenIds.ADD_EXPENSE.equals(screenId)) {
            addExpensePanel.refreshData();
        } else if (ScreenIds.SPLIT_EXPENSE.equals(screenId)) {
            splitExpensePanel.refreshData();
        } else if (ScreenIds.VIEW_BALANCES.equals(screenId)) {
            viewBalancesPanel.refreshData();
        } else if (ScreenIds.SETTLE_DEBTS.equals(screenId)) {
            settleDebtsPanel.refreshData();
        } else if (ScreenIds.TRANSACTION_HISTORY.equals(screenId)) {
            transactionHistoryPanel.refreshData();
        } else if (ScreenIds.BUDGET.equals(screenId)) {
            budgetPanel.refreshData();
        }
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Theme.SIDEBAR);
        sidebar.setPreferredSize(new Dimension(220, 760));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 12, 20, 12));

        JLabel brand = new JLabel("SettleMate", SwingConstants.LEFT);
        brand.setFont(Theme.TITLE_FONT.deriveFont(22f));
        brand.setForeground(Theme.TEXT);
        brand.setBorder(BorderFactory.createEmptyBorder(0, 8, 18, 8));
        sidebar.add(brand);

        sidebar.add(createSidebarButton("  Dashboard", UIManager.getIcon("FileView.computerIcon"), ScreenIds.DASHBOARD));
        sidebar.add(createSidebarButton("  Create Group", UIManager.getIcon("FileChooser.newFolderIcon"), ScreenIds.CREATE_GROUP));
        sidebar.add(createSidebarButton("  Add Expense", UIManager.getIcon("FileView.fileIcon"), ScreenIds.ADD_EXPENSE));
        sidebar.add(createSidebarButton("  Split Expense", UIManager.getIcon("OptionPane.questionIcon"), ScreenIds.SPLIT_EXPENSE));
        sidebar.add(createSidebarButton("  View Balances", UIManager.getIcon("FileView.hardDriveIcon"), ScreenIds.VIEW_BALANCES));
        sidebar.add(createSidebarButton("  Settle Debts", UIManager.getIcon("OptionPane.warningIcon"), ScreenIds.SETTLE_DEBTS));
        sidebar.add(createSidebarButton("  History", UIManager.getIcon("FileChooser.detailsViewIcon"), ScreenIds.TRANSACTION_HISTORY));
        sidebar.add(createSidebarButton("  Budgets", UIManager.getIcon("FileView.floppyDriveIcon"), ScreenIds.BUDGET));
        sidebar.add(createSidebarButton("  Logout", UIManager.getIcon("OptionPane.errorIcon"), ScreenIds.LOGIN));
        return sidebar;
    }

    private JButton createSidebarButton(String text, Icon icon, String targetScreen) {
        JButton button = new JButton(text, icon);
        Theme.styleSidebarButton(button);
        button.addActionListener(e -> showScreen(targetScreen));
        return button;
    }
}
