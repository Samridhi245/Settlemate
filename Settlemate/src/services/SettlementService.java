package services;

import models.Expense;
import models.Group;
import models.SettlementTransaction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class SettlementService {
    private final GroupService groupService;
    private final ExpenseService expenseService;

    public SettlementService(GroupService groupService, ExpenseService expenseService) {
        this.groupService = groupService;
        this.expenseService = expenseService;
    }

    public Map<String, Double> calculateNetBalances(String groupId) {
        Group group = groupService.getGroupById(groupId);
        Map<String, Double> balances = new HashMap<>();
        for (String userId : group.getMemberUserIds()) {
            balances.put(userId, 0.0);
        }

        List<Expense> expenses = expenseService.getExpensesByGroup(groupId);
        for (Expense expense : expenses) {
            balances.put(expense.getPaidByUserId(),
                    balances.getOrDefault(expense.getPaidByUserId(), 0.0) + expense.getAmount());

            for (Map.Entry<String, Double> entry : expense.getSplitDetails().entrySet()) {
                balances.put(entry.getKey(), balances.getOrDefault(entry.getKey(), 0.0) - entry.getValue());
            }
        }
        return balances;
    }

    public List<SettlementTransaction> simplifySettlements(String groupId) {
        Map<String, Double> balances = calculateNetBalances(groupId);
        PriorityQueue<Map.Entry<String, Double>> creditors = new PriorityQueue<>(
                Comparator.comparingDouble(Map.Entry<String, Double>::getValue).reversed()
        );
        PriorityQueue<Map.Entry<String, Double>> debtors = new PriorityQueue<>(
                Comparator.comparingDouble(Map.Entry<String, Double>::getValue)
        );

        for (Map.Entry<String, Double> entry : balances.entrySet()) {
            if (entry.getValue() > 0.01) {
                creditors.offer(entry);
            } else if (entry.getValue() < -0.01) {
                debtors.offer(entry);
            }
        }

        List<SettlementTransaction> settlements = new ArrayList<>();
        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            Map.Entry<String, Double> creditor = creditors.poll();
            Map.Entry<String, Double> debtor = debtors.poll();

            double payable = Math.min(creditor.getValue(), -debtor.getValue());
            settlements.add(new SettlementTransaction(debtor.getKey(), creditor.getKey(), payable));

            double creditorBalance = creditor.getValue() - payable;
            double debtorBalance = debtor.getValue() + payable;

            if (creditorBalance > 0.01) {
                creditors.offer(Map.entry(creditor.getKey(), creditorBalance));
            }
            if (debtorBalance < -0.01) {
                debtors.offer(Map.entry(debtor.getKey(), debtorBalance));
            }
        }
        return settlements;
    }

    public double calculateFinancialFairnessIndex(String groupId) {
        List<Expense> expenses = expenseService.getExpensesByGroup(groupId);
        if (expenses.isEmpty()) {
            return 100.0;
        }

        Group group = groupService.getGroupById(groupId);
        Map<String, Double> paidTotals = new HashMap<>();
        for (String userId : group.getMemberUserIds()) {
            paidTotals.put(userId, 0.0);
        }

        double total = 0.0;
        for (Expense expense : expenses) {
            total += expense.getAmount();
            paidTotals.put(expense.getPaidByUserId(), paidTotals.getOrDefault(expense.getPaidByUserId(), 0.0) + expense.getAmount());
        }

        double ideal = total / group.getMemberUserIds().size();
        double deviation = 0.0;
        for (String userId : group.getMemberUserIds()) {
            deviation += Math.abs(paidTotals.getOrDefault(userId, 0.0) - ideal);
        }

        double maxDeviation = 2 * total;
        double score = (1 - (deviation / maxDeviation)) * 100.0;
        return Math.max(0.0, Math.min(100.0, score));
    }
}
