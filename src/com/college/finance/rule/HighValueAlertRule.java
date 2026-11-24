package com.college.finance.rule;

import com.college.finance.model.Account;
import com.college.finance.model.ExpenseTransaction;
import com.college.finance.model.RuleAlert;
import java.util.Optional;

public class HighValueAlertRule implements ExpenseRule {
    private final double thresholdAmount;

    public HighValueAlertRule(double thresholdAmount) {
        this.thresholdAmount = Math.max(0.01, thresholdAmount);
    }

    @Override
    public String getRuleName() {
        return "HighValueAlertRule";
    }

    @Override
    public Optional<RuleAlert> evaluate(Account account, ExpenseTransaction expense, double categorySpentAfterTx) {
        if (expense.getAmount() >= thresholdAmount) {
            return Optional.of(new RuleAlert(
                    expense.getDate(),
                    getRuleName(),
                    "INFO",
                    String.format("High-value expense of %.2f recorded in '%s' (threshold %.2f, note: %s).",
                            expense.getAmount(), expense.getCategory(), thresholdAmount, expense.getNote())
            ));
        }
        return Optional.empty();
    }
}