package com.expenseapp.transaction.repository;

import com.expenseapp.transaction.domain.Transaction;
import com.expenseapp.transaction.domain.Transaction.TransactionType;
import com.expenseapp.user.domain.User;
import com.expenseapp.category.domain.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Transaction entity.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Find all transactions for a specific user.
     */
    List<Transaction> findByUserOrderByTransactionDateDesc(User user);

    /**
     * Find all transactions for a specific user with pagination.
     */
    Page<Transaction> findByUser(User user, Pageable pageable);

    /**
     * Find all transactions for a specific user with pagination, fetching category and accounts eagerly.
     */
    @Query("SELECT t FROM Transaction t LEFT JOIN FETCH t.category c LEFT JOIN FETCH t.fromAccount f LEFT JOIN FETCH t.toAccount to WHERE t.user = :user")
    Page<Transaction> findByUserWithCategoryAndAccounts(User user, Pageable pageable);

    /**
     * Find transactions by user and transaction type.
     */
    List<Transaction> findByUserAndTypeOrderByTransactionDateDesc(User user, TransactionType type);

    /**
     * Find transactions by user and category.
     */
    List<Transaction> findByUserAndCategoryOrderByTransactionDateDesc(User user, com.expenseapp.category.domain.Category category);

    /**
     * Find transactions by user within a date range.
     */
    List<Transaction> findByUserAndTransactionDateBetweenOrderByTransactionDateDesc(
        User user, LocalDate startDate, LocalDate endDate);

    /**
     * Find transactions by user, type, and date range.
     */
    List<Transaction> findByUserAndTypeAndTransactionDateBetweenOrderByTransactionDateDesc(
        User user, TransactionType type, LocalDate startDate, LocalDate endDate);

    /**
     * Find recurring transaction instances by user.
     */
    List<Transaction> findByUserAndIsRecurringInstanceTrueOrderByTransactionDateDesc(User user);

    /**
     * Find transactions by linked recurring transaction ID.
     */
    List<Transaction> findByLinkedRecurringTransactionIdOrderByTransactionDateDesc(Long recurringTransactionId);

    /**
     * Find a transaction by ID with category and accounts eagerly fetched.
     */
    @Query("SELECT t FROM Transaction t JOIN FETCH t.category c LEFT JOIN FETCH t.fromAccount f LEFT JOIN FETCH t.toAccount to WHERE t.id = :id")
    Optional<Transaction> findByIdWithCategoryAndAccounts(@Param("id") Long id);

    /**
     * Find the latest transaction for a recurring transaction.
     */
    Optional<Transaction> findTopByLinkedRecurringTransactionIdOrderByTransactionDateDesc(Long recurringTransactionId);

    /**
     * Calculate total amount for a user by transaction type.
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user = :user AND t.type = :type")
    BigDecimal calculateTotalAmountByUserAndType(@Param("user") User user, @Param("type") TransactionType type);

    /**
     * Calculate total amount for a user within a date range.
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user = :user AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalAmountByUserAndDateRange(@Param("user") User user, 
                                                     @Param("startDate") LocalDate startDate, 
                                                     @Param("endDate") LocalDate endDate);

    /**
     * Calculate total amount for a user by type and date range.
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user = :user AND t.type = :type AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalAmountByUserTypeAndDateRange(@Param("user") User user, 
                                                         @Param("type") TransactionType type,
                                                         @Param("startDate") LocalDate startDate, 
                                                         @Param("endDate") LocalDate endDate);

    /**
     * Calculate total amount by category for a user.
     */
    @Query("SELECT t.category, COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user = :user AND t.type = :type GROUP BY t.category")
    List<Object[]> calculateTotalAmountByCategory(@Param("user") User user, @Param("type") TransactionType type);

    /**
     * Calculate monthly income and expenses for a user within a date range.
     * Returns: Object[] { year, month, income, expenses }
     */
    @Query("SELECT YEAR(t.transactionDate), MONTH(t.transactionDate), " +
           "COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END), 0), " +
           "COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) " +
           "FROM Transaction t WHERE t.user = :user AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "GROUP BY YEAR(t.transactionDate), MONTH(t.transactionDate) ORDER BY YEAR(t.transactionDate), MONTH(t.transactionDate)")
    List<Object[]> calculateMonthlyIncomeAndExpenses(@Param("user") User user, 
                                                     @Param("startDate") LocalDate startDate, 
                                                     @Param("endDate") LocalDate endDate);

    /**
     * Find transactions for a specific month and year.
     */
    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND YEAR(t.transactionDate) = :year AND MONTH(t.transactionDate) = :month ORDER BY t.transactionDate DESC")
    List<Transaction> findByUserAndMonthAndYear(@Param("user") User user, 
                                               @Param("year") int year, 
                                               @Param("month") int month);

    /**
     * Find transactions by user with search criteria.
     */
    @Query("SELECT t FROM Transaction t WHERE t.user = :user " +
           "AND (:category IS NULL OR t.category = :category) " +
           "AND (:type IS NULL OR t.type = :type) " +
           "AND (:startDate IS NULL OR t.transactionDate >= :startDate) " +
           "AND (:endDate IS NULL OR t.transactionDate <= :endDate) " +
           "AND (:description IS NULL OR LOWER(t.description) LIKE LOWER(CONCAT('%', :description, '%'))) " +
           "ORDER BY t.transactionDate DESC")
    Page<Transaction> findByUserWithFilters(@Param("user") User user,
                                           @Param("category") com.expenseapp.category.domain.Category category,
                                           @Param("type") TransactionType type,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate,
                                           @Param("description") String description,
                                           Pageable pageable);

    /**
     * Check if a transaction exists for a recurring transaction on a specific date.
     */
    boolean existsByLinkedRecurringTransactionIdAndTransactionDate(Long recurringTransactionId, LocalDate transactionDate);

    /**
     * Check if a transaction exists for a recurring transaction in the same month and year.
     */
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Transaction t WHERE t.linkedRecurringTransactionId = :recurringTransactionId AND YEAR(t.transactionDate) = :year AND MONTH(t.transactionDate) = :month")
    boolean existsByLinkedRecurringTransactionIdAndMonth(@Param("recurringTransactionId") Long recurringTransactionId, 
                                                          @Param("year") int year, 
                                                          @Param("month") int month);

    /**
     * Delete all transactions for a specific user.
     */
    void deleteByUser(User user);
}