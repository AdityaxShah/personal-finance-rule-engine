package com.college.finance.model;

import com.college.finance.exception.InvalidTransactionException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Account {
    private final String ownerName;
    private double balance;
    private final Map<String, Double> categoryBudgets;
    private final List<Transaction> transactions;

    public Account(String ownerName, double initialBalance) throws InvalidTransactionException {
        if (ownerName == null || ownerName.isBlank()) {
            throw new InvalidTransactionException("Account owner name cannot be blank.");
        }
        if (initialBalance < 0 || Double.isNaN(initialBalance) || Double.isInfinite(initialBalance)) {
            throw new InvalidTransactionException("Initial balance cannot be negative.");
        }
        this.ownerName = ownerName.trim();
        this.balance = Math.round(initialBalance * 100.0) / 100.0;
        this.categoryBudgets = new LinkedHashMap<>();
        this.transactions = new ArrayList<>();
    }

    public void setCategoryBudget(String category, double limit) throws InvalidTransactionException {
        if (category == null || category.isBlank()) {
            throw new InvalidTransactionException("Budget category cannot be blank.");
        }
        if (limit <= 0 || Double.isNaN(limit) || Double.isInfinite(limit)) {
            throw new InvalidTransactionException("Budget limit must be positive.");
        }
        categoryBudgets.put(category.trim(), Math.round(limit * 100.0) / 100.0);
    }

    public Double getCategoryBudget(String category) {
        if (category == null) return null;
        return categoryBudgets.get(category.trim());
    }

    public Map<String, Double> getCategoryBudgets() {
        return Collections.unmodifiableMap(categoryBudgets);
    }

    public void addTransaction(Transaction tx) throws Exception {
        double updatedBalance = tx.applyToBalance(this.balance);
        this.balance = updatedBalance;
        this.transactions.add(tx);
    }

    public String getOwnerName() {
        return ownerName;
    }

    public double getBalance() {
        return balance;
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }
}