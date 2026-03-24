package services;

import exceptions.InsufficientMembersException;
import exceptions.InvalidSplitException;
import exceptions.UserNotFoundException;
import models.AppData;
import models.Expense;
import models.Group;
import models.SplitType;
import strategies.EqualSplitStrategy;
import strategies.ExactSplitStrategy;
import strategies.PercentageSplitStrategy;
import strategies.SplitStrategy;
import utils.IdGenerator;
import utils.InputValidator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenseService {
    private final AppData appData;
    private final GroupService groupService;
    private final UserService userService;
    private final Map<SplitType, SplitStrategy> strategyMap;

    public ExpenseService(AppData appData, GroupService groupService, UserService userService) {
        this.appData = appData;
        this.groupService = groupService;
        this.userService = userService;
        this.strategyMap = new HashMap<>();
        strategyMap.put(SplitType.EQUAL, new EqualSplitStrategy());
        strategyMap.put(SplitType.EXACT, new ExactSplitStrategy());
        strategyMap.put(SplitType.PERCENTAGE, new PercentageSplitStrategy());
    }

    public Expense addExpense(String groupId,
                              String description,
                              double amount,
                              String paidByUserId,
                              SplitType splitType,
                              Map<String, Double> inputSplitDetails,
                              String category,
                              LocalDate date) throws InvalidSplitException, InsufficientMembersException, UserNotFoundException {
        InputValidator.requireNonBlank(description, "Description");
        InputValidator.requireNonBlank(category, "Category");
        InputValidator.requirePositive(amount, "Amount");

        Group group = groupService.getGroupById(groupId);
        if (group.getMemberUserIds().size() < 2) {
            throw new InsufficientMembersException("A group requires at least 2 members to add expense.");
        }
        if (!group.getMemberUserIds().contains(paidByUserId)) {
            throw new IllegalArgumentException("Payer is not a member of this group.");
        }
        userService.getUserById(paidByUserId);

        List<String> participants = group.getMemberUserIds();
        SplitStrategy strategy = strategyMap.get(splitType);
        if (strategy == null) {
            throw new InvalidSplitException("Unsupported split type.");
        }

        Map<String, Double> shares = strategy.calculateShares(amount, participants, inputSplitDetails);
        String expenseId = IdGenerator.generateId("EXP");
        Expense expense = new Expense(expenseId, groupId, description.trim(), amount, paidByUserId, splitType, shares, category.trim(), date);
        appData.getExpenses().put(expenseId, expense);
        group.getExpenseIds().add(expenseId);
        return expense;
    }

    public List<Expense> getExpensesByGroup(String groupId) {
        Group group = groupService.getGroupById(groupId);
        List<Expense> list = new ArrayList<>();
        for (String expenseId : group.getExpenseIds()) {
            Expense expense = appData.getExpenses().get(expenseId);
            if (expense != null) {
                list.add(expense);
            }
        }
        return list;
    }
}
