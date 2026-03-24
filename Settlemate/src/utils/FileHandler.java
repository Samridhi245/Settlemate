package utils;

import models.AppData;
import models.Budget;
import models.Expense;
import models.Group;
import models.SplitType;
import models.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileHandler {
    private final Path filePath;
    private static final String VERSION = "SETTLEMATE_TXT_V3";
    private static final String LEGACY_V2 = "SETTLEMATE_TXT_V2";
    private static final String LEGACY_V1 = "SETTLEMATE_TXT_V1";

    public FileHandler(String fileName) {
        this.filePath = Path.of(fileName);
    }

    public void save(AppData data) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(VERSION);

        for (User user : data.getUsers().values()) {
            lines.add(String.join("|",
                    "USER",
                    user.getUserId(),
                    escape(user.getName()),
                    escape(user.getEmail())));
        }

        for (Group group : data.getGroups().values()) {
            lines.add(String.join("|",
                    "GROUP",
                    group.getGroupId(),
                    escape(group.getGroupName()),
                    String.join(",", group.getMemberUserIds()),
                    String.join(",", group.getExpenseIds())));
        }

        for (Expense expense : data.getExpenses().values()) {
            lines.add(String.join("|",
                    "EXPENSE",
                    expense.getExpenseId(),
                    expense.getGroupId(),
                    escape(expense.getDescription()),
                    Double.toString(expense.getAmount()),
                    expense.getPaidByUserId(),
                    expense.getSplitType().name(),
                    escape(expense.getCategory()),
                    expense.getDate().toString(),
                    serializeSplitDetails(expense.getSplitDetails())));
        }

        for (Budget budget : data.getBudgets().values()) {
            lines.add(String.join("|",
                    "BUDGET",
                    budget.getBudgetId(),
                    budget.getUserId(),
                    escape(budget.getCategory()),
                    budget.getPeriod(),
                    Double.toString(budget.getLimitAmount())));
        }
        Files.write(filePath, lines);
    }

    public AppData load() throws IOException {
        if (!Files.exists(filePath)) {
            return new AppData();
        }

        List<String> lines = Files.readAllLines(filePath);
        if (lines.isEmpty()) {
            return new AppData();
        }

        String fileVersion = lines.get(0);
        if (!VERSION.equals(fileVersion) && !LEGACY_V2.equals(fileVersion) && !LEGACY_V1.equals(fileVersion)) {
            throw new IOException("Unsupported data format in file: " + filePath);
        }
        boolean legacyV1 = LEGACY_V1.equals(fileVersion);
        boolean legacyV2 = LEGACY_V2.equals(fileVersion);

        AppData appData = new AppData();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.trim().isEmpty()) {
                continue;
            }

            String[] parts = line.split("\\|", -1);
            String recordType = parts[0];
            switch (recordType) {
                case "USER":
                    if (parts.length >= 4) {
                        String name = legacyV1 ? decode(parts[2]) : unescape(parts[2]);
                        String email = legacyV1 ? decode(parts[3]) : unescape(parts[3]);
                        User user = new User(parts[1], name, email);
                        appData.getUsers().put(user.getUserId(), user);
                    }
                    break;
                case "GROUP":
                    if (parts.length >= 5) {
                        String groupName = legacyV2 || legacyV1 ? decode(parts[2]) : unescape(parts[2]);
                        Group group = new Group(parts[1], groupName);
                        if (!parts[3].isEmpty()) {
                            for (String memberId : parts[3].split(",")) {
                                if (!memberId.isEmpty()) {
                                    group.getMemberUserIds().add(memberId);
                                }
                            }
                        }
                        if (!parts[4].isEmpty()) {
                            for (String expenseId : parts[4].split(",")) {
                                if (!expenseId.isEmpty()) {
                                    group.getExpenseIds().add(expenseId);
                                }
                            }
                        }
                        appData.getGroups().put(group.getGroupId(), group);
                    }
                    break;
                case "EXPENSE":
                    if (parts.length >= 10) {
                        String description = legacyV2 || legacyV1 ? decode(parts[3]) : unescape(parts[3]);
                        String category = legacyV2 || legacyV1 ? decode(parts[7]) : unescape(parts[7]);
                        Expense expense = new Expense(
                                parts[1],
                                parts[2],
                                description,
                                Double.parseDouble(parts[4]),
                                parts[5],
                                SplitType.valueOf(parts[6]),
                                deserializeSplitDetails(parts[9]),
                                category,
                                LocalDate.parse(parts[8])
                        );
                        appData.getExpenses().put(expense.getExpenseId(), expense);
                    }
                    break;
                case "BUDGET":
                    if (parts.length >= 6) {
                        String category = legacyV2 || legacyV1 ? decode(parts[3]) : unescape(parts[3]);
                        Budget budget = new Budget(
                                parts[1],
                                parts[2],
                                category,
                                parts[4],
                                Double.parseDouble(parts[5])
                        );
                        appData.getBudgets().put(budget.getBudgetId(), budget);
                    }
                    break;
                default:
                    break;
            }
        }
        return appData;
    }

    private static String serializeSplitDetails(Map<String, Double> splitDetails) {
        if (splitDetails.isEmpty()) {
            return "";
        }
        List<String> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : splitDetails.entrySet()) {
            entries.add(entry.getKey() + ":" + entry.getValue());
        }
        return String.join(",", entries);
    }

    private static Map<String, Double> deserializeSplitDetails(String raw) {
        Map<String, Double> result = new HashMap<>();
        if (raw == null || raw.isEmpty()) {
            return result;
        }
        String[] entries = raw.split(",");
        for (String entry : entries) {
            if (entry.isEmpty()) {
                continue;
            }
            String[] kv = entry.split(":", 2);
            if (kv.length == 2 && !kv[0].isEmpty()) {
                result.put(kv[0], Double.parseDouble(kv[1]));
            }
        }
        return result;
    }

    private static String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("|", "\\p")
                .replace(",", "\\c")
                .replace(":", "\\d");
    }

    private static String unescape(String value) {
        StringBuilder sb = new StringBuilder();
        boolean escaping = false;
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (escaping) {
                if (ch == 'p') {
                    sb.append('|');
                } else if (ch == 'c') {
                    sb.append(',');
                } else if (ch == 'd') {
                    sb.append(':');
                } else if (ch == '\\') {
                    sb.append('\\');
                } else {
                    sb.append(ch);
                }
                escaping = false;
            } else if (ch == '\\') {
                escaping = true;
            } else {
                sb.append(ch);
            }
        }
        if (escaping) {
            sb.append('\\');
        }
        return sb.toString();
    }

    private static String decode(String value) {
        return new String(java.util.Base64.getDecoder().decode(value));
    }
}
