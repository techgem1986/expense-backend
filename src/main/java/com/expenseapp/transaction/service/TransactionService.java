package com.expenseapp.transaction.service;

import com.expenseapp.transaction.domain.Transaction;
import com.expenseapp.transaction.domain.Transaction.TransactionType;
import com.expenseapp.transaction.repository.TransactionRepository;
import com.expenseapp.user.domain.User;
import com.expenseapp.category.domain.Category;
import com.expenseapp.shared.exception.ResourceNotFoundException;
import com.expenseapp.shared.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for transaction management operations.
 */
@Service
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Create a new transaction.
     */
    @Caching(evict = {
            @CacheEvict(value = "transactionById", allEntries = true),
            @CacheEvict(value = "transactionsByUser", allEntries = true),
            @CacheEvict(value = "transactionPages", allEntries = true),
            @CacheEvict(value = "analytics", allEntries = true),
            @CacheEvict(value = "spendingByCategory", allEntries = true),
            @CacheEvict(value = "currentMonthSummary", allEntries = true),
            @CacheEvict(value = "yearToDateSummary", allEntries = true)
    })
    public Transaction createTransaction(Transaction transaction) {
        validateTransaction(transaction);
        
        // Set default values if not provided
        if (transaction.getIsRecurringInstance() == null) {
            transaction.setIsRecurringInstance(false);
        }

        return transactionRepository.save(transaction);
    }

    /**
     * Get transaction by ID.
     */
    @Cacheable(value = "transactionById", key = "#id")
    public Transaction getTransactionById(Long id) {
        return transactionRepository.findByIdWithCategory(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
    }

    /**
     * Get all transactions for a user.
     */
    @Cacheable(value = "transactionsByUser", key = "#user.id")
    public List<Transaction> getTransactionsByUser(User user) {
        return transactionRepository.findByUserOrderByTransactionDateDesc(user);
    }

    /**
     * Get all transactions for a user with pagination.
     */
    @Transactional(readOnly = true)
    public Page<Transaction> getTransactionsByUser(User user, Pageable pageable) {
        return transactionRepository.findByUserWithCategory(user, pageable);
    }

    /**
     * Get transactions by user and type.
     */
    public List<Transaction> getTransactionsByUserAndType(User user, TransactionType type) {
        return transactionRepository.findByUserAndTypeOrderByTransactionDateDesc(user, type);
    }

    /**
     * Get transactions by user and category.
     */
    public List<Transaction> getTransactionsByUserAndCategory(User user, Category category) {
        return transactionRepository.findByUserAndCategoryOrderByTransactionDateDesc(user, category);
    }

    /**
     * Get transactions by user within a date range.
     */
    public List<Transaction> getTransactionsByUserAndDateRange(User user, LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Start date cannot be after end date");
        }
        return transactionRepository.findByUserAndTransactionDateBetweenOrderByTransactionDateDesc(user, startDate, endDate);
    }

    /**
     * Get transactions by user, type, and date range.
     */
    public List<Transaction> getTransactionsByUserTypeAndDateRange(User user, TransactionType type, 
                                                                  LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Start date cannot be after end date");
        }
        return transactionRepository.findByUserAndTypeAndTransactionDateBetweenOrderByTransactionDateDesc(
            user, type, startDate, endDate);
    }

    /**
     * Get recurring transaction instances for a user.
     */
    public List<Transaction> getRecurringTransactionInstancesByUser(User user) {
        return transactionRepository.findByUserAndIsRecurringInstanceTrueOrderByTransactionDateDesc(user);
    }

    /**
     * Get transactions by linked recurring transaction ID.
     */
    public List<Transaction> getTransactionsByRecurringTransactionId(Long recurringTransactionId) {
        return transactionRepository.findByLinkedRecurringTransactionIdOrderByTransactionDateDesc(recurringTransactionId);
    }

    /**
     * Update an existing transaction.
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "transactionById", key = "#id"),
            @CacheEvict(value = "transactionsByUser", allEntries = true),
            @CacheEvict(value = "transactionPages", allEntries = true),
            @CacheEvict(value = "analytics", allEntries = true),
            @CacheEvict(value = "spendingByCategory", allEntries = true),
            @CacheEvict(value = "currentMonthSummary", allEntries = true),
            @CacheEvict(value = "yearToDateSummary", allEntries = true)
    })
    public Transaction updateTransaction(Long id, Transaction transactionDetails) {
        Transaction transaction = getTransactionById(id);
        
        // Validate that the user owns this transaction (optional security check)
        // This could be enforced at the controller level instead
        
        transaction.setAmount(transactionDetails.getAmount());
        transaction.setType(transactionDetails.getType());
        transaction.setDescription(transactionDetails.getDescription());
        transaction.setTransactionDate(transactionDetails.getTransactionDate());
        transaction.setCategory(transactionDetails.getCategory());
        
        return transactionRepository.save(transaction);
    }

    /**
     * Delete a transaction.
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "transactionById", key = "#id"),
            @CacheEvict(value = "transactionsByUser", allEntries = true),
            @CacheEvict(value = "transactionPages", allEntries = true),
            @CacheEvict(value = "analytics", allEntries = true),
            @CacheEvict(value = "spendingByCategory", allEntries = true),
            @CacheEvict(value = "currentMonthSummary", allEntries = true),
            @CacheEvict(value = "yearToDateSummary", allEntries = true)
    })
    public void deleteTransaction(Long id) {
        Transaction transaction = getTransactionById(id);
        transactionRepository.delete(transaction);
    }

    /**
     * Calculate total income for a user.
     */
    public BigDecimal calculateTotalIncome(User user) {
        return transactionRepository.calculateTotalAmountByUserAndType(user, TransactionType.INCOME);
    }

    /**
     * Calculate total expenses for a user.
     */
    public BigDecimal calculateTotalExpenses(User user) {
        return transactionRepository.calculateTotalAmountByUserAndType(user, TransactionType.EXPENSE);
    }

    /**
     * Calculate net balance for a user (income - expenses).
     */
    public BigDecimal calculateNetBalance(User user) {
        BigDecimal income = calculateTotalIncome(user);
        BigDecimal expenses = calculateTotalExpenses(user);
        return income.subtract(expenses);
    }

    /**
     * Calculate total amount for a user within a date range.
     */
    public BigDecimal calculateTotalAmountByDateRange(User user, LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Start date cannot be after end date");
        }
        return transactionRepository.calculateTotalAmountByUserAndDateRange(user, startDate, endDate);
    }

    /**
     * Calculate total income for a user within a date range.
     */
    public BigDecimal calculateTotalIncomeByDateRange(User user, LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Start date cannot be after end date");
        }
        return transactionRepository.calculateTotalAmountByUserTypeAndDateRange(
            user, TransactionType.INCOME, startDate, endDate);
    }

    /**
     * Calculate total expenses for a user within a date range.
     */
    public BigDecimal calculateTotalExpensesByDateRange(User user, LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Start date cannot be after end date");
        }
        return transactionRepository.calculateTotalAmountByUserTypeAndDateRange(
            user, TransactionType.EXPENSE, startDate, endDate);
    }

    /**
     * Calculate spending by category for a user.
     */
    public List<Object[]> calculateSpendingByCategory(User user) {
        return transactionRepository.calculateTotalAmountByCategory(user, TransactionType.EXPENSE);
    }

    /**
     * Calculate monthly income and expenses for a user within a date range.
     */
    public List<Object[]> calculateMonthlyIncomeAndExpenses(User user, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.calculateMonthlyIncomeAndExpenses(user, startDate, endDate);
    }

    /**
     * Get transactions for a specific month and year.
     */
    public List<Transaction> getTransactionsByMonthAndYear(User user, int year, int month) {
        return transactionRepository.findByUserAndMonthAndYear(user, year, month);
    }

    /**
     * Search transactions with filters.
     */
    public Page<Transaction> searchTransactions(User user, Category category, TransactionType type,
                                               LocalDate startDate, LocalDate endDate, String description,
                                               Pageable pageable) {
        return transactionRepository.findByUserWithFilters(user, category, type, startDate, endDate, description, pageable);
    }

    /**
     * Check if a transaction exists for a recurring transaction on a specific date.
     */
    public boolean existsByRecurringTransactionIdAndDate(Long recurringTransactionId, LocalDate transactionDate) {
        return transactionRepository.existsByLinkedRecurringTransactionIdAndTransactionDate(recurringTransactionId, transactionDate);
    }

    /**
     * Validate transaction data.
     */
    private void validateTransaction(Transaction transaction) {
        if (transaction.getAmount() == null || transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Transaction amount must be greater than zero");
        }
        
        if (transaction.getType() == null) {
            throw new ValidationException("Transaction type must be specified");
        }
        
        if (transaction.getTransactionDate() == null) {
            throw new ValidationException("Transaction date must be specified");
        }
        
        if (transaction.getUser() == null) {
            throw new ValidationException("Transaction must be associated with a user");
        }
    }
}