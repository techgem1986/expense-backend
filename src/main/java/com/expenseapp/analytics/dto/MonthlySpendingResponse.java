package com.expenseapp.analytics.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * DTO for monthly spending response.
 */
public class MonthlySpendingResponse {

    private YearMonth month;
    private BigDecimal income;
    private BigDecimal expenses;
    private BigDecimal balance;

    // Constructors
    public MonthlySpendingResponse() {}

    public MonthlySpendingResponse(YearMonth month, BigDecimal income, BigDecimal expenses, BigDecimal balance) {
        this.month = month;
        this.income = income;
        this.expenses = expenses;
        this.balance = balance;
    }

    // Getters and setters
    public YearMonth getMonth() {
        return month;
    }

    public void setMonth(YearMonth month) {
        this.month = month;
    }

    public BigDecimal getIncome() {
        return income;
    }

    public void setIncome(BigDecimal income) {
        this.income = income;
    }

    public BigDecimal getExpenses() {
        return expenses;
    }

    public void setExpenses(BigDecimal expenses) {
        this.expenses = expenses;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}