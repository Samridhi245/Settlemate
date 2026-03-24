package models;

import java.io.Serializable;

public class Budget implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String budgetId;
    private final String userId;
    private final String category;
    private final String period; // WEEKLY or MONTHLY
    private double limitAmount;

    public Budget(String budgetId, String userId, String category, String period, double limitAmount) {
        this.budgetId = budgetId;
        this.userId = userId;
        this.category = category;
        this.period = period;
        this.limitAmount = limitAmount;
    }

    public String getBudgetId() {
        return budgetId;
    }

    public String getUserId() {
        return userId;
    }

    public String getCategory() {
        return category;
    }

    public String getPeriod() {
        return period;
    }

    public double getLimitAmount() {
        return limitAmount;
    }

    public void setLimitAmount(double limitAmount) {
        this.limitAmount = limitAmount;
    }

    @Override
    public String toString() {
        return "Budget{" +
                "budgetId='" + budgetId + '\'' +
                ", userId='" + userId + '\'' +
                ", category='" + category + '\'' +
                ", period='" + period + '\'' +
                ", limitAmount=" + limitAmount +
                '}';
    }
}
