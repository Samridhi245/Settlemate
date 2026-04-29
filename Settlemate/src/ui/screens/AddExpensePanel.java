package ui.screens;

import models.Group;
import models.SplitType;
import models.User;
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
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;

public class AddExpensePanel extends JPanel {
    private final ExpenseUiController controller;
    private final ScreenNavigator navigator;
    private final JComboBox<Group> groupComboBox;
    private final JTextField descriptionField;
    private final JTextField amountField;
    private final JComboBox<User> payerComboBox;
    private final JComboBox<SplitType> splitTypeComboBox;
    private final JTextField categoryField;
    private final JTextField dateField;
    private final JTextField dueDateField;

    public AddExpensePanel(ExpenseUiController controller, ScreenNavigator navigator) {
        this.controller = controller;
        this.navigator = navigator;
        Theme.styleRootPanel(this);
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        groupComboBox = new JComboBox<>();
        descriptionField = new JTextField();
        amountField = new JTextField();
        payerComboBox = new JComboBox<>();
        splitTypeComboBox = new JComboBox<>(SplitType.values());
        categoryField = new JTextField();
        dateField = new JTextField(LocalDate.now().toString());
        dueDateField = new JTextField("(optional, YYYY-MM-DD)");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        row = addField("Group", groupComboBox, gbc, row);
        row = addField("Description", descriptionField, gbc, row);
        row = addField("Amount", amountField, gbc, row);
        row = addField("Paid By", payerComboBox, gbc, row);
        row = addField("Split Type", splitTypeComboBox, gbc, row);
        row = addField("Category", categoryField, gbc, row);
        row = addField("Expense Date (YYYY-MM-DD)", dateField, gbc, row);
        row = addField("Due Date", dueDateField, gbc, row);

        JButton submitButton = new JButton("Continue");
        Theme.styleButton(submitButton);
        JButton backButton = new JButton("Back");
        Theme.styleButton(backButton);
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0.5;
        add(submitButton, gbc);
        gbc.gridx = 1;
        add(backButton, gbc);

        submitButton.addActionListener(e -> handleSubmit());
        backButton.addActionListener(e -> navigator.showScreen(ScreenIds.DASHBOARD));
    }

    public void refreshData() {
        DefaultComboBoxModel<Group> groupModel = new DefaultComboBoxModel<>();
        for (Group group : controller.getAllGroups()) {
            groupModel.addElement(group);
        }
        groupComboBox.setModel(groupModel);

        DefaultComboBoxModel<User> userModel = new DefaultComboBoxModel<>();
        for (User user : controller.getAllUsers()) {
            userModel.addElement(user);
        }
        payerComboBox.setModel(userModel);
    }

    private void handleSubmit() {
        try {
            Group group = (Group) groupComboBox.getSelectedItem();
            User payer = (User) payerComboBox.getSelectedItem();
            if (group == null || payer == null) {
                throw new IllegalArgumentException("Select group and payer.");
            }
            double amount = Double.parseDouble(amountField.getText().trim());
            LocalDate date = LocalDate.parse(dateField.getText().trim());
            SplitType splitType = (SplitType) splitTypeComboBox.getSelectedItem();
            if (splitType == null) {
                throw new IllegalArgumentException("Choose split type.");
            }

            if (splitType == SplitType.EQUAL) {
                controller.addEqualExpense(
                        group.getGroupId(),
                        descriptionField.getText(),
                        amount,
                        payer.getUserId(),
                        categoryField.getText(),
                        date
                );
                JOptionPane.showMessageDialog(this, "Expense added.");
                clearForm();
            } else {
                controller.createPendingSplitExpense(
                        group.getGroupId(),
                        descriptionField.getText(),
                        amount,
                        payer.getUserId(),
                        splitType,
                        categoryField.getText(),
                        date
                );
                navigator.showScreen(ScreenIds.SPLIT_EXPENSE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Add expense failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        descriptionField.setText("");
        amountField.setText("");
        categoryField.setText("");
        dateField.setText(LocalDate.now().toString());
        dueDateField.setText("(optional, YYYY-MM-DD)");
    }

    private int addField(String label, java.awt.Component field, GridBagConstraints gbc, int row) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0.25;
        add(new JLabel(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.75;
        add(field, gbc);
        return row + 1;
    }
}
