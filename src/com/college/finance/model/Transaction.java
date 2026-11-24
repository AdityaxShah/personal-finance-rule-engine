package com.college.finance.model;

import com.college.finance.exception.InvalidTransactionException;
import java.time.LocalDate;
import java.util.Objects;

public abstract class Transaction {
    private final String id;
    private final double amount;
    private final String category;
    private final LocalDate date;
    private final String note;

    protected Transaction(String id, double amount, String category, LocalDate date, String note)
            throws InvalidTransactionException {
        if (id == null || id.isBlank()) {
            throw new InvalidTransactionException("Transaction ID cannot be blank.");
        }
        if (amount <= 0 || Double.isNaN(amount) || Double.isInfinite(amount)) {
            throw new InvalidTransactionException("Transaction amount must be positive.");
        }
        if (category == null || category.isBlank()) {
            throw new InvalidTransactionException("Transaction category cannot be blank.");
        }
        if (date == null) {
            throw new InvalidTransactionException("Transaction date cannot be null.");
        }
        this.id = id.trim();
        this.amount = roundTwoDecimals(amount);
        this.category = category.trim();
        this.date = date;
        this.note = (note == null) ? "" : note.trim().replace(",", ";");
    }

    public abstract TransactionType getType();

    public abstract double applyToBalance(double currentBalance) throws Exception;

    protected static double roundTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public String getId() {
        return id;
    }

    public double getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getNote() {
        return note;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}