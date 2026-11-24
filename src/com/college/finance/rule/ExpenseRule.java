package com.college.finance.rule;

import com.college.finance.model.Account;
import com.college.finance.model.ExpenseTransaction;
import com.college.finance.model.RuleAlert;
import java.util.Optional;

public interface ExpenseRule {
    String getRuleName();

    Optional<RuleAlert> evaluate(Account account, ExpenseTransaction expense, double categorySpentAfterTx);
}