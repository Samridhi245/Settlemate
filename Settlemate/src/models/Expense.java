package models;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Expense implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String expenseId;
    private final String groupId;
    private final String description;
    private final double amount;
    private final String paidByUserId;
    private final SplitType splitType;
    private final Map<String, Double> splitDetails;
    private final String category;
    private final LocalDate date;

    public Expense(String expenseId,
                   String groupId,
                   String description,
                   double amount,
                   String paidByUserId,
                   SplitType splitType,
                   Map<String, Double> splitDetails,
                   String category,
                   LocalDate date) {
        this.expenseId = expenseId;
        this.groupId = groupId;
        this.description = description;
        this.amount = amount;
        this.paidByUserId = paidByUserId;
        this.splitType = splitType;
        this.splitDetails = new HashMap<>(splitDetails);
        this.category = category;
        this.date = date;
    }

    public String getExpenseId() {
        return expenseId;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public String getPaidByUserId() {
        return paidByUserId;
    }

    public SplitType getSplitType() {
        return splitType;
    }

    public Map<String, Double> getSplitDetails() {
        return splitDetails;
    }

    public String getCategory() {
        return category;
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "Expense{" +
                "expenseId='" + expenseId + '\'' +
                ", groupId='" + groupId + '\'' +
                ", description='" + description + '\'' +
                ", amount=" + amount +
                ", paidByUserId='" + paidByUserId + '\'' +
                ", splitType=" + splitType +
                ", category='" + category + '\'' +
                ", date=" + date +
                '}';
    }
}
