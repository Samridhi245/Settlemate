package ui.controllers;

import controllers.AppController;
import models.Expense;
import models.Group;
import models.SplitType;
import models.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExpenseUiController {
    private final AppController appController;
    private final List<Runnable> expenseListeners;

    public ExpenseUiController(AppController appController) {
        this.appController = appController;
        this.expenseListeners = new ArrayList<>();
    }

    public List<Group> getAllGroups() {
        return appController.getAllGroups();
    }

    public List<User> getAllUsers() {
        return appController.getAllUsers();
    }

    public Expense addEqualExpense(String groupId, String description, double amount, String payerId, String category, LocalDate date) {
        Expense expense = appController.addEqualExpense(groupId, description, amount, payerId, category, date);
        notifyExpenseUpdated();
        return expense;
    }

    public void createPendingSplitExpense(String groupId, String description, double amount, String payerId, SplitType splitType,
                                          String category, LocalDate date) {
        appController.createPendingSplitExpense(groupId, description, amount, payerId, splitType, category, date);
    }

    public AppController.ExpenseDraft getPendingSplitExpense() {
        return appController.getPendingExpenseDraft();
    }

    public List<String> getMemberIdsByGroup(String groupId) {
        return appController.getMemberIdsByGroup(groupId);
    }

    public String getUserName(String userId) {
        return appController.getUserName(userId);
    }

    public List<models.Expense> getExpensesByGroup(String groupId) {
        return appController.getExpensesByGroup(groupId);
    }

    public Expense submitPendingSplitExpense(Map<String, Double> splitDetails) {
        Expense expense = appController.submitPendingSplitExpense(splitDetails);
        notifyExpenseUpdated();
        return expense;
    }

    public void addExpenseUpdatedListener(Runnable listener) {
        expenseListeners.add(listener);
    }

    private void notifyExpenseUpdated() {
        for (Runnable listener : expenseListeners) {
            listener.run();
        }
    }
}
