package com.expenseapp.recurring.service;

import com.expenseapp.recurring.domain.RecurringTransaction;
import com.expenseapp.recurring.domain.RecurringTransaction.Frequency;
import com.expenseapp.recurring.domain.RecurringTransaction.TransactionType;
import com.expenseapp.recurring.repository.RecurringTransactionRepository;
import com.expenseapp.user.domain.User;
import com.expenseapp.shared.exception.ResourceNotFoundException;
import com.expenseapp.shared.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service layer for recurring transaction management operations.
 */
@Service
@Transactional
public class RecurringTransactionService {

    private final RecurringTransactionRepository recurringTransactionRepository;

    @Autowired
    public RecurringTransactionService(RecurringTransactionRepository recurringTransactionRepository) {
        this.recurringTransactionRepository = recurringTransactionRepository;
    }

    /**
     * Create a new recurring transaction.
     */
    @Caching(evict = {
            @CacheEvict(value = "recurringById", allEntries = true),
            @CacheEvict(value = "activeRecurringByUser", allEntries = true),
            @CacheEvict(value = "recurringByUser", allEntries = true)
    })
    public RecurringTransaction createRecurringTransaction(RecurringTransaction recurringTransaction) {
        validateRecurringTransaction(recurringTransaction);
        
        // Set default values if not provided
        if (recurringTransaction.getFrequency() == null) {
            recurringTransaction.setFrequency(Frequency.MONTHLY);
        }
        
        if (recurringTransaction.getIsActive() == null) {
            recurringTransaction.setIsActive(true);
        }

        return recurringTransactionRepository.save(recurringTransaction);
    }

    /**
     * Get recurring transaction by ID.
     */
    @Cacheable(value = "recurringById", key = "#id")
    public RecurringTransaction getRecurringTransactionById(Long id) {
        return recurringTransactionRepository.findByIdWithCategory(id)
            .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found with id: " + id));
    }

    /**
     * Get all active recurring transactions for a user.
     */
    @Cacheable(value = "activeRecurringByUser", key = "#user.id")
    public List<RecurringTransaction> getActiveRecurringTransactionsByUser(User user) {
        return recurringTransactionRepository.findByUserAndIsActiveTrueOrderByNextExecutionDateAsc(user);
    }

    /**
     * Get all recurring transactions for a user.
     */
    @Cacheable(value = "recurringByUser", key = "#user.id")
    public List<RecurringTransaction> getRecurringTransactionsByUser(User user) {
        return recurringTransactionRepository.findByUserWithCategoryOrderByCreatedAtDesc(user);
    }

    /**
     * Get due recurring transactions for a specific date.
     */
    public List<RecurringTransaction> getDueRecurringTransactions(LocalDate date) {
        return recurringTransactionRepository.findByNextExecutionDateLessThanEqualAndIsActiveTrue(date);
    }

    /**
     * Update an existing recurring transaction.
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "recurringById", key = "#id"),
            @CacheEvict(value = "activeRecurringByUser", allEntries = true),
            @CacheEvict(value = "recurringByUser", allEntries = true)
    })
    public RecurringTransaction updateRecurringTransaction(Long id, RecurringTransaction recurringTransactionDetails) {
        RecurringTransaction recurringTransaction = getRecurringTransactionById(id);
        
        // Validate that the user owns this recurring transaction (optional security check)
        // This could be enforced at the controller level instead
        
        recurringTransaction.setName(recurringTransactionDetails.getName());
        recurringTransaction.setAmount(recurringTransactionDetails.getAmount());
        recurringTransaction.setType(recurringTransactionDetails.getType());
        recurringTransaction.setDescription(recurringTransactionDetails.getDescription());
        recurringTransaction.setFrequency(recurringTransactionDetails.getFrequency());
        recurringTransaction.setDayOfMonth(recurringTransactionDetails.getDayOfMonth());
        recurringTransaction.setStartDate(recurringTransactionDetails.getStartDate());
        recurringTransaction.setEndDate(recurringTransactionDetails.getEndDate());
        
        return recurringTransactionRepository.save(recurringTransaction);
    }

    /**
     * Deactivate a recurring transaction.
     */
    @Transactional
    public void deactivateRecurringTransaction(Long id) {
        RecurringTransaction recurringTransaction = getRecurringTransactionById(id);
        recurringTransaction.setIsActive(false);
        recurringTransactionRepository.save(recurringTransaction);
    }

    /**
     * Update the next execution date for a recurring transaction.
     */
    @Transactional
    public void updateNextExecutionDate(Long id, LocalDate nextExecutionDate) {
        RecurringTransaction recurringTransaction = getRecurringTransactionById(id);
        recurringTransaction.setNextExecutionDate(nextExecutionDate);
        recurringTransactionRepository.save(recurringTransaction);
    }

    /**
     * Delete a recurring transaction.
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "recurringById", key = "#id"),
            @CacheEvict(value = "activeRecurringByUser", allEntries = true),
            @CacheEvict(value = "recurringByUser", allEntries = true)
    })
    public void deleteRecurringTransaction(Long id) {
        RecurringTransaction recurringTransaction = getRecurringTransactionById(id);
        recurringTransactionRepository.delete(recurringTransaction);
    }

    /**
     * Validate recurring transaction data.
     */
    private void validateRecurringTransaction(RecurringTransaction recurringTransaction) {
        if (recurringTransaction.getAmount() == null || recurringTransaction.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Recurring transaction amount must be greater than zero");
        }
        
        if (recurringTransaction.getType() == null) {
            throw new ValidationException("Recurring transaction type must be specified");
        }
        
        if (recurringTransaction.getStartDate() == null) {
            throw new ValidationException("Recurring transaction start date must be specified");
        }
        
        if (recurringTransaction.getDayOfMonth() == null || recurringTransaction.getDayOfMonth() < 1 || recurringTransaction.getDayOfMonth() > 31) {
            throw new ValidationException("Day of month must be between 1 and 31");
        }
        
        if (recurringTransaction.getUser() == null) {
            throw new ValidationException("Recurring transaction must be associated with a user");
        }
        
        if (recurringTransaction.getEndDate() != null && recurringTransaction.getStartDate().isAfter(recurringTransaction.getEndDate())) {
            throw new ValidationException("Start date cannot be after end date");
        }
    }
}