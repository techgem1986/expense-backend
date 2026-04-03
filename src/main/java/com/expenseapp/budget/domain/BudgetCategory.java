package com.expenseapp.budget.domain;

import com.expenseapp.shared.entity.BaseEntity;
import com.expenseapp.budget.domain.Budget;
import com.expenseapp.category.domain.Category;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * BudgetCategory entity for linking budgets to specific categories with optional limits.
 */
@Entity
@Table(name = "budget_categories")
public class BudgetCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id", nullable = false)
    private Budget budget;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "limit_amount", precision = 15, scale = 2)
    private BigDecimal limitAmount;

    // Constructors
    public BudgetCategory() {}

    public BudgetCategory(Budget budget, Category category, BigDecimal limitAmount) {
        this.budget = budget;
        this.category = category;
        this.limitAmount = limitAmount;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Budget getBudget() {
        return budget;
    }

    public void setBudget(Budget budget) {
        this.budget = budget;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public BigDecimal getLimitAmount() {
        return limitAmount;
    }

    public void setLimitAmount(BigDecimal limitAmount) {
        this.limitAmount = limitAmount;
    }

    // Helper methods
    public boolean hasCategoryLimit() {
        return limitAmount != null && limitAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isCategoryLimitExceeded(BigDecimal currentSpending) {
        return hasCategoryLimit() && currentSpending.compareTo(limitAmount) >= 0;
    }
}