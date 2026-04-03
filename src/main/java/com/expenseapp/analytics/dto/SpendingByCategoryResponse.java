package com.expenseapp.analytics.dto;

import java.math.BigDecimal;

/**
 * DTO for spending by category response.
 */
public class SpendingByCategoryResponse {

    private String categoryName;
    private BigDecimal totalAmount;
    private Double percentage;

    // Constructors
    public SpendingByCategoryResponse() {}

    public SpendingByCategoryResponse(String categoryName, BigDecimal totalAmount) {
        this.categoryName = categoryName;
        this.totalAmount = totalAmount;
    }

    public SpendingByCategoryResponse(String categoryName, BigDecimal totalAmount, Double percentage) {
        this.categoryName = categoryName;
        this.totalAmount = totalAmount;
        this.percentage = percentage;
    }

    // Getters and setters
    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }
}