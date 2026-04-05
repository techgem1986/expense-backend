package com.expenseapp.account.repository;

import com.expenseapp.account.domain.Account;
import com.expenseapp.account.domain.AccountType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository for Account entities.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Find all accounts for a specific user.
     */
    Page<Account> findByUserId(Long userId, Pageable pageable);

    /**
     * Find all active accounts for a specific user.
     */
    Page<Account> findByUserIdAndIsActiveTrue(Long userId, Pageable pageable);

    /**
     * Find all accounts for a user by account type.
     */
    List<Account> findByUserIdAndAccountType(Long userId, AccountType accountType);

    /**
     * Find all active accounts for a user.
     */
    List<Account> findByUserIdAndIsActiveTrue(Long userId);

    /**
     * Count accounts for a user.
     */
    long countByUserId(Long userId);

    /**
     * Find accounts by bank name containing the given string.
     */
    Page<Account> findByUserIdAndBankNameContainingIgnoreCase(Long userId, String bankName, Pageable pageable);

    /**
     * Find active accounts by bank name containing the given string.
     */
    Page<Account> findByUserIdAndBankNameContainingIgnoreCaseAndIsActiveTrue(Long userId, String bankName, Pageable pageable);

    /**
     * Calculate total balance for all accounts of a user.
     */
    @Query("SELECT COALESCE(SUM(a.currentBalance), 0) FROM Account a WHERE a.user.id = :userId AND a.isActive = true")
    BigDecimal getTotalBalanceByUserId(@Param("userId") Long userId);

    /**
     * Calculate total balance for accounts of a specific type for a user.
     */
    @Query("SELECT COALESCE(SUM(a.currentBalance), 0) FROM Account a WHERE a.user.id = :userId AND a.accountType = :accountType AND a.isActive = true")
    BigDecimal getTotalBalanceByUserIdAndAccountType(@Param("userId") Long userId, @Param("accountType") AccountType accountType);

    /**
     * Check if an account number already exists for a user.
     */
    boolean existsByUserIdAndAccountNumber(Long userId, String accountNumber);

    /**
     * Find account by user and account number.
     */
    Account findByUserIdAndAccountNumber(Long userId, String accountNumber);
}