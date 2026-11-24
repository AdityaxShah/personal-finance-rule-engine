package com.college.finance.service;

import com.college.finance.model.Account;
import com.college.finance.model.ExpenseTransaction;
import com.college.finance.model.IncomeTransaction;
import com.college.finance.model.RuleAlert;
import com.college.finance.model.Transaction;
import com.college.finance.model.TransactionType;
import com.college.finance.persistence.FileStorageManager;
import com.college.finance.rule.ExpenseRule;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LedgerService {
    private final Account account;
    private final List<ExpenseRule> rules;
    private final FileStorageManager storageManager;
    private final List<RuleAlert> sessionAlerts;

    public LedgerService(Account account, FileStorageManager storageManager) {
        this.account = account;
        this.storageManager = storageManager;
        this.rules = new ArrayList<>();
        this.sessionAlerts = new ArrayList<>();
    }

    public void registerRule(ExpenseRule rule) {
        if (rule != null) {
            rules.add(rule);
        }
    }

    public void recordIncome(IncomeTransaction income) throws Exception {
        account.addTransaction(income);
    }

    public List<RuleAlert> recordExpense(ExpenseTransaction expense) throws Exception {
        account.addTransaction(expense);
        double spentInCategory = getCategoryExpenseTotal(expense.getCategory());
        List<RuleAlert> triggered = new ArrayList<>();

        for (ExpenseRule rule : rules) {
            rule.evaluate(account, expense, spentInCategory).ifPresent(triggered::add);
        }

        if (!triggered.isEmpty()) {
            sessionAlerts.addAll(triggered);
            storageManager.appendAlerts(triggered);
        }
        return triggered;
    }

    public void saveState() throws IOException {
        storageManager.saveLedger(account);
    }

    public double getTotalIncome() {
        return account.getTransactions().stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getTotalExpense() {
        return account.getTransactions().stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getCategoryExpenseTotal(String category) {
        if (category == null) return 0.0;
        String target = category.trim();
        return account.getTransactions().stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE && t.getCategory().equalsIgnoreCase(target))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public Map<String, Double> getExpenseByCategory() {
        Map<String, Double> totals = new LinkedHashMap<>();
        for (Transaction tx : account.getTransactions()) {
            if (tx.getType() == TransactionType.EXPENSE) {
                totals.merge(tx.getCategory(), tx.getAmount(), Double::sum);
            }
        }
        return totals;
    }

    public Account getAccount() {
        return account;
    }

    public List<RuleAlert> getSessionAlerts() {
        return List.copyOf(sessionAlerts);
    }
}