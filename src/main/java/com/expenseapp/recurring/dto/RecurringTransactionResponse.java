package com.expenseapp.recurring.dto;

import com.expenseapp.recurring.domain.RecurringTransaction.Frequency;
import com.expenseapp.recurring.domain.RecurringTransaction.TransactionType;
import com.expenseapp.category.dto.CategoryResponse;
import com.expenseapp.user.dto.UserResponse;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for recurring transaction responses.
 */
public class RecurringTransactionResponse {

    private Long id;
    private UserResponse user;
    private CategoryResponse category;
    private String name;
    private BigDecimal amount;
    private TransactionType type;
    private String description;
    private Frequency frequency;
    private Integer dayOfMonth;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextExecutionDate;
    private Boolean isActive;
    private String createdAt;
    private String updatedAt;

    // Constructors
    public RecurringTransactionResponse() {}

    public RecurringTransactionResponse(Long id, UserResponse user, CategoryResponse category,
                                       String name, BigDecimal amount, TransactionType type,
                                       String description, Frequency frequency, Integer dayOfMonth,
                                       LocalDate startDate, LocalDate endDate, LocalDate nextExecutionDate,
                                       Boolean isActive, String createdAt, String updatedAt) {
        this.id = id;
        this.user = user;
        this.category = category;
        this.name = name;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.frequency = frequency;
        this.dayOfMonth = dayOfMonth;
        this.startDate = startDate;
        this.endDate = endDate;
        this.nextExecutionDate = nextExecutionDate;
        this.isActive = isActive;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Frequency getFrequency() {
        return frequency;
    }

    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }

    public Integer getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(Integer dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
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

    public LocalDate getNextExecutionDate() {
        return nextExecutionDate;
    }

    public void setNextExecutionDate(LocalDate nextExecutionDate) {
        this.nextExecutionDate = nextExecutionDate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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