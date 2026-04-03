package com.expenseapp.budget.domain;

import com.expenseapp.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Budget and BudgetCategory entities.
 */
class BudgetTest {

    private User testUser;
    private Budget budget;

    @BeforeEach
    void setUp() {
        testUser = new User("test@example.com", "password", "John", "Doe");
        testUser.setId(1L);

        budget = new Budget();
        budget.setId(1L);
        budget.setUser(testUser);
        budget.setName("Monthly Food Budget");
        budget.setLimitAmount(new BigDecimal("1000.00"));
        budget.setPeriod(Budget.BudgetPeriod.MONTHLY);
        budget.setAlertThreshold(new BigDecimal("0.80"));
        budget.setStartDate(LocalDate.of(2024, 1, 1));
        budget.setBudgetCategories(new ArrayList<>());
    }

    @Test
    void testBudgetCreation() {
        assertNotNull(budget);
        assertEquals(testUser, budget.getUser());
        assertEquals("Monthly Food Budget", budget.getName());
        assertEquals(new BigDecimal("1000.00"), budget.getLimitAmount());
        assertEquals(Budget.BudgetPeriod.MONTHLY, budget.getPeriod());
        assertEquals(new BigDecimal("0.80"), budget.getAlertThreshold());
        assertEquals(LocalDate.of(2024, 1, 1), budget.getStartDate());
        assertNotNull(budget.getBudgetCategories());
    }

    @Test
    void testDefaultConstructor() {
        Budget emptyBudget = new Budget();
        
        assertNull(emptyBudget.getId());
        assertNull(emptyBudget.getUser());
        assertNull(emptyBudget.getName());
        assertNull(emptyBudget.getLimitAmount());
        assertNull(emptyBudget.getPeriod());
        // alertThreshold has default value 0.80 in entity
        assertEquals(java.math.BigDecimal.valueOf(0.80), emptyBudget.getAlertThreshold());
        assertNull(emptyBudget.getStartDate());
        // budgetCategories is initialized to empty ArrayList in entity
        assertNotNull(emptyBudget.getBudgetCategories());
        assertTrue(emptyBudget.getBudgetCategories().isEmpty());
    }

    @Test
    void testSetters() {
        budget.setName("Updated Budget");
        budget.setLimitAmount(new BigDecimal("2000.00"));
        budget.setPeriod(Budget.BudgetPeriod.YEARLY);
        budget.setAlertThreshold(new BigDecimal("0.90"));
        budget.setStartDate(LocalDate.of(2024, 1, 1));

        assertEquals("Updated Budget", budget.getName());
        assertEquals(new BigDecimal("2000.00"), budget.getLimitAmount());
        assertEquals(Budget.BudgetPeriod.YEARLY, budget.getPeriod());
        assertEquals(new BigDecimal("0.90"), budget.getAlertThreshold());
        assertEquals(LocalDate.of(2024, 1, 1), budget.getStartDate());
    }

    @Test
    void testIdSetter() {
        budget.setId(999L);
        assertEquals(999L, budget.getId());
    }

    @Test
    void testBudgetPeriodEnum() {
        assertEquals(2, Budget.BudgetPeriod.values().length);
        assertEquals(Budget.BudgetPeriod.MONTHLY, Budget.BudgetPeriod.valueOf("MONTHLY"));
        assertEquals(Budget.BudgetPeriod.YEARLY, Budget.BudgetPeriod.valueOf("YEARLY"));
    }

    @Test
    void testBudgetCategoriesList() {
        assertNotNull(budget.getBudgetCategories());
        assertTrue(budget.getBudgetCategories().isEmpty());
    }

    @Test
    void testAddBudgetCategory() {
        BudgetCategory budgetCategory = new BudgetCategory(budget, null, null);
        budget.getBudgetCategories().add(budgetCategory);
        
        assertEquals(1, budget.getBudgetCategories().size());
        assertTrue(budget.getBudgetCategories().contains(budgetCategory));
    }

    // BudgetCategory tests
    @Test
    void testBudgetCategoryCreation() {
        BudgetCategory budgetCategory = new BudgetCategory(budget, null, null);
        
        assertNotNull(budgetCategory);
        assertEquals(budget, budgetCategory.getBudget());
        assertNull(budgetCategory.getCategory());
        assertNull(budgetCategory.getLimitAmount());
    }

    @Test
    void testBudgetCategorySetters() {
        BudgetCategory budgetCategory = new BudgetCategory();
        
        budgetCategory.setBudget(budget);
        budgetCategory.setCategory(null);
        budgetCategory.setLimitAmount(new BigDecimal("500.00"));

        assertEquals(budget, budgetCategory.getBudget());
        assertNull(budgetCategory.getCategory());
        assertEquals(new BigDecimal("500.00"), budgetCategory.getLimitAmount());
    }

    @Test
    void testBudgetWithNullName() {
        budget.setName(null);
        assertNull(budget.getName());
    }

    @Test
    void testBudgetWithNullLimitAmount() {
        budget.setLimitAmount(null);
        assertNull(budget.getLimitAmount());
    }

    @Test
    void testBudgetWithNullPeriod() {
        budget.setPeriod(null);
        assertNull(budget.getPeriod());
    }

    @Test
    void testBudgetWithNullAlertThreshold() {
        budget.setAlertThreshold(null);
        assertNull(budget.getAlertThreshold());
    }

    @Test
    void testBudgetWithNullStartDate() {
        budget.setStartDate(null);
        assertNull(budget.getStartDate());
    }
}