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
import services.AnalyticsService;
import services.BudgetService;
import services.ExpenseService;
import services.GroupService;
import services.SettlementService;
import services.UserService;
import utils.FileHandler;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private static final String DATA_FILE = "settlemate-data.txt";

    public static void main(String[] args) {
        FileHandler fileHandler = new FileHandler(DATA_FILE);
        AppData appData;
        try {
            appData = fileHandler.load();
            System.out.println("Data loaded successfully.");
        } catch (IOException e) {
            System.out.println("No existing data found. Starting fresh.");
            appData = new AppData();
        }

        UserService userService = new UserService(appData);
        GroupService groupService = new GroupService(appData, userService);
        ExpenseService expenseService = new ExpenseService(appData, groupService, userService);
        SettlementService settlementService = new SettlementService(groupService, expenseService);
        BudgetService budgetService = new BudgetService(appData);
        AnalyticsService analyticsService = new AnalyticsService(appData);

        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                printMainMenu();
                String choice = scanner.nextLine().trim();
                try {
                    switch (choice) {
                        case "1":
                            handleUserManagement(scanner, userService);
                            break;
                        case "2":
                            handleGroupManagement(scanner, groupService, userService);
                            break;
                        case "3":
                            handleExpenseManagement(scanner, expenseService, groupService, userService);
                            break;
                        case "4":
                            handleBalances(scanner, settlementService, userService, groupService);
                            break;
                        case "5":
                            handleBudgetManagement(scanner, budgetService, userService);
                            break;
                        case "6":
                            handleAnalytics(scanner, analyticsService, groupService);
                            break;
                        case "7":
                            fileHandler.save(appData);
                            System.out.println("Data saved. Exiting SettleMate. Goodbye!");
                            running = false;
                            break;
                        default:
                            System.out.println("Invalid option. Try again.");
                    }
                } catch (Exception e) {
                    System.out.println("Operation failed: " + e.getMessage());
                }
            }
        }
    }

    private static void printMainMenu() {
        System.out.println("\n==== SettleMate Menu ====");
        System.out.println("1. User Management");
        System.out.println("2. Group Management");
        System.out.println("3. Expense Management");
        System.out.println("4. View Balances & Settlements");
        System.out.println("5. Budget Management");
        System.out.println("6. Analytics");
        System.out.println("7. Save & Exit");
        System.out.print("Enter choice: ");
    }

    private static void handleUserManagement(Scanner scanner, UserService userService) {
        System.out.println("\n1. Create User");
        System.out.println("2. View Users");
        System.out.print("Enter choice: ");
        String c = scanner.nextLine().trim();
        if ("1".equals(c)) {
            System.out.print("Name: ");
            String name = scanner.nextLine();
            System.out.print("Email: ");
            String email = scanner.nextLine();
            User user = userService.createUser(name, email);
            System.out.println("Created user: " + user);
        } else if ("2".equals(c)) {
            List<User> users = userService.getAllUsers();
            if (users.isEmpty()) {
                System.out.println("No users found.");
                return;
            }
            users.sort(Comparator.comparing(User::getUserId));
            users.forEach(System.out::println);
        } else {
            System.out.println("Invalid option.");
        }
    }

    private static void handleGroupManagement(Scanner scanner, GroupService groupService, UserService userService) throws UserNotFoundException {
        System.out.println("\n1. Create Group");
        System.out.println("2. Add Member");
        System.out.println("3. Remove Member");
        System.out.println("4. View Groups");
        System.out.print("Enter choice: ");
        String c = scanner.nextLine().trim();
        switch (c) {
            case "1":
                System.out.print("Group name: ");
                Group group = groupService.createGroup(scanner.nextLine());
                System.out.println("Created group: " + group);
                break;
            case "2":
                String groupId = resolveGroupIdInput(scanner, "Group ID or Name", groupService);
                String userId = resolveUserIdInput(scanner, "User ID or Name", userService);
                groupService.addMember(groupId, userId);
                System.out.println("Member added.");
                break;
            case "3":
                String gId = resolveGroupIdInput(scanner, "Group ID or Name", groupService);
                System.out.print("User ID: ");
                String uId = scanner.nextLine().trim();
                groupService.removeMember(gId, uId);
                System.out.println("Member removed.");
                break;
            case "4":
                List<Group> groups = groupService.getAllGroups();
                if (groups.isEmpty()) {
                    System.out.println("No groups found.");
                    return;
                }
                groups.sort(Comparator.comparing(Group::getGroupId));
                groups.forEach(g -> System.out.println(g + ", members=" + formatMemberNames(g, userService)));
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    private static void handleExpenseManagement(Scanner scanner,
                                                ExpenseService expenseService,
                                                GroupService groupService,
                                                UserService userService) throws InvalidSplitException, InsufficientMembersException, UserNotFoundException {
        System.out.println("\n1. Add Expense");
        System.out.println("2. View Expenses by Group");
        System.out.print("Enter choice: ");
        String c = scanner.nextLine().trim();
        if ("1".equals(c)) {
            String groupId = resolveGroupIdInput(scanner, "Group ID or Name", groupService);
            Group group = groupService.getGroupById(groupId);

            System.out.print("Description: ");
            String description = scanner.nextLine();
            System.out.print("Amount: ");
            double amount = Double.parseDouble(scanner.nextLine().trim());
            String paidBy = resolveUserIdInput(scanner, "Paid By (User ID or Name)", userService);
            System.out.print("Category: ");
            String category = scanner.nextLine().trim();
            System.out.print("Date (YYYY-MM-DD, blank for today): ");
            String dateInput = scanner.nextLine().trim();
            LocalDate date = dateInput.isEmpty() ? LocalDate.now() : LocalDate.parse(dateInput);

            System.out.println("Split Type: 1.EQUAL 2.EXACT 3.PERCENTAGE");
            String splitChoice = scanner.nextLine().trim();
            SplitType splitType;
            if ("1".equals(splitChoice)) {
                splitType = SplitType.EQUAL;
            } else if ("2".equals(splitChoice)) {
                splitType = SplitType.EXACT;
            } else if ("3".equals(splitChoice)) {
                splitType = SplitType.PERCENTAGE;
            } else {
                throw new IllegalArgumentException("Invalid split type.");
            }

            Map<String, Double> details = new HashMap<>();
            if (splitType != SplitType.EQUAL) {
                for (String memberId : group.getMemberUserIds()) {
                    String memberName = memberId;
                    try {
                        memberName = userService.getUserById(memberId).getName();
                    } catch (UserNotFoundException ignored) {
                    }
                    if (splitType == SplitType.EXACT) {
                        System.out.print("Exact amount for " + memberName + ": ");
                    } else {
                        System.out.print("Percentage for " + memberName + ": ");
                    }
                    details.put(memberId, Double.parseDouble(scanner.nextLine().trim()));
                }
            }

            Expense expense = expenseService.addExpense(groupId, description, amount, paidBy, splitType, details, category, date);
            System.out.println("Expense added: " + expense);
        } else if ("2".equals(c)) {
            String groupId = resolveGroupIdInput(scanner, "Group ID or Name", groupService);
            List<Expense> expenses = expenseService.getExpensesByGroup(groupId);
            if (expenses.isEmpty()) {
                System.out.println("No expenses found.");
                return;
            }
            expenses.forEach(e -> System.out.println(e + ", splitDetails=" + e.getSplitDetails()));
        } else {
            System.out.println("Invalid option.");
        }
    }

    private static void handleBalances(Scanner scanner, SettlementService settlementService, UserService userService, GroupService groupService) {
        String groupId = resolveGroupIdInput(scanner, "Group ID or Name", groupService);
        Map<String, Double> balances = settlementService.calculateNetBalances(groupId);
        System.out.println("Net Balances:");
        for (Map.Entry<String, Double> e : balances.entrySet()) {
            String displayName = e.getKey();
            try {
                displayName = userService.getUserById(e.getKey()).getName() + " (" + e.getKey() + ")";
            } catch (UserNotFoundException ignored) {
            }
            System.out.println("- " + displayName + ": " + String.format("%.2f", e.getValue()));
        }

        List<SettlementTransaction> txns = settlementService.simplifySettlements(groupId);
        System.out.println("Suggested Settlements:");
        if (txns.isEmpty()) {
            System.out.println("No settlements needed.");
        } else {
            for (SettlementTransaction t : txns) {
                System.out.println("- " + t.getFromUserId() + " pays " + t.getToUserId() + " : " + String.format("%.2f", t.getAmount()));
            }
        }
        System.out.println("Financial Fairness Index: " + String.format("%.2f", settlementService.calculateFinancialFairnessIndex(groupId)));
    }

    private static void handleBudgetManagement(Scanner scanner, BudgetService budgetService, UserService userService) {
        System.out.println("\n1. Set Budget");
        System.out.println("2. View Budget Status");
        System.out.print("Enter choice: ");
        String c = scanner.nextLine().trim();
        if ("1".equals(c)) {
            String userId = resolveUserIdInput(scanner, "User ID or Name", userService);
            System.out.print("Category: ");
            String category = scanner.nextLine().trim();
            System.out.print("Period (WEEKLY/MONTHLY): ");
            String period = scanner.nextLine().trim();
            System.out.print("Budget amount: ");
            double amount = Double.parseDouble(scanner.nextLine().trim());
            Budget budget = budgetService.setBudget(userId, category, period, amount);
            System.out.println("Budget saved: " + budget);
        } else if ("2".equals(c)) {
            String userId = resolveUserIdInput(scanner, "User ID or Name", userService);
            List<Budget> budgets = budgetService.getBudgetsByUser(userId);
            if (budgets.isEmpty()) {
                System.out.println("No budgets found.");
                return;
            }
            for (Budget b : budgets) {
                double spent = budgetService.calculateSpentForBudget(b);
                System.out.println("- " + b + ", spent=" + String.format("%.2f", spent));
            }
            List<String> alerts = budgetService.getBudgetAlerts(userId);
            if (!alerts.isEmpty()) {
                System.out.println("Alerts:");
                alerts.forEach(a -> System.out.println("- " + a));
            }
        } else {
            System.out.println("Invalid option.");
        }
    }

    private static String resolveUserIdInput(Scanner scanner, String prompt, UserService userService) {
        System.out.print(prompt + ": ");
        String rawInput = scanner.nextLine().trim();
        if (rawInput.isEmpty()) {
            throw new IllegalArgumentException("User input cannot be empty.");
        }

        for (User user : userService.getAllUsers()) {
            if (user.getUserId().equalsIgnoreCase(rawInput)) {
                return user.getUserId();
            }
        }

        User matched = null;
        for (User user : userService.getAllUsers()) {
            if (user.getName().equalsIgnoreCase(rawInput)) {
                if (matched != null) {
                    throw new IllegalArgumentException("Multiple users found with name '" + rawInput + "'. Please enter User ID.");
                }
                matched = user;
            }
        }

        if (matched != null) {
            return matched.getUserId();
        }
        throw new IllegalArgumentException("User not found for input: " + rawInput);
    }

    private static void handleAnalytics(Scanner scanner, AnalyticsService analyticsService, GroupService groupService) {
        String groupId = resolveGroupIdInput(scanner, "Group ID or Name", groupService);

        Map<String, Double> category = analyticsService.getCategoryWiseSpending(groupId);
        System.out.println("Category-wise Spending:");
        if (category.isEmpty()) {
            System.out.println("No expenses found.");
        } else {
            category.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(e -> System.out.println("- " + e.getKey() + ": " + String.format("%.2f", e.getValue())));
        }

        Map<YearMonth, Double> trend = analyticsService.getSpendingTrend(groupId);
        System.out.println("Spending Trend:");
        if (trend.isEmpty()) {
            System.out.println("No trend data.");
        } else {
            trend.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(e -> System.out.println("- " + e.getKey() + ": " + String.format("%.2f", e.getValue())));
        }

        System.out.println(analyticsService.generateSummaryReport(groupId));
    }

    private static String resolveGroupIdInput(Scanner scanner, String prompt, GroupService groupService) {
        System.out.print(prompt + ": ");
        String rawInput = scanner.nextLine().trim();
        if (rawInput.isEmpty()) {
            throw new IllegalArgumentException("Group input cannot be empty.");
        }

        for (Group group : groupService.getAllGroups()) {
            if (group.getGroupId().equalsIgnoreCase(rawInput)) {
                return group.getGroupId();
            }
        }

        return groupService.getGroupByName(rawInput).getGroupId();
    }

    private static String formatMemberNames(Group group, UserService userService) {
        StringBuilder sb = new StringBuilder("[");
        List<String> memberIds = group.getMemberUserIds();
        for (int i = 0; i < memberIds.size(); i++) {
            String memberId = memberIds.get(i);
            String label = memberId;
            try {
                label = userService.getUserById(memberId).getName();
            } catch (UserNotFoundException ignored) {
            }
            sb.append(label);
            if (i < memberIds.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
