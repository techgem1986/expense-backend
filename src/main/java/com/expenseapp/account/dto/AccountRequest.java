package com.expenseapp.account.dto;

import com.expenseapp.account.domain.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/**
 * DTO for creating or updating an account.
 */
public class AccountRequest {

    @NotBlank
    private String name;

    @NotNull
    private AccountType accountType;

    private String bankName;

    private String accountNumber;

    private String ifscCode;

    @NotNull
    @PositiveOrZero
    private BigDecimal openingBalance = BigDecimal.ZERO;

    private String description;

    // Constructors
    public AccountRequest() {}

    public AccountRequest(String name, AccountType accountType, BigDecimal openingBalance) {
        this.name = name;
        this.accountType = accountType;
        this.openingBalance = openingBalance;
    }

    // Getters and setters
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}