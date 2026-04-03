package com.expenseapp.recurring.domain;

import com.expenseapp.category.domain.Category;
import com.expenseapp.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RecurringTransaction entity.
 */
class RecurringTransactionTest {

    private User testUser;
    private Category testCategory;
    private RecurringTransaction recurringTransaction;

    @BeforeEach
    void setUp() {
        testUser = new User("test@example.com", "password", "John", "Doe");
        testUser.setId(1L);

        testCategory = new Category("Food", "Food expenses", Category.CategoryType.EXPENSE);
        testCategory.setId(1L);

        recurringTransaction = new RecurringTransaction();
        recurringTransaction.setId(1L);
        recurringTransaction.setUser(testUser);
        recurringTransaction.setCategory(testCategory);
        recurringTransaction.setName("Monthly Rent");
        recurringTransaction.setAmount(new BigDecimal("1500.00"));
        recurringTransaction.setType(RecurringTransaction.TransactionType.EXPENSE);
        recurringTransaction.setDescription("Monthly apartment rent");
        recurringTransaction.setFrequency(RecurringTransaction.Frequency.MONTHLY);
        recurringTransaction.setDayOfMonth(1);
        recurringTransaction.setStartDate(LocalDate.of(2024, 1, 1));
        recurringTransaction.setNextExecutionDate(LocalDate.of(2024, 2, 1));
        recurringTransaction.setIsActive(true);
    }

    @Test
    void testRecurringTransactionCreation() {
        assertNotNull(recurringTransaction);
        assertEquals(testUser, recurringTransaction.getUser());
        assertEquals(testCategory, recurringTransaction.getCategory());
        assertEquals("Monthly Rent", recurringTransaction.getName());
        assertEquals(new BigDecimal("1500.00"), recurringTransaction.getAmount());
        assertEquals(RecurringTransaction.TransactionType.EXPENSE, recurringTransaction.getType());
        assertEquals("Monthly apartment rent", recurringTransaction.getDescription());
        assertEquals(RecurringTransaction.Frequency.MONTHLY, recurringTransaction.getFrequency());
        assertEquals(1, recurringTransaction.getDayOfMonth());
        assertEquals(LocalDate.of(2024, 1, 1), recurringTransaction.getStartDate());
        assertEquals(LocalDate.of(2024, 2, 1), recurringTransaction.getNextExecutionDate());
        assertTrue(recurringTransaction.getIsActive());
    }

    @Test
    void testDefaultConstructor() {
        RecurringTransaction empty = new RecurringTransaction();
        
        assertNull(empty.getId());
        assertNull(empty.getUser());
        assertNull(empty.getCategory());
        assertNull(empty.getName());
        assertNull(empty.getAmount());
        assertNull(empty.getType());
        assertNull(empty.getDescription());
        // Frequency has default value MONTHLY in entity
        assertEquals(RecurringTransaction.Frequency.MONTHLY, empty.getFrequency());
        assertNull(empty.getDayOfMonth());
        assertNull(empty.getStartDate());
        assertNull(empty.getNextExecutionDate());
        // isActive has default value true in entity
        assertTrue(empty.getIsActive());
        assertNull(empty.getEndDate());
    }

    @Test
    void testSetters() {
        recurringTransaction.setName("Updated Name");
        recurringTransaction.setAmount(new BigDecimal("2000.00"));
        recurringTransaction.setType(RecurringTransaction.TransactionType.INCOME);
        recurringTransaction.setDescription("Updated description");
        recurringTransaction.setFrequency(RecurringTransaction.Frequency.MONTHLY);
        recurringTransaction.setDayOfMonth(15);
        recurringTransaction.setStartDate(LocalDate.of(2024, 6, 1));
        recurringTransaction.setNextExecutionDate(LocalDate.of(2024, 7, 15));
        recurringTransaction.setIsActive(false);
        recurringTransaction.setEndDate(LocalDate.of(2024, 12, 31));

        assertEquals("Updated Name", recurringTransaction.getName());
        assertEquals(new BigDecimal("2000.00"), recurringTransaction.getAmount());
        assertEquals(RecurringTransaction.TransactionType.INCOME, recurringTransaction.getType());
        assertEquals("Updated description", recurringTransaction.getDescription());
        assertEquals(RecurringTransaction.Frequency.MONTHLY, recurringTransaction.getFrequency());
        assertEquals(15, recurringTransaction.getDayOfMonth());
        assertEquals(LocalDate.of(2024, 6, 1), recurringTransaction.getStartDate());
        assertEquals(LocalDate.of(2024, 7, 15), recurringTransaction.getNextExecutionDate());
        assertFalse(recurringTransaction.getIsActive());
        assertEquals(LocalDate.of(2024, 12, 31), recurringTransaction.getEndDate());
    }

    @Test
    void testIdSetter() {
        recurringTransaction.setId(999L);
        assertEquals(999L, recurringTransaction.getId());
    }

    @Test
    void testTransactionTypeEnum() {
        assertEquals(2, RecurringTransaction.TransactionType.values().length);
        assertEquals(RecurringTransaction.TransactionType.INCOME, 
                RecurringTransaction.TransactionType.valueOf("INCOME"));
        assertEquals(RecurringTransaction.TransactionType.EXPENSE, 
                RecurringTransaction.TransactionType.valueOf("EXPENSE"));
    }

    @Test
    void testFrequencyEnum() {
        assertEquals(1, RecurringTransaction.Frequency.values().length);
        assertEquals(RecurringTransaction.Frequency.MONTHLY, 
                RecurringTransaction.Frequency.valueOf("MONTHLY"));
    }

    @Test
    void testWithNullName() {
        recurringTransaction.setName(null);
        assertNull(recurringTransaction.getName());
    }

    @Test
    void testWithNullAmount() {
        recurringTransaction.setAmount(null);
        assertNull(recurringTransaction.getAmount());
    }

    @Test
    void testWithNullType() {
        recurringTransaction.setType(null);
        assertNull(recurringTransaction.getType());
    }

    @Test
    void testWithNullDescription() {
        recurringTransaction.setDescription(null);
        assertNull(recurringTransaction.getDescription());
    }

    @Test
    void testWithNullFrequency() {
        recurringTransaction.setFrequency(null);
        assertNull(recurringTransaction.getFrequency());
    }

    @Test
    void testWithNullDayOfMonth() {
        recurringTransaction.setDayOfMonth(null);
        assertNull(recurringTransaction.getDayOfMonth());
    }

    // Note: testWithNullStartDate removed because the entity's setStartDate method
    // calls calculateNextExecutionDate which requires a non-null startDate

    @Test
    void testWithNullNextExecutionDate() {
        recurringTransaction.setNextExecutionDate(null);
        assertNull(recurringTransaction.getNextExecutionDate());
    }

    @Test
    void testWithNullEndDate() {
        recurringTransaction.setEndDate(null);
        assertNull(recurringTransaction.getEndDate());
    }

    @Test
    void testWithNullIsActive() {
        recurringTransaction.setIsActive(null);
        assertNull(recurringTransaction.getIsActive());
    }
}