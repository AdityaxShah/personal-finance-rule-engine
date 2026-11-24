package com.college.finance.model;

import com.college.finance.exception.InvalidTransactionException;
import java.time.LocalDate;

public class IncomeTransaction extends Transaction {
    public IncomeTransaction(String id, double amount, String category, LocalDate date, String note)
            throws InvalidTransactionException {
        super(id, amount, category, date, note);
    }

    @Override
    public TransactionType getType() {
        return TransactionType.INCOME;
    }

    @Override
    public double applyToBalance(double currentBalance) {
        return roundTwoDecimals(currentBalance + getAmount());
    }
}