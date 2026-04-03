package com.expenseapp.transaction.dto;

import com.expenseapp.transaction.domain.Transaction.TransactionType;
import com.expenseapp.category.dto.CategoryResponse;
import com.expenseapp.user.dto.UserResponse;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for transaction responses.
 */
public class TransactionResponse {

    private Long id;
    private UserResponse user;
    private CategoryResponse category;
    private BigDecimal amount;
    private TransactionType type;
    private String description;
    private LocalDate transactionDate;
    private Boolean isRecurringInstance;
    private Long linkedRecurringTransactionId;
    private String createdAt;
    private String updatedAt;

    // Constructors
    public TransactionResponse() {}

    public TransactionResponse(Long id, UserResponse user, CategoryResponse category, BigDecimal amount,
                              TransactionType type, String description, LocalDate transactionDate,
                              Boolean isRecurringInstance, Long linkedRecurringTransactionId,
                              String createdAt, String updatedAt) {
        this.id = id;
        this.user = user;
        this.category = category;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.transactionDate = transactionDate;
        this.isRecurringInstance = isRecurringInstance;
        this.linkedRecurringTransactionId = linkedRecurringTransactionId;
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

    public CategoryResponse getCategory() {
        return category;
    }

    public void setCategory(CategoryResponse category) {
        this.category = category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Boolean getIsRecurringInstance() {
        return isRecurringInstance;
    }

    public void setIsRecurringInstance(Boolean isRecurringInstance) {
        this.isRecurringInstance = isRecurringInstance;
    }

    public Long getLinkedRecurringTransactionId() {
        return linkedRecurringTransactionId;
    }

    public void setLinkedRecurringTransactionId(Long linkedRecurringTransactionId) {
        this.linkedRecurringTransactionId = linkedRecurringTransactionId;
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

    // Helper methods
    public boolean isIncome() {
        return TransactionType.INCOME.equals(this.type);
    }

    public boolean isExpense() {
        return TransactionType.EXPENSE.equals(this.type);
    }

    @Override
    public String toString() {
        return "TransactionResponse{" +
                "id=" + id +
                ", amount=" + amount +
                ", type=" + type +
                ", description='" + description + '\'' +
                ", transactionDate=" + transactionDate +
                ", isRecurringInstance=" + isRecurringInstance +
                '}';
    }
}