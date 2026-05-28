package com.expenseapp.account.domain;

/**
 * Enum representing different types of accounts.
 */
public enum AccountType {
    SAVINGS("Savings"),
    SALARY("Salary"),
    CURRENT("Current"),
    INVESTMENT("Investment"),
    MUTUAL_FUND("Mutual Funds"),
    CREDIT_CARD("Credit Cards");

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