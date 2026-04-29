CREATE TABLE IF NOT EXISTS users (
    user_id TEXT PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    email TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS groups (
    group_id TEXT PRIMARY KEY,
    group_name TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS group_members (
    group_id TEXT NOT NULL,
    user_id TEXT NOT NULL,
    PRIMARY KEY (group_id, user_id),
    FOREIGN KEY (group_id) REFERENCES groups(group_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS expenses (
    expense_id TEXT PRIMARY KEY,
    group_id TEXT NOT NULL,
    description TEXT NOT NULL,
    amount REAL NOT NULL,
    paid_by_user_id TEXT NOT NULL,
    split_type TEXT NOT NULL,
    category TEXT NOT NULL,
    expense_date TEXT NOT NULL,
    FOREIGN KEY (group_id) REFERENCES groups(group_id),
    FOREIGN KEY (paid_by_user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS expense_splits (
    expense_id TEXT NOT NULL,
    user_id TEXT NOT NULL,
    share_amount REAL NOT NULL,
    PRIMARY KEY (expense_id, user_id),
    FOREIGN KEY (expense_id) REFERENCES expenses(expense_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS settlements (
    settlement_id INTEGER PRIMARY KEY AUTOINCREMENT,
    group_id TEXT NOT NULL,
    from_user_id TEXT NOT NULL,
    to_user_id TEXT NOT NULL,
    amount REAL NOT NULL,
    settled_at TEXT NOT NULL,
    FOREIGN KEY (group_id) REFERENCES groups(group_id)
);

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
    group_id TEXT NOT NULL,
    transaction_type TEXT NOT NULL,
    reference_id TEXT NOT NULL,
    details TEXT,
    amount REAL NOT NULL,
    created_at TEXT NOT NULL,
    FOREIGN KEY (group_id) REFERENCES groups(group_id)
);
