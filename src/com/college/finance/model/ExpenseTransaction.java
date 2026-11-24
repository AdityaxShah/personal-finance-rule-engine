package com.college.finance.model;

import com.college.finance.exception.InsufficientBalanceException;
import com.college.finance.exception.InvalidTransactionException;
import java.time.LocalDate;

public class ExpenseTransaction extends Transaction {
    public ExpenseTransaction(String id, double amount, String category, LocalDate date, String note)
            throws InvalidTransactionException {
        super(id, amount, category, date, note);
    }

    @Override
    public TransactionType getType() {
        return TransactionType.EXPENSE;
    }

    @Override
    public double applyToBalance(double currentBalance) throws InsufficientBalanceException {
        if (getAmount() > currentBalance) {
            throw new InsufficientBalanceException(
                    String.format("Insufficient balance (%.2f) for expense of %.2f in category '%s'.",
                            currentBalance, getAmount(), getCategory())
            );
        }
        return roundTwoDecimals(currentBalance - getAmount());
    }
}