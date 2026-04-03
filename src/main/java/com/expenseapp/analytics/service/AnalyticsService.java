package com.expenseapp.analytics.service;

import com.expenseapp.analytics.dto.AnalyticsResponse;
import com.expenseapp.analytics.dto.SpendingByCategoryResponse;
import com.expenseapp.analytics.dto.MonthlySpendingResponse;
import com.expenseapp.category.domain.Category;
import com.expenseapp.transaction.service.TransactionService;
import com.expenseapp.user.domain.User;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class for analytics and reporting operations.
 */
@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final TransactionService transactionService;

    public AnalyticsService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Get comprehensive analytics for a user.
     */
    @Cacheable(value = "analytics", key = "#user.id + '_' + #startDate + '_' + #endDate")
    public AnalyticsResponse getUserAnalytics(User user, LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(6);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        BigDecimal totalIncome = transactionService.calculateTotalIncomeByDateRange(user, startDate, endDate);
        BigDecimal totalExpenses = transactionService.calculateTotalExpensesByDateRange(user, startDate, endDate);
        BigDecimal netBalance = totalIncome.subtract(totalExpenses);

        List<SpendingByCategoryResponse> spendingByCategory = getSpendingByCategory(user, startDate, endDate);
        List<MonthlySpendingResponse> monthlySpending = getMonthlySpending(user, startDate, endDate);

        return new AnalyticsResponse(
                totalIncome,
                totalExpenses,
                netBalance,
                spendingByCategory,
                monthlySpending,
                startDate,
                endDate
        );
    }

    /**
     * Get spending breakdown by category.
     */
    @Cacheable(value = "spendingByCategory", key = "#user.id + '_' + #startDate + '_' + #endDate")
    public List<SpendingByCategoryResponse> getSpendingByCategory(User user, LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = transactionService.calculateSpendingByCategory(user);

        return results.stream()
                .map(row -> new SpendingByCategoryResponse(
                        (String) ((Category)row[0]).getName(), // category name
                        (BigDecimal) row[1] // total amount
                ))
                .collect(Collectors.toList());
    }

    /**
     * Get monthly spending trends.
     */
    public List<MonthlySpendingResponse> getMonthlySpending(User user, LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = transactionService.calculateMonthlyIncomeAndExpenses(user, startDate, endDate);
        
        return results.stream()
                .map(row -> {
                    Integer year = ((Number) row[0]).intValue();
                    Integer month = ((Number) row[1]).intValue();
                    BigDecimal income = (BigDecimal) row[2];
                    BigDecimal expenses = (BigDecimal) row[3];
                    // Create YearMonth and calculate balance
                    YearMonth yearMonth = YearMonth.of(year, month);
                    BigDecimal balance = income.subtract(expenses);
                    return new MonthlySpendingResponse(yearMonth, income, expenses, balance);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get current month summary.
     */
    @Cacheable(value = "currentMonthSummary", key = "#user.id")
    public Map<String, BigDecimal> getCurrentMonthSummary(User user) {
        YearMonth currentMonth = YearMonth.now();
        LocalDate startOfMonth = currentMonth.atDay(1);
        LocalDate endOfMonth = currentMonth.atEndOfMonth();

        BigDecimal income = transactionService.calculateTotalIncomeByDateRange(user, startOfMonth, endOfMonth);
        BigDecimal expenses = transactionService.calculateTotalExpensesByDateRange(user, startOfMonth, endOfMonth);

        return Map.of(
                "income", income,
                "expenses", expenses,
                "balance", income.subtract(expenses)
        );
    }

    /**
     * Get year-to-date summary.
     */
    @Cacheable(value = "yearToDateSummary", key = "#user.id")
    public Map<String, BigDecimal> getYearToDateSummary(User user) {
        LocalDate startOfYear = LocalDate.now().withDayOfYear(1);
        LocalDate today = LocalDate.now();

        BigDecimal income = transactionService.calculateTotalIncomeByDateRange(user, startOfYear, today);
        BigDecimal expenses = transactionService.calculateTotalExpensesByDateRange(user, startOfYear, today);

        return Map.of(
                "income", income,
                "expenses", expenses,
                "balance", income.subtract(expenses)
        );
    }
}