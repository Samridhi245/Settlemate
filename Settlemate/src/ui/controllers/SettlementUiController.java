package ui.controllers;

import controllers.AppController;
import models.Group;
import models.SettlementTransaction;

import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.Map;

public class SettlementUiController {
    private final AppController appController;

    public SettlementUiController(AppController appController) {
        this.appController = appController;
    }

    public List<Group> getAllGroups() {
        return appController.getAllGroups();
    }

    public List<SettlementTransaction> getSettlements(String groupId) {
        return appController.getSettlementSuggestions(groupId);
    }

    public DefaultTableModel getBalancesTableModel(String groupId) {
        DefaultTableModel model = new DefaultTableModel(new Object[]{"Member", "Net Balance"}, 0);
        Map<String, Double> balances = appController.getBalances(groupId);
        for (Map.Entry<String, Double> entry : balances.entrySet()) {
            model.addRow(new Object[]{appController.getUserName(entry.getKey()), String.format("%.2f", entry.getValue())});
        }
        return model;
    }

    public DefaultTableModel getSettlementsTableModel(String groupId) {
        DefaultTableModel model = new DefaultTableModel(new Object[]{"From", "To", "Amount"}, 0);
        List<SettlementTransaction> rows = appController.getSettlementSuggestions(groupId);
        for (SettlementTransaction row : rows) {
            model.addRow(new Object[]{
                    appController.getUserName(row.getFromUserId()),
                    appController.getUserName(row.getToUserId()),
                    String.format("%.2f", row.getAmount())
            });
        }
        return model;
    }

    public double getFairnessScore(String groupId) {
        return appController.getFairnessScore(groupId);
    }

    public int settleDebts(String groupId) {
        return appController.settleDebts(groupId);
    }

    public void settleIndividual(String groupId, String fromUserId, String toUserId, double amount) {
        appController.settleIndividual(groupId, fromUserId, toUserId, amount);
    }

    public String getUserName(String userId) {
        return appController.getUserName(userId);
    }
}
