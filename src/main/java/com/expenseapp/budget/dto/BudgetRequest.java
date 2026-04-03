package com.expenseapp.budget.dto;

import com.expenseapp.budget.domain.Budget.BudgetPeriod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for creating and updating budgets.
 */
public class BudgetRequest {

    @NotNull(message = "Name is required")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    private String name;

    @NotNull(message = "Limit amount is required")
    @Positive(message = "Limit amount must be greater than zero")
    private BigDecimal limitAmount;

    @NotNull(message = "Period is required")
    private BudgetPeriod period;

    private BigDecimal alertThreshold;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private List<Long> categoryIds;

    // Constructors
    public BudgetRequest() {}

    public BudgetRequest(String name, BigDecimal limitAmount, BudgetPeriod period,
                        BigDecimal alertThreshold, LocalDate startDate, List<Long> categoryIds) {
        this.name = name;
        this.limitAmount = limitAmount;
        this.period = period;
        this.alertThreshold = alertThreshold;
        this.startDate = startDate;
        this.categoryIds = categoryIds;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getLimitAmount() {
        return limitAmount;
    }

    public void setLimitAmount(BigDecimal limitAmount) {
        this.limitAmount = limitAmount;
    }

    public BudgetPeriod getPeriod() {
        return period;
    }

    public void setPeriod(BudgetPeriod period) {
        this.period = period;
    }

    public BigDecimal getAlertThreshold() {
        return alertThreshold;
    }

    public void setAlertThreshold(BigDecimal alertThreshold) {
        this.alertThreshold = alertThreshold;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public List<Long> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(List<Long> categoryIds) {
        this.categoryIds = categoryIds;
    }
}