package com.expenseapp.budget.domain;

import com.expenseapp.shared.entity.BaseEntity;
import com.expenseapp.user.domain.User;
import com.expenseapp.category.domain.Category;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Budget entity for managing spending limits and tracking.
 */
@Entity
@Table(name = "budgets")
public class Budget extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Positive
    @Column(name = "limit_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal limitAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "period", nullable = false)
    private BudgetPeriod period;

    @Column(name = "alert_threshold", precision = 5, scale = 2, nullable = false)
    private BigDecimal alertThreshold = BigDecimal.valueOf(0.80);

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @OneToMany(mappedBy = "budget", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<BudgetCategory> budgetCategories = new ArrayList<>();

    // Constructors
    public Budget() {}

    public Budget(User user, String name, BigDecimal limitAmount, BudgetPeriod period, LocalDate startDate) {
        this.user = user;
        this.name = name;
        this.limitAmount = limitAmount;
        this.period = period;
        this.startDate = startDate;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
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

    public List<BudgetCategory> getBudgetCategories() {
        return budgetCategories;
    }

    public void setBudgetCategories(List<BudgetCategory> budgetCategories) {
        this.budgetCategories = budgetCategories;
    }

    // Helper methods
    public void addBudgetCategory(BudgetCategory budgetCategory) {
        budgetCategories.add(budgetCategory);
        budgetCategory.setBudget(this);
    }

    public void removeBudgetCategory(BudgetCategory budgetCategory) {
        budgetCategories.remove(budgetCategory);
        budgetCategory.setBudget(null);
    }

    public boolean isExceeded(BigDecimal currentSpending) {
        return currentSpending.compareTo(limitAmount) >= 0;
    }

    public boolean shouldTriggerAlert(BigDecimal currentSpending) {
        BigDecimal thresholdAmount = limitAmount.multiply(alertThreshold);
        return currentSpending.compareTo(thresholdAmount) >= 0;
    }

    public enum BudgetPeriod {
        MONTHLY, YEARLY
    }
}