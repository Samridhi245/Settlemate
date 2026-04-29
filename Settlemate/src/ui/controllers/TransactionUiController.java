package ui.controllers;

import controllers.AppController;
import models.Group;

import javax.swing.table.DefaultTableModel;
import java.util.List;

public class TransactionUiController {
    private final AppController appController;

    public TransactionUiController(AppController appController) {
        this.appController = appController;
    }

    public List<Group> getAllGroups() {
        return appController.getAllGroups();
    }

    public DefaultTableModel getHistoryTableModel(String groupId) {
        DefaultTableModel model = new DefaultTableModel(new Object[]{"DateTime", "Type", "Amount", "Details"}, 0);
        List<String> rows = appController.getTransactionHistory(groupId);
        for (String row : rows) {
            String[] parts = row.split(" \\| ", 4);
            if (parts.length == 4) {
                model.addRow(new Object[]{parts[0], parts[1], parts[2], parts[3]});
            } else {
                model.addRow(new Object[]{row, "", "", ""});
            }
        }
        return model;
    }
}
