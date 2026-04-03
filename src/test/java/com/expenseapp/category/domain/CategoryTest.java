package com.expenseapp.category.domain;

import com.expenseapp.transaction.domain.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Category entity.
 */
class CategoryTest {

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category("Food", "Food and dining expenses", Category.CategoryType.EXPENSE);
        category.setId(1L);
    }

    @Test
    void testCategoryCreationWithConstructor() {
        assertNotNull(category);
        assertEquals("Food", category.getName());
        assertEquals("Food and dining expenses", category.getDescription());
        assertEquals(Category.CategoryType.EXPENSE, category.getType());
    }

    @Test
    void testDefaultConstructor() {
        Category emptyCategory = new Category();
        
        assertNull(emptyCategory.getId());
        assertNull(emptyCategory.getName());
        assertNull(emptyCategory.getDescription());
        assertNull(emptyCategory.getType());
        // transactions list is initialized to empty ArrayList in entity
        assertNotNull(emptyCategory.getTransactions());
        assertTrue(emptyCategory.getTransactions().isEmpty());
    }

    @Test
    void testSetters() {
        category.setName("Transport");
        category.setDescription("Transportation costs");
        category.setType(Category.CategoryType.INCOME);

        assertEquals("Transport", category.getName());
        assertEquals("Transportation costs", category.getDescription());
        assertEquals(Category.CategoryType.INCOME, category.getType());
    }

    @Test
    void testIsIncomeCategory() {
        category.setType(Category.CategoryType.INCOME);
        assertTrue(category.isIncomeCategory());
        assertFalse(category.isExpenseCategory());
    }

    @Test
    void testIsExpenseCategory() {
        category.setType(Category.CategoryType.EXPENSE);
        assertFalse(category.isIncomeCategory());
        assertTrue(category.isExpenseCategory());
    }

    @Test
    void testTransactionsList() {
        assertNotNull(category.getTransactions());
        assertTrue(category.getTransactions().isEmpty());
    }

    @Test
    void testSetTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        category.setTransactions(transactions);
        
        assertEquals(transactions, category.getTransactions());
    }

    @Test
    void testIdSetter() {
        category.setId(999L);
        assertEquals(999L, category.getId());
    }

    @Test
    void testCategoryWithNullName() {
        category.setName(null);
        assertNull(category.getName());
    }

    @Test
    void testCategoryWithNullDescription() {
        category.setDescription(null);
        assertNull(category.getDescription());
    }

    @Test
    void testCategoryWithNullType() {
        category.setType(null);
        assertNull(category.getType());
    }

    @Test
    void testCategoryTypeEnumValues() {
        assertEquals(2, Category.CategoryType.values().length);
        assertEquals(Category.CategoryType.INCOME, Category.CategoryType.valueOf("INCOME"));
        assertEquals(Category.CategoryType.EXPENSE, Category.CategoryType.valueOf("EXPENSE"));
    }
}