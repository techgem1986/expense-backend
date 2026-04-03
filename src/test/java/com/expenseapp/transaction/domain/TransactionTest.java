package com.expenseapp.transaction.domain;

import com.expenseapp.category.domain.Category;
import com.expenseapp.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Transaction entity.
 */
class TransactionTest {

    private User testUser;
    private Category testCategory;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");

        // Create test category
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Food");
        testCategory.setType(Category.CategoryType.EXPENSE);

        // Create test transaction
        transaction = new Transaction(
            testUser,
            testCategory,
            new BigDecimal("100.50"),
            Transaction.TransactionType.EXPENSE,
            "Grocery shopping",
            LocalDate.now()
        );
        transaction.setId(1L);
    }

    @Test
    void testTransactionCreation() {
        assertNotNull(transaction);
        assertEquals(testUser, transaction.getUser());
        assertEquals(testCategory, transaction.getCategory());
        assertEquals(new BigDecimal("100.50"), transaction.getAmount());
        assertEquals(Transaction.TransactionType.EXPENSE, transaction.getType());
        assertEquals("Grocery shopping", transaction.getDescription());
        assertEquals(LocalDate.now(), transaction.getTransactionDate());
        assertNull(transaction.getIsRecurringInstance());
        assertNull(transaction.getLinkedRecurringTransactionId());
    }

    @Test
    void testTransactionSetters() {
        // Test setters
        transaction.setAmount(new BigDecimal("200.00"));
        transaction.setDescription("Updated description");
        transaction.setIsRecurringInstance(true);
        transaction.setLinkedRecurringTransactionId(123L);

        assertEquals(new BigDecimal("200.00"), transaction.getAmount());
        assertEquals("Updated description", transaction.getDescription());
        assertTrue(transaction.getIsRecurringInstance());
        assertEquals(Long.valueOf(123L), transaction.getLinkedRecurringTransactionId());
    }

    @Test
    void testIsIncomeAndIsExpense() {
        // Test expense transaction
        assertFalse(transaction.isIncome());
        assertTrue(transaction.isExpense());

        // Test income transaction
        Transaction incomeTransaction = new Transaction(
            testUser,
            testCategory,
            new BigDecimal("1000.00"),
            Transaction.TransactionType.INCOME,
            "Salary",
            LocalDate.now()
        );

        assertTrue(incomeTransaction.isIncome());
        assertFalse(incomeTransaction.isExpense());
    }

    @Test
    void testTransactionWithNullValues() {
        Transaction nullTransaction = new Transaction();
        
        assertNull(nullTransaction.getId());
        assertNull(nullTransaction.getUser());
        assertNull(nullTransaction.getCategory());
        assertNull(nullTransaction.getAmount());
        assertNull(nullTransaction.getType());
        assertNull(nullTransaction.getDescription());
        assertNull(nullTransaction.getTransactionDate());
        assertNull(nullTransaction.getIsRecurringInstance());
        assertNull(nullTransaction.getLinkedRecurringTransactionId());
    }

    @Test
    void testTransactionEquality() {
        Transaction sameTransaction = new Transaction(
            testUser,
            testCategory,
            new BigDecimal("100.50"),
            Transaction.TransactionType.EXPENSE,
            "Grocery shopping",
            LocalDate.now()
        );
        sameTransaction.setId(1L);

        assertEquals(transaction, sameTransaction);
        assertEquals(transaction.hashCode(), sameTransaction.hashCode());
    }

    @Test
    void testTransactionToString() {
        String transactionString = transaction.toString();
        assertNotNull(transactionString);
        assertTrue(transactionString.contains("Transaction"));
        assertTrue(transactionString.contains("amount=100.50"));
        assertTrue(transactionString.contains("type=EXPENSE"));
    }
}