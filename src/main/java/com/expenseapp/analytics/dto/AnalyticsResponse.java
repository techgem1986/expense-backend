package com.expenseapp.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for analytics response.
 */
public class AnalyticsResponse {

    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netBalance;
    private List<SpendingByCategoryResponse> spendingByCategory;
    private List<MonthlySpendingResponse> monthlySpending;
    private LocalDate startDate;
    private LocalDate endDate;

    // Constructors
    public AnalyticsResponse() {}

    public AnalyticsResponse(BigDecimal totalIncome, BigDecimal totalExpenses, BigDecimal netBalance,
                           List<SpendingByCategoryResponse> spendingByCategory,
                           List<MonthlySpendingResponse> monthlySpending,
                           LocalDate startDate, LocalDate endDate) {
        this.totalIncome = totalIncome;
        this.totalExpenses = totalExpenses;
        this.netBalance = netBalance;
        this.spendingByCategory = spendingByCategory;
        this.monthlySpending = monthlySpending;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and setters
    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public BigDecimal getNetBalance() {
        return netBalance;
    }

    public void setNetBalance(BigDecimal netBalance) {
        this.netBalance = netBalance;
    }

    public List<SpendingByCategoryResponse> getSpendingByCategory() {
        return spendingByCategory;
    }

    public void setSpendingByCategory(List<SpendingByCategoryResponse> spendingByCategory) {
        this.spendingByCategory = spendingByCategory;
    }

    public List<MonthlySpendingResponse> getMonthlySpending() {
        return monthlySpending;
    }

    public void setMonthlySpending(List<MonthlySpendingResponse> monthlySpending) {
        this.monthlySpending = monthlySpending;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}