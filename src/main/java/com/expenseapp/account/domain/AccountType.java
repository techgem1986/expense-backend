package com.expenseapp.account.domain;

/**
 * Enum representing different types of accounts.
 */
public enum AccountType {
    SAVINGS("Savings"),
    CHECKING("Checking"),
    LOAN("Loan"),
    INVESTMENT("Investment"),
    MUTUAL_FUND("Mutual Fund"),
    CREDIT_CARD("Credit Card"),
    CASH("Cash"),
    OTHER("Other");

    private final String displayName;

    AccountType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}