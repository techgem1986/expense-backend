package com.expenseapp.service;

import com.expenseapp.analytics.dto.AnalyticsResponse;
import com.expenseapp.analytics.dto.MonthlySpendingResponse;
import com.expenseapp.analytics.dto.SpendingByCategoryResponse;
import com.expenseapp.analytics.service.AnalyticsService;
import com.expenseapp.category.domain.Category;
import com.expenseapp.transaction.service.TransactionService;
import com.expenseapp.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private AnalyticsService analyticsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("test@example.com", "password", "John", "Doe");
        testUser.setId(1L);
    }

    @Test
    void shouldGetUserAnalytics() {
        when(transactionService.calculateTotalIncomeByDateRange(any(), any(), any())).thenReturn(new BigDecimal("5000.00"));
        when(transactionService.calculateTotalExpensesByDateRange(any(), any(), any())).thenReturn(new BigDecimal("3000.00"));
        List<Object[]> spendingData = List.<Object[]>of(new Object[]{new Category("Food", "Food", Category.CategoryType.EXPENSE), new BigDecimal("1000.00")});
        when(transactionService.calculateSpendingByCategory(any())).thenReturn(spendingData);

        AnalyticsResponse response = analyticsService.getUserAnalytics(testUser, LocalDate.now().minusMonths(6), LocalDate.now());

        assertNotNull(response);
        assertEquals(new BigDecimal("5000.00"), response.getTotalIncome());
        assertEquals(new BigDecimal("3000.00"), response.getTotalExpenses());
        assertEquals(new BigDecimal("2000.00"), response.getNetBalance());
        assertNotNull(response.getSpendingByCategory());
        assertNotNull(response.getMonthlySpending());
    }

    @Test
    void shouldGetUserAnalyticsWithDefaultDates() {
        when(transactionService.calculateTotalIncomeByDateRange(any(), any(), any())).thenReturn(new BigDecimal("5000.00"));
        when(transactionService.calculateTotalExpensesByDateRange(any(), any(), any())).thenReturn(new BigDecimal("3000.00"));
        when(transactionService.calculateSpendingByCategory(any())).thenReturn(List.of());

        AnalyticsResponse response = analyticsService.getUserAnalytics(testUser, null, null);

        assertNotNull(response);
        assertNotNull(response.getStartDate());
        assertNotNull(response.getEndDate());
    }

    @Test
    void shouldGetSpendingByCategory() {
        Category foodCategory = new Category("Food", "Food expenses", Category.CategoryType.EXPENSE);
        List<Object[]> spendingData = List.<Object[]>of(new Object[]{foodCategory, new BigDecimal("500.00")});
        when(transactionService.calculateSpendingByCategory(testUser)).thenReturn(spendingData);
        when(transactionService.calculateTotalExpensesByDateRange(any(), any(), any())).thenReturn(new BigDecimal("1000.00"));

        List<SpendingByCategoryResponse> spending = analyticsService.getSpendingByCategory(
                testUser, LocalDate.now().minusMonths(1), LocalDate.now());

        assertNotNull(spending);
        assertEquals(1, spending.size());
        assertEquals("Food", spending.get(0).getCategoryName());
        assertEquals(new BigDecimal("500.00"), spending.get(0).getTotalAmount());
        assertTrue(spending.get(0).getPercentage() > 0);
    }

    @Test
    void shouldGetSpendingByCategoryWithZeroExpenses() {
        Category foodCategory = new Category("Food", "Food expenses", Category.CategoryType.EXPENSE);
        List<Object[]> spendingData = List.<Object[]>of(new Object[]{foodCategory, new BigDecimal("500.00")});
        when(transactionService.calculateSpendingByCategory(testUser)).thenReturn(spendingData);
        when(transactionService.calculateTotalExpensesByDateRange(any(), any(), any())).thenReturn(BigDecimal.ZERO);

        List<SpendingByCategoryResponse> spending = analyticsService.getSpendingByCategory(
                testUser, LocalDate.now().minusMonths(1), LocalDate.now());

        assertNotNull(spending);
        assertEquals(1, spending.size());
        assertEquals(0.0, spending.get(0).getPercentage());
    }

    @Test
    void shouldGetMonthlySpending() {
        List<Object[]> monthlyData = List.<Object[]>of(new Object[]{2024, 1, new BigDecimal("5000.00"), new BigDecimal("3000.00")});
        when(transactionService.calculateMonthlyIncomeAndExpenses(any(), any(), any())).thenReturn(monthlyData);

        List<MonthlySpendingResponse> monthly = analyticsService.getMonthlySpending(
                testUser, LocalDate.now().minusMonths(6), LocalDate.now());

        assertNotNull(monthly);
        assertEquals(1, monthly.size());
        assertEquals(YearMonth.of(2024, 1), monthly.get(0).getMonth());
        assertEquals(new BigDecimal("5000.00"), monthly.get(0).getIncome());
        assertEquals(new BigDecimal("3000.00"), monthly.get(0).getExpenses());
        assertEquals(new BigDecimal("2000.00"), monthly.get(0).getBalance());
    }

    @Test
    void shouldGetMonthlySpendingEmpty() {
        when(transactionService.calculateMonthlyIncomeAndExpenses(any(), any(), any())).thenReturn(List.of());

        List<MonthlySpendingResponse> monthly = analyticsService.getMonthlySpending(
                testUser, LocalDate.now().minusMonths(6), LocalDate.now());

        assertNotNull(monthly);
        assertTrue(monthly.isEmpty());
    }

    @Test
    void shouldGetCurrentMonthSummary() {
        when(transactionService.calculateTotalIncomeByDateRange(any(), any(), any())).thenReturn(new BigDecimal("5000.00"));
        when(transactionService.calculateTotalExpensesByDateRange(any(), any(), any())).thenReturn(new BigDecimal("3000.00"));

        Map<String, BigDecimal> summary = analyticsService.getCurrentMonthSummary(testUser);

        assertNotNull(summary);
        assertEquals(new BigDecimal("5000.00"), summary.get("income"));
        assertEquals(new BigDecimal("3000.00"), summary.get("expenses"));
        assertEquals(new BigDecimal("2000.00"), summary.get("balance"));
    }

    @Test
    void shouldGetYearToDateSummary() {
        when(transactionService.calculateTotalIncomeByDateRange(any(), any(), any())).thenReturn(new BigDecimal("30000.00"));
        when(transactionService.calculateTotalExpensesByDateRange(any(), any(), any())).thenReturn(new BigDecimal("18000.00"));

        Map<String, BigDecimal> summary = analyticsService.getYearToDateSummary(testUser);

        assertNotNull(summary);
        assertEquals(new BigDecimal("30000.00"), summary.get("income"));
        assertEquals(new BigDecimal("18000.00"), summary.get("expenses"));
        assertEquals(new BigDecimal("12000.00"), summary.get("balance"));
    }

    @Test
    void shouldGetSpendingByCategoryWithMultipleCategories() {
        Category foodCategory = new Category("Food", "Food expenses", Category.CategoryType.EXPENSE);
        Category transportCategory = new Category("Transport", "Transport expenses", Category.CategoryType.EXPENSE);
        List<Object[]> spendingData = List.<Object[]>of(
                new Object[]{foodCategory, new BigDecimal("500.00")},
                new Object[]{transportCategory, new BigDecimal("300.00")}
        );
        when(transactionService.calculateSpendingByCategory(testUser)).thenReturn(spendingData);
        when(transactionService.calculateTotalExpensesByDateRange(any(), any(), any())).thenReturn(new BigDecimal("800.00"));

        List<SpendingByCategoryResponse> spending = analyticsService.getSpendingByCategory(
                testUser, LocalDate.now().minusMonths(1), LocalDate.now());

        assertNotNull(spending);
        assertEquals(2, spending.size());
        assertEquals("Food", spending.get(0).getCategoryName());
        assertEquals("Transport", spending.get(1).getCategoryName());
    }

    @Test
    void shouldGetMonthlySpendingWithMultipleMonths() {
        List<Object[]> monthlyData = List.<Object[]>of(
                new Object[]{2024, 1, new BigDecimal("5000.00"), new BigDecimal("3000.00")},
                new Object[]{2024, 2, new BigDecimal("5500.00"), new BigDecimal("3200.00")},
                new Object[]{2024, 3, new BigDecimal("4800.00"), new BigDecimal("2800.00")}
        );
        when(transactionService.calculateMonthlyIncomeAndExpenses(any(), any(), any())).thenReturn(monthlyData);

        List<MonthlySpendingResponse> monthly = analyticsService.getMonthlySpending(
                testUser, LocalDate.now().minusMonths(6), LocalDate.now());

        assertNotNull(monthly);
        assertEquals(3, monthly.size());
        assertEquals(YearMonth.of(2024, 1), monthly.get(0).getMonth());
        assertEquals(YearMonth.of(2024, 2), monthly.get(1).getMonth());
        assertEquals(YearMonth.of(2024, 3), monthly.get(2).getMonth());
    }
}