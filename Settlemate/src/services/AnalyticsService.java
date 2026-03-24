package services;

import models.AppData;
import models.Expense;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

public class AnalyticsService {
    private final AppData appData;

    public AnalyticsService(AppData appData) {
        this.appData = appData;
    }

    public Map<String, Double> getCategoryWiseSpending(String groupId) {
        Map<String, Double> categoryTotals = new HashMap<>();
        for (Expense expense : appData.getExpenses().values()) {
            if (expense.getGroupId().equals(groupId)) {
                categoryTotals.put(expense.getCategory(),
                        categoryTotals.getOrDefault(expense.getCategory(), 0.0) + expense.getAmount());
            }
        }
        return categoryTotals;
    }

    public Map<YearMonth, Double> getSpendingTrend(String groupId) {
        Map<YearMonth, Double> trend = new HashMap<>();
        for (Expense expense : appData.getExpenses().values()) {
            if (expense.getGroupId().equals(groupId)) {
                YearMonth ym = YearMonth.from(expense.getDate());
                trend.put(ym, trend.getOrDefault(ym, 0.0) + expense.getAmount());
            }
        }
        return trend;
    }

    public String generateSummaryReport(String groupId) {
        Map<String, Double> category = getCategoryWiseSpending(groupId);
        Map<YearMonth, Double> trend = getSpendingTrend(groupId);

        double total = 0.0;
        for (double val : category.values()) {
            total += val;
        }

        StringBuilder report = new StringBuilder();
        report.append("---- Summary Report ----\n");
        report.append("Total Spending: ").append(String.format("%.2f", total)).append("\n");
        report.append("Category Breakdown:\n");
        for (Map.Entry<String, Double> e : category.entrySet()) {
            report.append("- ").append(e.getKey()).append(": ").append(String.format("%.2f", e.getValue())).append("\n");
        }
        report.append("Spending Trend:\n");
        for (Map.Entry<YearMonth, Double> e : trend.entrySet()) {
            report.append("- ").append(e.getKey()).append(": ").append(String.format("%.2f", e.getValue())).append("\n");
        }
        return report.toString();
    }
}
