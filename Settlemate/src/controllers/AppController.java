package controllers;

import db.DatabaseManager;
import db.dao.ExpenseDao;
import db.dao.GroupDao;
import db.dao.SettlementDao;
import db.dao.TransactionDao;
import db.dao.UserDao;
import exceptions.InsufficientMembersException;
import exceptions.InvalidSplitException;
import exceptions.UserNotFoundException;
import models.AppData;
import models.Budget;
import models.Expense;
import models.Group;
import models.SettlementTransaction;
import models.SplitType;
import models.User;
import services.BudgetService;
import services.ExpenseService;
import services.GroupService;
import services.SettlementService;
import services.UserService;
import utils.FileHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppController {
    private final AppData appData;
    private final FileHandler fileHandler;
    private final UserService userService;
    private final GroupService groupService;
    private final ExpenseService expenseService;
    private final SettlementService settlementService;
    private final BudgetService budgetService;
    private final DatabaseManager databaseManager;
    private final UserDao userDao;
    private final GroupDao groupDao;
    private final ExpenseDao expenseDao;
    private final SettlementDao settlementDao;
    private final TransactionDao transactionDao;
    private User currentUser;
    private ExpenseDraft pendingExpenseDraft;

    public AppController(String dataFile) throws IOException {
        this.fileHandler = new FileHandler(dataFile);
        this.appData = fileHandler.load();
        this.userService = new UserService(appData);
        this.groupService = new GroupService(appData, userService);
        this.expenseService = new ExpenseService(appData, groupService, userService);
        this.settlementService = new SettlementService(groupService, expenseService);
        this.budgetService = new BudgetService(appData);
        this.databaseManager = new DatabaseManager("settlemate.db", "database/schema.sql");
        this.userDao = new UserDao(databaseManager);
        this.groupDao = new GroupDao(databaseManager);
        this.expenseDao = new ExpenseDao(databaseManager);
        this.settlementDao = new SettlementDao(databaseManager);
        this.transactionDao = new TransactionDao(databaseManager);
        try {
            databaseManager.initialize();
            syncInMemoryDataToDatabase();
        } catch (SQLException e) {
            throw new IOException("Database initialization failed: " + e.getMessage(), e);
        }
    }

    public User registerUser(String name, String email) {
        User user = userService.createUser(name, email);
        try {
            userDao.upsert(user);
        } catch (SQLException e) {
            throw new IllegalArgumentException("Could not persist user to DB: " + e.getMessage());
        }
        return user;
    }

    public User loginByName(String name) {
        User match = null;
        for (User user : userService.getAllUsers()) {
            if (user.getName().equalsIgnoreCase(name.trim())) {
                if (match != null) {
                    throw new IllegalArgumentException("Multiple users found with this name. Use unique names.");
                }
                match = user;
            }
        }
        if (match == null) {
            throw new IllegalArgumentException("User not found. Please register first.");
        }
        this.currentUser = match;
        return match;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>(userService.getAllUsers());
        users.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        return users;
    }

    public List<Group> getAllGroups() {
        List<Group> groups = new ArrayList<>(groupService.getAllGroups());
        groups.sort((a, b) -> a.getGroupName().compareToIgnoreCase(b.getGroupName()));
        return groups;
    }

    public Group createGroup(String groupName, List<String> memberUserIds) {
        Group group = groupService.createGroup(groupName);
        for (String memberUserId : memberUserIds) {
            try {
                groupService.addMember(group.getGroupId(), memberUserId);
            } catch (UserNotFoundException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
        try {
            groupDao.upsert(group);
        } catch (SQLException e) {
            throw new IllegalArgumentException("Could not persist group to DB: " + e.getMessage());
        }
        return group;
    }

    public void deleteGroup(String groupId) {
        appData.getGroups().remove(groupId);
        try {
            groupDao.deleteById(groupId);
        } catch (SQLException e) {
            throw new IllegalArgumentException("Could not delete group from DB: " + e.getMessage());
        }
    }

    public Group updateGroup(String groupId, String newName, List<String> memberUserIds) {
        Group group = groupService.getGroupById(groupId);
        group.setGroupName(newName);
        group.getMemberUserIds().clear();
        for (String memberId : memberUserIds) {
            try {
                groupService.addMember(groupId, memberId);
            } catch (UserNotFoundException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
        try {
            groupDao.upsert(group);
        } catch (SQLException e) {
            throw new IllegalArgumentException("Could not update group in DB: " + e.getMessage());
        }
        return group;
    }

    public Expense addEqualExpense(String groupId,
                                   String description,
                                   double amount,
                                   String paidByUserId,
                                   String category,
                                   LocalDate date) {
        try {
            Expense expense = expenseService.addExpense(groupId, description, amount, paidByUserId, SplitType.EQUAL,
                    Collections.emptyMap(), category, date);
            persistExpenseAndTransaction(expense);
            return expense;
        } catch (InvalidSplitException | InsufficientMembersException | UserNotFoundException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public void createPendingSplitExpense(String groupId,
                                          String description,
                                          double amount,
                                          String paidByUserId,
                                          SplitType splitType,
                                          String category,
                                          LocalDate date) {
        this.pendingExpenseDraft = new ExpenseDraft(groupId, description, amount, paidByUserId, splitType, category, date);
    }

    public ExpenseDraft getPendingExpenseDraft() {
        return pendingExpenseDraft;
    }

    public Expense submitPendingSplitExpense(Map<String, Double> splitDetails) {
        if (pendingExpenseDraft == null) {
            throw new IllegalStateException("No pending split expense found.");
        }
        try {
            Expense expense = expenseService.addExpense(
                    pendingExpenseDraft.groupId,
                    pendingExpenseDraft.description,
                    pendingExpenseDraft.amount,
                    pendingExpenseDraft.paidByUserId,
                    pendingExpenseDraft.splitType,
                    splitDetails,
                    pendingExpenseDraft.category,
                    pendingExpenseDraft.date
            );
            persistExpenseAndTransaction(expense);
            pendingExpenseDraft = null;
            return expense;
        } catch (InvalidSplitException | InsufficientMembersException | UserNotFoundException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public List<String> getMemberIdsByGroup(String groupId) {
        return new ArrayList<>(groupService.getGroupById(groupId).getMemberUserIds());
    }

    public Map<String, Double> getBalances(String groupId) {
        return settlementService.calculateNetBalances(groupId);
    }

    public List<SettlementTransaction> getSettlementSuggestions(String groupId) {
        return settlementService.simplifySettlements(groupId);
    }

    public int settleDebts(String groupId) {
        List<SettlementTransaction> suggestions = settlementService.simplifySettlements(groupId);
        if (suggestions.isEmpty()) {
            return 0;
        }
        try {
            settlementDao.insertSettlements(groupId, suggestions);
            for (SettlementTransaction settlement : suggestions) {
                String details = getUserName(settlement.getFromUserId()) + " -> " +
                        getUserName(settlement.getToUserId());
                transactionDao.insert(groupId, "SETTLEMENT", "SET-" + LocalDateTime.now(), details, settlement.getAmount());
            }

            // Clear settled expenses from in-memory group so balances reset to zero
            Group group = groupService.getGroupById(groupId);
            for (String expenseId : new ArrayList<>(group.getExpenseIds())) {
                appData.getExpenses().remove(expenseId);
            }
            group.getExpenseIds().clear();

            return suggestions.size();
        } catch (SQLException e) {
            throw new IllegalArgumentException("Could not persist settlements to DB: " + e.getMessage());
        }
    }

    public void settleIndividual(String groupId, String fromUserId, String toUserId, double amount) {
        // --- Defensive Check 1: amount must be positive ---
        if (amount <= 0) {
            throw new IllegalArgumentException("Settlement amount must be greater than 0.");
        }

        // --- Defensive Check 2: amount must not exceed what is actually owed ---
        Map<String, Double> currentBalances = settlementService.calculateNetBalances(groupId);
        double debtorBalance = currentBalances.getOrDefault(fromUserId, 0.0);  // negative = owes money
        double remainingDebt = -debtorBalance; // convert to positive "amount owed"

        System.out.printf("[SettleIndividual] Before settlement:%n");
        System.out.printf("  Debtor  (%s) net balance : %.2f  (owes: %.2f)%n",
                getUserName(fromUserId), debtorBalance, remainingDebt);
        System.out.printf("  Paid amount requested    : %.2f%n", amount);

        if (amount > remainingDebt + 0.01) { // +0.01 tolerance for floating-point
            throw new IllegalArgumentException(String.format(
                    "Cannot settle %.2f — %s only owes %.2f to %s.",
                    amount, getUserName(fromUserId), remainingDebt, getUserName(toUserId)));
        }

        // Clamp to remaining debt to avoid floating-point overshoot
        double amountToSettle = Math.min(amount, remainingDebt);

        try {
            List<SettlementTransaction> single = new java.util.ArrayList<>();
            single.add(new models.SettlementTransaction(fromUserId, toUserId, amountToSettle));
            settlementDao.insertSettlements(groupId, single);

            String details = getUserName(fromUserId) + " paid " + getUserName(toUserId);
            transactionDao.insert(groupId, "PARTIAL_SETTLEMENT",
                    "PSET-" + LocalDateTime.now(), details, amountToSettle);

            // ---------------------------------------------------------------
            // SYNTHETIC EXPENSE — correct direction:
            //
            // calculateNetBalances logic:
            //   payer           → balance += amount
            //   splitDetails[k] → balance -= value
            //
            // We want:
            //   fromUserId (debtor,   balance is -ve) : += amountToSettle → debt shrinks toward 0 ✓
            //   toUserId   (creditor, balance is +ve) : -= amountToSettle → credit shrinks toward 0 ✓
            //
            // Therefore: payer = fromUserId, splitDetails = { toUserId: amountToSettle }
            // ---------------------------------------------------------------
            java.util.Map<String, Double> splitDetails = new java.util.HashMap<>();
            splitDetails.put(toUserId, amountToSettle);   // creditor's share is reduced

            models.Expense synthetic = new models.Expense(
                    utils.IdGenerator.generateId("SYN"), groupId,
                    "Settlement: " + getUserName(fromUserId) + " -> " + getUserName(toUserId),
                    amountToSettle,
                    fromUserId,                  // ← debtor is "payer" of synthetic expense
                    models.SplitType.EXACT,
                    splitDetails,
                    "Settlement",
                    java.time.LocalDate.now());

            appData.getExpenses().put(synthetic.getExpenseId(), synthetic);
            groupService.getGroupById(groupId).getExpenseIds().add(synthetic.getExpenseId());

            // --- Debug log: show updated balances after settlement ---
            Map<String, Double> updatedBalances = settlementService.calculateNetBalances(groupId);
            double updatedDebt = -updatedBalances.getOrDefault(fromUserId, 0.0);
            System.out.printf("[SettleIndividual] After settlement:%n");
            System.out.printf("  Paid                     : %.2f%n", amountToSettle);
            System.out.printf("  Debtor (%s) remaining debt: %.2f%n",
                    getUserName(fromUserId), Math.max(0.0, updatedDebt));
            System.out.printf("  %s%n",
                    updatedDebt <= 0.01 ? "✔ Debt fully settled." : "Partial payment recorded.");

        } catch (SQLException e) {
            throw new IllegalArgumentException("Could not record settlement: " + e.getMessage());
        }
    }

    public double getFairnessScore(String groupId) {
        return settlementService.calculateFinancialFairnessIndex(groupId);
    }

    public List<Expense> getExpensesByGroup(String groupId) {
        return expenseService.getExpensesByGroup(groupId);
    }

    public List<models.Group> getGroupsForCurrentUser() {
        if (currentUser == null) return getAllGroups();
        List<models.Group> result = new java.util.ArrayList<>();
        for (models.Group g : groupService.getAllGroups()) {
            if (g.getMemberUserIds().contains(currentUser.getUserId())) {
                result.add(g);
            }
        }
        return result;
    }

    public double getTotalExpensesAmount(String groupId) {
        double total = 0.0;
        for (Expense expense : getExpensesByGroup(groupId)) {
            total += expense.getAmount();
        }
        return total;
    }

    public Map<String, Double> getCategoryTotals(String groupId) {
        Map<String, Double> totals = new HashMap<>();
        for (Expense expense : getExpensesByGroup(groupId)) {
            totals.put(expense.getCategory(), totals.getOrDefault(expense.getCategory(), 0.0) + expense.getAmount());
        }
        return totals;
    }

    public List<String> getTransactionHistory(String groupId) {
        try {
            return transactionDao.getHistoryByGroup(groupId);
        } catch (SQLException e) {
            throw new IllegalArgumentException("Could not fetch DB history: " + e.getMessage());
        }
    }

    public String getUserName(String userId) {
        try {
            return userService.getUserById(userId).getName();
        } catch (UserNotFoundException e) {
            return userId;
        }
    }

    public Map<String, String> getUserNameMap() {
        Map<String, String> nameMap = new HashMap<>();
        for (User user : userService.getAllUsers()) {
            nameMap.put(user.getUserId(), user.getName());
        }
        return nameMap;
    }

    public void saveData() throws IOException {
        fileHandler.save(appData);
    }

    public Budget setBudget(String userId, String category, String period, double amount) {
        return budgetService.setBudget(userId, category, period, amount);
    }

    public List<Budget> getBudgetsByUser(String userId) {
        return budgetService.getBudgetsByUser(userId);
    }

    public double getSpentForBudget(Budget budget) {
        return budgetService.calculateSpentForBudget(budget);
    }

    public List<String> getBudgetAlerts(String userId) {
        return budgetService.getBudgetAlerts(userId);
    }

    private void syncInMemoryDataToDatabase() throws SQLException {
        for (User user : appData.getUsers().values()) {
            userDao.upsert(user);
        }
        for (Group group : appData.getGroups().values()) {
            groupDao.upsert(group);
        }
        for (Expense expense : appData.getExpenses().values()) {
            try {
                expenseDao.insertExpenseWithSplits(expense);
                transactionDao.insert(expense.getGroupId(), "EXPENSE", expense.getExpenseId(),
                        expense.getDescription(), expense.getAmount());
            } catch (SQLException ignored) {
                // Ignore duplicates from prior app runs.
            }
        }
    }

    private void persistExpenseAndTransaction(Expense expense) {
        try {
            expenseDao.insertExpenseWithSplits(expense);
            transactionDao.insert(expense.getGroupId(), "EXPENSE", expense.getExpenseId(),
                    expense.getDescription(), expense.getAmount());
        } catch (SQLException e) {
            throw new IllegalArgumentException("Could not persist expense to DB: " + e.getMessage());
        }
    }

    public static class ExpenseDraft {
        public final String groupId;
        public final String description;
        public final double amount;
        public final String paidByUserId;
        public final SplitType splitType;
        public final String category;
        public final LocalDate date;

        public ExpenseDraft(String groupId,
                           String description,
                           double amount,
                           String paidByUserId,
                           SplitType splitType,
                           String category,
                           LocalDate date) {
            this.groupId = groupId;
            this.description = description;
            this.amount = amount;
            this.paidByUserId = paidByUserId;
            this.splitType = splitType;
            this.category = category;
            this.date = date;
        }
    }
}
