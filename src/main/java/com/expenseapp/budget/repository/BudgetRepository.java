package com.expenseapp.budget.repository;

import com.expenseapp.budget.domain.Budget;
import com.expenseapp.budget.domain.Budget.BudgetPeriod;
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
 * Repository for Budget entity operations.
 */
@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    /**
     * Find all budgets for a user.
     */
    List<Budget> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Find budgets for a user with pagination.
     */
    Page<Budget> findByUser(User user, Pageable pageable);

    /**
     * Find budget by user and name.
     */
    Budget findByUserAndName(User user, String name);

    /**
     * Find budgets by user and period.
     */
    List<Budget> findByUserAndPeriod(User user, BudgetPeriod period);

    /**
     * Find active budgets for a user (current period).
     */
    @Query("SELECT b FROM Budget b WHERE b.user = :user AND b.startDate <= :currentDate")
    List<Budget> findActiveBudgetsByUser(@Param("user") User user, @Param("currentDate") LocalDate currentDate);

    /**
     * Check if budget name exists for user.
     */
    boolean existsByUserAndName(User user, String name);

    /**
     * Find budgets that are close to or over their limit.
     */
    @Query("SELECT b FROM Budget b WHERE b.user = :user AND b.alertThreshold IS NOT NULL")
    List<Budget> findBudgetsWithAlerts(@Param("user") User user);
}