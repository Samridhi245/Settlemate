package models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AppData implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<String, User> users;
    private final Map<String, Group> groups;
    private final Map<String, Expense> expenses;
    private final Map<String, Budget> budgets;

    public AppData() {
        this.users = new HashMap<>();
        this.groups = new HashMap<>();
        this.expenses = new HashMap<>();
        this.budgets = new HashMap<>();
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public Map<String, Group> getGroups() {
        return groups;
    }

    public Map<String, Expense> getExpenses() {
        return expenses;
    }

    public Map<String, Budget> getBudgets() {
        return budgets;
    }
}
