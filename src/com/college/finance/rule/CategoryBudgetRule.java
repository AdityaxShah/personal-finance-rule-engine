package com.college.finance.rule;

import com.college.finance.model.Account;
import com.college.finance.model.ExpenseTransaction;
import com.college.finance.model.RuleAlert;
import java.util.Optional;

public class CategoryBudgetRule implements ExpenseRule {
    private final double warningRatio;

    public CategoryBudgetRule(double warningRatio) {
        this.warningRatio = (warningRatio > 0 && warningRatio <= 1.0) ? warningRatio : 0.80;
    }

    @Override
    public String getRuleName() {
        return "CategoryBudgetRule";
    }

    @Override
    public Optional<RuleAlert> evaluate(Account account, ExpenseTransaction expense, double categorySpentAfterTx) {
        Double budget = account.getCategoryBudget(expense.getCategory());
        if (budget == null || budget <= 0) {
            return Optional.empty();
        }
        double ratio = categorySpentAfterTx / budget;
        if (ratio > 1.0) {
            double overrun = categorySpentAfterTx - budget;
            return Optional.of(new RuleAlert(
                    expense.getDate(),
                    getRuleName(),
                    "CRITICAL",
                    String.format("Category '%s' exceeded budget %.2f (spent %.2f, overrun %.2f).",
                            expense.getCategory(), budget, categorySpentAfterTx, overrun)
            ));
        }
        if (ratio >= warningRatio) {
            return Optional.of(new RuleAlert(
                    expense.getDate(),
                    getRuleName(),
                    "WARNING",
                    String.format("Category '%s' reached %.1f%% of budget %.2f (spent %.2f).",
                            expense.getCategory(), ratio * 100.0, budget, categorySpentAfterTx)
            ));
        }
        return Optional.empty();
    }
}