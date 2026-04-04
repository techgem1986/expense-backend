package com.expenseapp.account.domain;

import com.expenseapp.shared.entity.BaseEntity;
import com.expenseapp.user.domain.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/**
 * Account entity representing user financial accounts like savings, loans, investments, etc.
 */
@Entity
@Table(name = "accounts")
public class Account extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "ifsc_code")
    private String ifscCode;

    @NotNull
    @PositiveOrZero
    @Column(name = "opening_balance", precision = 15, scale = 2, nullable = false)
    private BigDecimal openingBalance = BigDecimal.ZERO;

    @NotNull
    @Column(name = "current_balance", precision = 15, scale = 2, nullable = false)
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Column(name = "description")
    private String description;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // Constructors
    public Account() {}

    public Account(User user, String name, AccountType accountType, BigDecimal openingBalance) {
        this.user = user;
        this.name = name;
        this.accountType = accountType;
        this.openingBalance = openingBalance;
        this.currentBalance = openingBalance;
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

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
    }

    public BigDecimal getOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(BigDecimal openingBalance) {
        this.openingBalance = openingBalance;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    // Helper methods
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    /**
     * Credit (add) amount to the account balance.
     */
    public void credit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.currentBalance = this.currentBalance.add(amount);
    }

    /**
     * Debit (subtract) amount from the account balance.
     */
    public void debit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.currentBalance = this.currentBalance.subtract(amount);
    }

    /**
     * Check if this account is an asset account (positive balance is good).
     */
    public boolean isAssetAccount() {
        return accountType == AccountType.SAVINGS ||
               accountType == AccountType.CHECKING ||
               accountType == AccountType.INVESTMENT ||
               accountType == AccountType.MUTUAL_FUND ||
               accountType == AccountType.CASH;
    }

    /**
     * Check if this account is a liability account (negative balance is bad).
     */
    public boolean isLiabilityAccount() {
        return accountType == AccountType.LOAN ||
               accountType == AccountType.CREDIT_CARD;
    }
}