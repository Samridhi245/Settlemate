# SettleMate - Expense Sharing System

Console-based Java backend for shared expense management with OOP design.

## Features
- User and group management
- Expense tracking with split strategies: EQUAL, EXACT, PERCENTAGE
- Net balance calculation and simplified settlement suggestions
- Financial Fairness Index
- Budget guard (weekly/monthly per category) with warnings/alerts
- Analytics: category summaries, monthly trends, text report
- Data persistence using Java serialization

## Project Structure
- `src/Main.java`
- `src/models/`
- `src/services/`
- `src/strategies/`
- `src/utils/`
- `src/exceptions/`

## Build and Run
From project root:

```powershell
javac -d out (Get-ChildItem -Recurse -File src\*.java | ForEach-Object { $_.FullName })
java -cp out Main
```

Data is persisted in `settlemate-data.ser` at the project root.
