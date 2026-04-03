package com.expenseapp.budget.event;

import com.expenseapp.budget.domain.Budget;
import com.expenseapp.user.domain.User;
import java.math.BigDecimal;

/**
 * Event published when a budget threshold is exceeded.
 */
public class BudgetThresholdExceededEvent {

    private final User user;
    private final Budget budget;
    private final BigDecimal currentSpending;
    private final BigDecimal thresholdAmount;
    private final boolean isBudgetExceeded;

    public BudgetThresholdExceededEvent(User user, Budget budget, BigDecimal currentSpending, 
                                      BigDecimal thresholdAmount, boolean isBudgetExceeded) {
        this.user = user;
        this.budget = budget;
        this.currentSpending = currentSpending;
        this.thresholdAmount = thresholdAmount;
        this.isBudgetExceeded = isBudgetExceeded;
    }

    public User getUser() {
        return user;
    }

    public Budget getBudget() {
        return budget;
    }

    public BigDecimal getCurrentSpending() {
        return currentSpending;
    }

    public BigDecimal getThresholdAmount() {
        return thresholdAmount;
    }

    public boolean isBudgetExceeded() {
        return isBudgetExceeded;
    }

    public String getAlertMessage() {
        if (isBudgetExceeded) {
            return String.format("Budget '%s' has been exceeded! Current spending: ₹%s, Limit: ₹%s", 
                budget.getName(), currentSpending, budget.getLimitAmount());
        } else {
            return String.format("Budget '%s' threshold reached! Current spending: ₹%s, Threshold: ₹%s", 
                budget.getName(), currentSpending, thresholdAmount);
        }
    }
}