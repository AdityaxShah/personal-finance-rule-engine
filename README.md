# Personal Finance & Expense Rule Engine (Core Java)

A modular, object-oriented **Personal Finance & Expense Rule Engine** built in **Core Java** with polymorphic spending rules, custom exception handling, stream-based financial analytics, and local CSV/log file persistence.

## Overview
This project models personal income and expense workflows around an encapsulated `Account` domain entity and an extensible rule engine. Whenever an expense is recorded, the engine evaluates active policies (`CategoryBudgetRule` and `HighValueAlertRule`), logs triggered alerts, updates balances safely, and persists state to disk.

## Key Features
- **Object-Oriented Domain Model:** Abstract `Transaction` base class extended by `IncomeTransaction` and `ExpenseTransaction`, enforcing input validation and two-decimal rounding.
- **Polymorphic Rule Engine (`ExpenseRule`):**
  - `CategoryBudgetRule`: Triggers a `WARNING` alert at configurable budget utilization (default `80%`) and a `CRITICAL` overrun alert when spending exceeds `100%` of a category budget.
  - `HighValueAlertRule`: Triggers an `INFO` alert when a single expense meets or exceeds a configured threshold amount.
- **Custom Exception Handling:** Enforces financial invariants using `InvalidTransactionException` and `InsufficientBalanceException`.
- **File Persistence (`FileStorageManager`):** Saves and reloads account details, category budgets, and transaction history via `data/ledger_state.csv` and appends rule alerts to `data/alerts.log`.
- **Financial Analytics (`LedgerService`):** Computes total income, total expenses, net balance, category-wise expenditure, and remaining budget allocations.

## Project Structure
```text
personal-finance-rule-engine/
├── .gitignore
├── README.md
├── data/
│   ├── alerts.log
│   └── ledger_state.csv
└── src/
    └── com/college/finance/
        ├── app/
        │   └── FinanceApp.java
        ├── exception/
        │   ├── InsufficientBalanceException.java
        │   └── InvalidTransactionException.java
        ├── model/
        │   ├── Account.java
        │   ├── ExpenseTransaction.java
        │   ├── IncomeTransaction.java
        │   ├── RuleAlert.java
        │   ├── Transaction.java
        │   └── TransactionType.java
        ├── persistence/
        │   └── FileStorageManager.java
        ├── rule/
        │   ├── CategoryBudgetRule.java
        │   ├── ExpenseRule.java
        │   └── HighValueAlertRule.java
        └── service/
            └── LedgerService.java
