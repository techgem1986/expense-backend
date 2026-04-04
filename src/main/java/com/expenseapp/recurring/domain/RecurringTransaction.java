package com.expenseapp.recurring.domain;

import com.expenseapp.shared.entity.BaseEntity;
import com.expenseapp.user.domain.User;
import com.expenseapp.category.domain.Category;
import com.expenseapp.account.domain.Account;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * RecurringTransaction entity for managing automated recurring transactions.
 */
@Entity
@Table(name = "recurring_transactions")
public class RecurringTransaction extends BaseEntity {

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
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Positive
    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false)
    private Frequency frequency = Frequency.MONTHLY;

    @Column(name = "day_of_month", nullable = false)
    private Integer dayOfMonth;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @NotNull
    @Column(name = "next_execution_date", nullable = false)
    private LocalDate nextExecutionDate;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id")
    private Account fromAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id")
    private Account toAccount;

    // Constructors
    public RecurringTransaction() {}

    public RecurringTransaction(User user, Category category, String name, BigDecimal amount,
                               TransactionType type, String description, Frequency frequency,
                               Integer dayOfMonth, LocalDate startDate, LocalDate endDate) {
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
        // Calculate next execution date based on start date, frequency, and day of month
        this.nextExecutionDate = calculateNextExecutionDate(startDate, frequency, dayOfMonth);
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
        this.nextExecutionDate = calculateNextExecutionDate(startDate);
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

    public Account getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(Account fromAccount) {
        this.fromAccount = fromAccount;
    }

    public Account getToAccount() {
        return toAccount;
    }

    public void setToAccount(Account toAccount) {
        this.toAccount = toAccount;
    }

    // Helper methods
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    public boolean isExpired() {
        return endDate != null && endDate.isBefore(LocalDate.now());
    }

    public boolean shouldExecute() {
        return isActive() && !isExpired() && nextExecutionDate.isBefore(LocalDate.now());
    }

    public LocalDate calculateNextExecutionDate(LocalDate startDate) {
        return calculateNextExecutionDate(startDate, this.frequency, this.dayOfMonth);
    }

    public LocalDate calculateNextExecutionDate(LocalDate startDate, Frequency frequency, Integer dayOfMonth) {
        if (startDate == null || frequency == null) {
            // If no start date, use today as fallback
            startDate = LocalDate.now();
        }
        
        LocalDate today = LocalDate.now();
        LocalDate nextDate = startDate;
        
        // If start date is in the future, use it
        if (startDate.isAfter(today)) {
            return startDate;
        }
        
        // Calculate next execution date based on frequency
        switch (frequency) {
            case MONTHLY:
                nextDate = calculateNextMonthlyDate(startDate, dayOfMonth, today);
                break;
            default:
                // Default to monthly behavior
                nextDate = calculateNextMonthlyDate(startDate, dayOfMonth, today);
        }
        
        // Ensure we always return a valid date
        if (nextDate == null) {
            nextDate = today.plusMonths(1);
        }
        
        return nextDate;
    }

    private LocalDate calculateNextMonthlyDate(LocalDate startDate, Integer dayOfMonth, LocalDate today) {
        int targetDay = dayOfMonth != null ? dayOfMonth : startDate.getDayOfMonth();
        
        // Start from current month
        LocalDate nextDate = LocalDate.of(today.getYear(), today.getMonth(), 1);
        
        // Try to set the target day in current month
        int daysInMonth = nextDate.lengthOfMonth();
        int actualDay = Math.min(targetDay, daysInMonth);
        nextDate = nextDate.withDayOfMonth(actualDay);
        
        // If this date has already passed this month, move to next month
        if (nextDate.isBefore(today)) {
            nextDate = nextDate.plusMonths(1);
            daysInMonth = nextDate.lengthOfMonth();
            actualDay = Math.min(targetDay, daysInMonth);
            nextDate = nextDate.withDayOfMonth(actualDay);
        }
        
        return nextDate;
    }

    public enum TransactionType {
        INCOME, EXPENSE
    }

    public enum Frequency {
        MONTHLY
    }
}