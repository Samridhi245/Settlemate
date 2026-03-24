package services;

import models.AppData;
import models.Budget;
import models.Expense;
import utils.IdGenerator;
import utils.InputValidator;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BudgetService {
    private final AppData appData;

    public BudgetService(AppData appData) {
        this.appData = appData;
    }

    public Budget setBudget(String userId, String category, String period, double amount) {
        InputValidator.requireNonBlank(userId, "User ID");
        InputValidator.requireNonBlank(category, "Category");
        InputValidator.requireNonBlank(period, "Period");
        InputValidator.requirePositive(amount, "Budget amount");

        String normalizedPeriod = period.trim().toUpperCase();
        if (!"WEEKLY".equals(normalizedPeriod) && !"MONTHLY".equals(normalizedPeriod)) {
            throw new IllegalArgumentException("Budget period must be WEEKLY or MONTHLY.");
        }

        for (Budget existing : appData.getBudgets().values()) {
            if (existing.getUserId().equals(userId)
                    && existing.getCategory().equalsIgnoreCase(category.trim())
                    && existing.getPeriod().equalsIgnoreCase(normalizedPeriod)) {
                existing.setLimitAmount(amount);
                return existing;
            }
        }

        Budget budget = new Budget(IdGenerator.generateId("BDG"), userId, category.trim(), normalizedPeriod, amount);
        appData.getBudgets().put(budget.getBudgetId(), budget);
        return budget;
    }

    public List<Budget> getBudgetsByUser(String userId) {
        List<Budget> result = new ArrayList<>();
        for (Budget budget : appData.getBudgets().values()) {
            if (budget.getUserId().equals(userId)) {
                result.add(budget);
            }
        }
        return result;
    }

    public double calculateSpentForBudget(Budget budget) {
        double spent = 0.0;
        LocalDate today = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int currentWeek = today.get(weekFields.weekOfWeekBasedYear());
        int currentYear = today.getYear();
        int currentMonth = today.getMonthValue();

        for (Expense expense : appData.getExpenses().values()) {
            if (!expense.getPaidByUserId().equals(budget.getUserId())) {
                continue;
            }
            if (!expense.getCategory().equalsIgnoreCase(budget.getCategory())) {
                continue;
            }

            LocalDate expenseDate = expense.getDate();
            if ("MONTHLY".equalsIgnoreCase(budget.getPeriod())) {
                if (expenseDate.getYear() == currentYear && expenseDate.getMonthValue() == currentMonth) {
                    spent += expense.getAmount();
                }
            } else {
                int expenseWeek = expenseDate.get(weekFields.weekOfWeekBasedYear());
                if (expenseDate.getYear() == currentYear && expenseWeek == currentWeek) {
                    spent += expense.getAmount();
                }
            }
        }
        return spent;
    }

    public List<String> getBudgetAlerts(String userId) {
        List<String> alerts = new ArrayList<>();
        for (Budget budget : getBudgetsByUser(userId)) {
            double spent = calculateSpentForBudget(budget);
            if (spent > budget.getLimitAmount()) {
                alerts.add("ALERT: " + budget.getCategory() + " " + budget.getPeriod() + " budget exceeded by "
                        + String.format("%.2f", spent - budget.getLimitAmount()));
            } else if (spent >= 0.8 * budget.getLimitAmount()) {
                alerts.add("WARNING: " + budget.getCategory() + " " + budget.getPeriod() + " budget at "
                        + String.format("%.1f", (spent / budget.getLimitAmount()) * 100) + "%");
            }
        }
        return alerts;
    }
}
