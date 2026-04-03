package com.expenseapp.recurring.repository;

import com.expenseapp.recurring.domain.RecurringTransaction;
import com.expenseapp.recurring.domain.RecurringTransaction.Frequency;
import com.expenseapp.recurring.domain.RecurringTransaction.TransactionType;
import com.expenseapp.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for RecurringTransaction entity.
 */
@Repository
public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {

    /**
     * Find all recurring transactions for a specific user.
     */
    List<RecurringTransaction> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Find all recurring transactions for a specific user with category eagerly fetched.
     */
    @Query("SELECT rt FROM RecurringTransaction rt JOIN FETCH rt.category WHERE rt.user = :user ORDER BY rt.createdAt DESC")
    List<RecurringTransaction> findByUserWithCategoryOrderByCreatedAtDesc(User user);

    /**
     * Find all recurring transactions for a specific user with pagination.
     */
    Page<RecurringTransaction> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * Find all recurring transactions for a specific user with pagination and category eagerly fetched.
     */
    @Query("SELECT rt FROM RecurringTransaction rt JOIN FETCH rt.category WHERE rt.user = :user ORDER BY rt.createdAt DESC")
    Page<RecurringTransaction> findByUserWithCategoryOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * Find active recurring transactions for a user.
     */
    List<RecurringTransaction> findByUserAndIsActiveTrueOrderByNextExecutionDateAsc(User user);

    /**
     * Find recurring transactions by user and type.
     */
    List<RecurringTransaction> findByUserAndTypeOrderByNextExecutionDateAsc(User user, TransactionType type);

    /**
     * Find recurring transactions by user and frequency.
     */
    List<RecurringTransaction> findByUserAndFrequencyOrderByNextExecutionDateAsc(User user, Frequency frequency);

    /**
     * Find recurring transactions by user and category.
     */
    List<RecurringTransaction> findByUserAndCategoryOrderByNextExecutionDateAsc(User user, com.expenseapp.category.domain.Category category);

    /**
     * Find a recurring transaction by ID with category eagerly fetched.
     */
    @Query("SELECT rt FROM RecurringTransaction rt JOIN FETCH rt.category WHERE rt.id = :id")
    java.util.Optional<RecurringTransaction> findByIdWithCategory(@Param("id") Long id);

    /**
     * Find due recurring transactions (next execution date is today or before).
     */
    List<RecurringTransaction> findByNextExecutionDateLessThanEqualAndIsActiveTrue(LocalDate date);

    /**
     * Find due recurring transactions with end date check.
     */
    @Query("SELECT rt FROM RecurringTransaction rt WHERE rt.nextExecutionDate <= :date AND rt.isActive = true AND (rt.endDate IS NULL OR rt.endDate >= :date)")
    List<RecurringTransaction> findByNextExecutionDateLessThanEqualAndIsActiveTrueAndEndDateGreaterThanEqualOrEndDateIsNull(
        @Param("date") LocalDate date);

    /**
     * Find recurring transactions by user within a date range.
     */
    List<RecurringTransaction> findByUserAndStartDateBetweenOrderByStartDateAsc(User user, LocalDate startDate, LocalDate endDate);

    /**
     * Find recurring transactions by user and active status.
     */
    List<RecurringTransaction> findByUserAndIsActiveOrderByCreatedAtDesc(User user, Boolean isActive);

    /**
     * Find recurring transactions by user and name (case insensitive).
     */
    List<RecurringTransaction> findByUserAndNameContainingIgnoreCaseOrderByCreatedAtDesc(User user, String name);

    /**
     * Find recurring transactions by user with search criteria.
     */
    @Query("SELECT rt FROM RecurringTransaction rt WHERE rt.user = :user " +
           "AND (:type IS NULL OR rt.type = :type) " +
           "AND (:frequency IS NULL OR rt.frequency = :frequency) " +
           "AND (:category IS NULL OR rt.category = :category) " +
           "AND (:isActive IS NULL OR rt.isActive = :isActive) " +
           "AND (:startDate IS NULL OR rt.startDate >= :startDate) " +
           "AND (:endDate IS NULL OR rt.endDate <= :endDate) " +
           "ORDER BY rt.nextExecutionDate ASC")
    List<RecurringTransaction> findByUserWithFilters(@Param("user") User user,
                                                    @Param("type") TransactionType type,
                                                    @Param("frequency") Frequency frequency,
                                                    @Param("category") com.expenseapp.category.domain.Category category,
                                                    @Param("isActive") Boolean isActive,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    /**
     * Count active recurring transactions for a user.
     */
    long countByUserAndIsActiveTrue(User user);

    /**
     * Count recurring transactions by user and type.
     */
    long countByUserAndType(User user, TransactionType type);

    /**
     * Find recurring transactions by next execution date range.
     */
    List<RecurringTransaction> findByNextExecutionDateBetweenOrderByNextExecutionDateAsc(LocalDate startDate, LocalDate endDate);

    /**
     * Delete all recurring transactions for a specific user.
     */
    void deleteByUser(User user);
}