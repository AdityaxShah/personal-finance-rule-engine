package com.college.finance.app;

import com.college.finance.exception.InsufficientBalanceException;
import com.college.finance.exception.InvalidTransactionException;
import com.college.finance.model.Account;
import com.college.finance.model.ExpenseTransaction;
import com.college.finance.model.IncomeTransaction;
import com.college.finance.model.RuleAlert;
import com.college.finance.persistence.FileStorageManager;
import com.college.finance.rule.CategoryBudgetRule;
import com.college.finance.rule.HighValueAlertRule;
import com.college.finance.service.LedgerService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class FinanceApp {
    public static void main(String[] args) {
        FileStorageManager storage = new FileStorageManager("data");

        try {
            Account account = new Account("Aarav Mehta", 0.0);
            account.setCategoryBudget("Food", 4000.00);
            account.setCategoryBudget("Travel", 2500.00);
            account.setCategoryBudget("Books", 2000.00);

            LedgerService service = new LedgerService(account, storage);
            service.registerRule(new CategoryBudgetRule(0.80));
            service.registerRule(new HighValueAlertRule(1500.00));

            service.recordIncome(new IncomeTransaction(
                    "TX-101", 15000.00, "Allowance", LocalDate.of(2026, 11, 1), "Monthly stipend"));
            service.recordIncome(new IncomeTransaction(
                    "TX-102", 3000.00, "Freelance", LocalDate.of(2026, 11, 5), "Tutoring session"));

            recordAndPrint(service, new ExpenseTransaction(
                    "TX-103", 1200.00, "Food", LocalDate.of(2026, 11, 6), "Mess and snacks"));
            recordAndPrint(service, new ExpenseTransaction(
                    "TX-104", 2200.00, "Food", LocalDate.of(2026, 11, 10), "Group dinner"));
            recordAndPrint(service, new ExpenseTransaction(
                    "TX-105", 900.00, "Food", LocalDate.of(2026, 11, 14), "Coffee and lunch"));
            recordAndPrint(service, new ExpenseTransaction(
                    "TX-106", 1800.00, "Books", LocalDate.of(2026, 11, 16), "Algorithms and Java reference"));

            try {
                service.recordExpense(new ExpenseTransaction(
                        "TX-107", 50000.00, "Travel", LocalDate.of(2026, 11, 18), "Exceeds balance"));
            } catch (InsufficientBalanceException e) {
                System.out.println("[REJECTED EXPENSE] " + e.getMessage());
            }

            try {
                service.recordExpense(new ExpenseTransaction(
                        "TX-108", -250.00, "Food", LocalDate.of(2026, 11, 19), "Negative amount"));
            } catch (InvalidTransactionException e) {
                System.out.println("[REJECTED TRANSACTION] " + e.getMessage());
            }

            service.saveState();
            Account reloaded = storage.loadLedger();

            System.out.println("\n=== LEDGER SUMMARY (RELOADED FROM DISK) ===");
            System.out.printf("Owner         : %s%n", reloaded.getOwnerName());
            System.out.printf("Current Balance: INR %.2f%n", reloaded.getBalance());
            System.out.printf("Total Income  : INR %.2f%n", service.getTotalIncome());
            System.out.printf("Total Expense : INR %.2f%n", service.getTotalExpense());
            System.out.printf("Transactions  : %d%n", reloaded.getTransactions().size());

            System.out.println("\n=== CATEGORY BUDGET UTILIZATION ===");
            for (Map.Entry<String, Double> entry : reloaded.getCategoryBudgets().entrySet()) {
                String cat = entry.getKey();
                double budget = entry.getValue();
                double spent = service.getCategoryExpenseTotal(cat);
                double remaining = budget - spent;
                System.out.printf("  %-8s | Budget: %7.2f | Spent: %7.2f | Remaining: %8.2f%n",
                        cat, budget, spent, remaining);
            }

            System.out.println("\nPersisted CSV : " + storage.getStateFilePath().toAbsolutePath());
            System.out.println("Alert Log     : " + storage.getAlertLogFilePath().toAbsolutePath());

        } catch (Exception e) {
            System.err.println("Application error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void recordAndPrint(LedgerService service, ExpenseTransaction tx) throws Exception {
        List<RuleAlert> alerts = service.recordExpense(tx);
        System.out.printf("Recorded %s -> %-8s INR %.2f (%s)%n",
                tx.getId(), tx.getCategory(), tx.getAmount(), tx.getNote());
        for (RuleAlert alert : alerts) {
            System.out.println("   ALERT: " + alert.toLogLine());
        }
    }
}