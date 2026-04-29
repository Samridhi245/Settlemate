package ui.controllers;

import controllers.AppController;
import models.Budget;
import models.User;

import java.util.List;

public class BudgetUiController {
    private final AppController appController;

    public BudgetUiController(AppController appController) {
        this.appController = appController;
    }

    public User getCurrentUser() {
        return appController.getCurrentUser();
    }

    public List<User> getAllUsers() {
        return appController.getAllUsers();
    }

    public Budget setBudget(String userId, String category, String period, double amount) {
        return appController.setBudget(userId, category, period, amount);
    }

    public List<Budget> getBudgetsByUser(String userId) {
        return appController.getBudgetsByUser(userId);
    }

    public double getSpentForBudget(Budget budget) {
        return appController.getSpentForBudget(budget);
    }

    public List<String> getBudgetAlerts(String userId) {
        return appController.getBudgetAlerts(userId);
    }
}
