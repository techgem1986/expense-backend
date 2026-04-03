package com.expenseapp.budget.dto;

import com.expenseapp.budget.domain.Budget.BudgetPeriod;
import com.expenseapp.category.dto.CategoryResponse;
import com.expenseapp.user.dto.UserResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for budget responses.
 */
public class BudgetResponse {

    private Long id;
    private UserResponse user;
    private String name;
    private BigDecimal limitAmount;
    private BudgetPeriod period;
    private BigDecimal alertThreshold;
    private LocalDate startDate;
    private BigDecimal currentSpent;
    private BigDecimal remainingAmount;
    private Double spentPercentage;
    private Boolean isOverBudget;
    private List<CategoryResponse> categories;
    private String createdAt;
    private String updatedAt;

    // Constructors
    public BudgetResponse() {}

    public BudgetResponse(Long id, UserResponse user, String name, BigDecimal limitAmount,
                         BudgetPeriod period, BigDecimal alertThreshold, LocalDate startDate,
                         BigDecimal currentSpent, BigDecimal remainingAmount, Double spentPercentage,
                         Boolean isOverBudget, List<CategoryResponse> categories,
                         String createdAt, String updatedAt) {
        this.id = id;
        this.user = user;
        this.name = name;
        this.limitAmount = limitAmount;
        this.period = period;
        this.alertThreshold = alertThreshold;
        this.startDate = startDate;
        this.currentSpent = currentSpent;
        this.remainingAmount = remainingAmount;
        this.spentPercentage = spentPercentage;
        this.isOverBudget = isOverBudget;
        this.categories = categories;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }

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

    public BigDecimal getCurrentSpent() {
        return currentSpent;
    }

    public void setCurrentSpent(BigDecimal currentSpent) {
        this.currentSpent = currentSpent;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public Double getSpentPercentage() {
        return spentPercentage;
    }

    public void setSpentPercentage(Double spentPercentage) {
        this.spentPercentage = spentPercentage;
    }

    public Boolean getIsOverBudget() {
        return isOverBudget;
    }

    public void setIsOverBudget(Boolean isOverBudget) {
        this.isOverBudget = isOverBudget;
    }

    public List<CategoryResponse> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryResponse> categories) {
        this.categories = categories;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}