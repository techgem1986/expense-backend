package com.expenseapp.transaction.domain;

import com.expenseapp.shared.entity.BaseEntity;
import com.expenseapp.user.domain.User;
import com.expenseapp.category.domain.Category;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Transaction entity representing income and expense records.
 */
@Entity
@Table(name = "transactions")
public class Transaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @NotNull
    @Positive
    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;

    @Column(name = "description")
    private String description;

    @NotNull
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "is_recurring_instance")
    private Boolean isRecurringInstance;

    @Column(name = "linked_recurring_transaction_id")
    private Long linkedRecurringTransactionId;

    // Constructors
    public Transaction() {}

    public Transaction(User user, Category category, BigDecimal amount, 
                      TransactionType type, String description, LocalDate transactionDate) {
        this.user = user;
        this.category = category;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.transactionDate = transactionDate;
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
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

    // Helper methods
    public boolean isIncome() {
        return TransactionType.INCOME.equals(this.type);
    }

    public boolean isExpense() {
        return TransactionType.EXPENSE.equals(this.type);
    }

    public enum TransactionType {
        INCOME, EXPENSE
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Transaction that = (Transaction) o;

        if (id != null && that.id != null) {
            return id.equals(that.id);
        }
        
        return user.equals(that.user) &&
               category.equals(that.category) &&
               amount.equals(that.amount) &&
               type == that.type &&
               description.equals(that.description) &&
               transactionDate.equals(that.transactionDate);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        }
        return java.util.Objects.hash(user, category, amount, type, description, transactionDate);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", user=" + user +
                ", category=" + category +
                ", amount=" + amount +
                ", type=" + type +
                ", description='" + description + '\'' +
                ", transactionDate=" + transactionDate +
                ", isRecurringInstance=" + isRecurringInstance +
                ", linkedRecurringTransactionId=" + linkedRecurringTransactionId +
                '}';
    }
}
