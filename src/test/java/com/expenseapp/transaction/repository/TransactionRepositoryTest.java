package com.expenseapp.transaction.repository;

import com.expenseapp.account.domain.Account;
import com.expenseapp.account.domain.AccountType;
import com.expenseapp.account.repository.AccountRepository;
import com.expenseapp.category.domain.Category;
import com.expenseapp.category.repository.CategoryRepository;
import com.expenseapp.transaction.domain.Transaction;
import com.expenseapp.user.domain.User;
import com.expenseapp.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository tests for Transaction entity to verify JOIN FETCH queries
 * that prevent LazyInitializationException when accessing related entities.
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;
    private Category testCategory;
    private Account testFromAccount;
    private Account testToAccount;

    @BeforeEach
    void setUp() {
        // Create and persist test user
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("password123");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser = userRepository.save(testUser);

        // Create and persist test category
        testCategory = new Category();
        testCategory.setName("Food_" + System.currentTimeMillis());
        testCategory.setDescription("Food expenses");
        testCategory.setType(Category.CategoryType.EXPENSE);
        testCategory.setCreatedAt(LocalDateTime.now());
        testCategory = categoryRepository.save(testCategory);

        // Create and persist test accounts
        testFromAccount = new Account();
        testFromAccount.setName("Checking Account");
        testFromAccount.setAccountType(AccountType.CHECKING);
        testFromAccount.setBankName("Test Bank");
        testFromAccount.setCurrentBalance(new BigDecimal("1000.00"));
        testFromAccount.setOpeningBalance(new BigDecimal("1000.00"));
        testFromAccount.setIsActive(true);
        testFromAccount.setUser(testUser);
        testFromAccount.setCreatedAt(LocalDateTime.now());
        testFromAccount = accountRepository.save(testFromAccount);

        testToAccount = new Account();
        testToAccount.setName("Savings Account");
        testToAccount.setAccountType(AccountType.SAVINGS);
        testToAccount.setBankName("Test Bank");
        testToAccount.setCurrentBalance(new BigDecimal("5000.00"));
        testToAccount.setOpeningBalance(new BigDecimal("5000.00"));
        testToAccount.setIsActive(true);
        testToAccount.setUser(testUser);
        testToAccount.setCreatedAt(LocalDateTime.now());
        testToAccount = accountRepository.save(testToAccount);
    }

    @Test
    void findByIdWithCategoryAndAccounts_ShouldFetchAllRelatedEntities() {
        // Given - Create a transaction with all relationships
        Transaction transaction = new Transaction();
        transaction.setUser(testUser);
        transaction.setCategory(testCategory);
        transaction.setFromAccount(testFromAccount);
        transaction.setToAccount(testToAccount);
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setType(Transaction.TransactionType.EXPENSE);
        transaction.setDescription("Test expense");
        transaction.setTransactionDate(LocalDate.now());
        transaction = transactionRepository.save(transaction);

        entityManager.flush();
        entityManager.clear(); // Clear persistence context to simulate session close

        // When - Fetch using the JOIN FETCH query
        Optional<Transaction> fetchedTransaction = transactionRepository.findByIdWithCategoryAndAccounts(transaction.getId());

        // Then - Verify all entities are fetched without LazyInitializationException
        assertTrue(fetchedTransaction.isPresent());
        Transaction result = fetchedTransaction.get();
        
        // Access all lazy-loaded entities - should not throw LazyInitializationException
        assertNotNull(result.getCategory());
        assertEquals(testCategory.getId(), result.getCategory().getId());
        assertEquals(testCategory.getName(), result.getCategory().getName());
        
        assertNotNull(result.getFromAccount());
        assertEquals(testFromAccount.getId(), result.getFromAccount().getId());
        assertEquals(testFromAccount.getName(), result.getFromAccount().getName());
        
        assertNotNull(result.getToAccount());
        assertEquals(testToAccount.getId(), result.getToAccount().getId());
        assertEquals(testToAccount.getName(), result.getToAccount().getName());
    }

    @Test
    void findByIdWithCategoryAndAccounts_ShouldHandleNullFromAccount() {
        // Given - Create an income transaction (only toAccount)
        Transaction transaction = new Transaction();
        transaction.setUser(testUser);
        transaction.setCategory(testCategory);
        transaction.setFromAccount(null);
        transaction.setToAccount(testToAccount);
        transaction.setAmount(new BigDecimal("500.00"));
        transaction.setType(Transaction.TransactionType.INCOME);
        transaction.setDescription("Income transaction");
        transaction.setTransactionDate(LocalDate.now());
        transaction = transactionRepository.save(transaction);

        entityManager.flush();
        entityManager.clear();

        // When
        Optional<Transaction> fetchedTransaction = transactionRepository.findByIdWithCategoryAndAccounts(transaction.getId());

        // Then
        assertTrue(fetchedTransaction.isPresent());
        Transaction result = fetchedTransaction.get();
        
        assertNotNull(result.getCategory());
        assertNull(result.getFromAccount());
        assertNotNull(result.getToAccount());
        assertEquals(testToAccount.getName(), result.getToAccount().getName());
    }

    @Test
    void findByIdWithCategoryAndAccounts_ShouldHandleNullToAccount() {
        // Given - Create an expense transaction (only fromAccount)
        Transaction transaction = new Transaction();
        transaction.setUser(testUser);
        transaction.setCategory(testCategory);
        transaction.setFromAccount(testFromAccount);
        transaction.setToAccount(null);
        transaction.setAmount(new BigDecimal("50.00"));
        transaction.setType(Transaction.TransactionType.EXPENSE);
        transaction.setDescription("Expense transaction");
        transaction.setTransactionDate(LocalDate.now());
        transaction = transactionRepository.save(transaction);

        entityManager.flush();
        entityManager.clear();

        // When
        Optional<Transaction> fetchedTransaction = transactionRepository.findByIdWithCategoryAndAccounts(transaction.getId());

        // Then
        assertTrue(fetchedTransaction.isPresent());
        Transaction result = fetchedTransaction.get();
        
        assertNotNull(result.getCategory());
        assertNotNull(result.getFromAccount());
        assertEquals(testFromAccount.getName(), result.getFromAccount().getName());
        assertNull(result.getToAccount());
    }

    @Test
    void findByUserWithCategoryAndAccounts_ShouldFetchAllTransactionsWithRelatedEntities() {
        // Given - Create multiple transactions
        Transaction transaction1 = new Transaction();
        transaction1.setUser(testUser);
        transaction1.setCategory(testCategory);
        transaction1.setFromAccount(testFromAccount);
        transaction1.setToAccount(testToAccount);
        transaction1.setAmount(new BigDecimal("100.00"));
        transaction1.setType(Transaction.TransactionType.EXPENSE);
        transaction1.setDescription("Transaction 1");
        transaction1.setTransactionDate(LocalDate.now());

        Transaction transaction2 = new Transaction();
        transaction2.setUser(testUser);
        transaction2.setCategory(testCategory);
        transaction2.setFromAccount(testFromAccount);
        transaction2.setToAccount(null);
        transaction2.setAmount(new BigDecimal("200.00"));
        transaction2.setType(Transaction.TransactionType.EXPENSE);
        transaction2.setDescription("Transaction 2");
        transaction2.setTransactionDate(LocalDate.now().minusDays(1));

        transactionRepository.saveAll(List.of(transaction1, transaction2));
        entityManager.flush();
        entityManager.clear();

        // When
        Page<Transaction> transactions = transactionRepository.findByUserWithCategoryAndAccounts(
            testUser, PageRequest.of(0, 10));

        // Then
        assertEquals(2, transactions.getTotalElements());
        
        for (Transaction result : transactions.getContent()) {
            // Access all lazy-loaded entities - should not throw LazyInitializationException
            assertNotNull(result.getCategory());
            assertEquals(testCategory.getName(), result.getCategory().getName());
            
            if (result.getFromAccount() != null) {
                assertEquals(testFromAccount.getName(), result.getFromAccount().getName());
            }
            if (result.getToAccount() != null) {
                assertEquals(testToAccount.getName(), result.getToAccount().getName());
            }
        }
    }

    @Test
    void findByUserWithCategoryAndAccounts_ShouldSupportPagination() {
        // Given - Create 5 transactions
        for (int i = 0; i < 5; i++) {
            Transaction transaction = new Transaction();
            transaction.setUser(testUser);
            transaction.setCategory(testCategory);
            transaction.setFromAccount(testFromAccount);
            transaction.setAmount(new BigDecimal("100.00"));
            transaction.setType(Transaction.TransactionType.EXPENSE);
            transaction.setDescription("Transaction " + i);
            transaction.setTransactionDate(LocalDate.now().minusDays(i));
            transactionRepository.save(transaction);
        }
        entityManager.flush();
        entityManager.clear();

        // When - Fetch first page with size 2
        Page<Transaction> page1 = transactionRepository.findByUserWithCategoryAndAccounts(
            testUser, PageRequest.of(0, 2));

        // Then
        assertEquals(2, page1.getContent().size());
        assertEquals(5, page1.getTotalElements());
        
        // Verify entities are initialized
        for (Transaction t : page1.getContent()) {
            assertNotNull(t.getCategory().getName());
            assertNotNull(t.getFromAccount().getName());
        }

        // When - Fetch second page
        Page<Transaction> page2 = transactionRepository.findByUserWithCategoryAndAccounts(
            testUser, PageRequest.of(1, 2));

        // Then
        assertEquals(2, page2.getContent().size());
        assertEquals(5, page2.getTotalElements());
    }

    @Test
    void findByIdWithCategoryAndAccounts_ShouldReturnEmptyForNonExistentId() {
        // When
        Optional<Transaction> result = transactionRepository.findByIdWithCategoryAndAccounts(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findByUserWithCategoryAndAccounts_ShouldReturnEmptyForUserWithNoTransactions() {
        // Given - Create a new user with no transactions
        User newUser = new User();
        newUser.setEmail("newuser@example.com");
        newUser.setPasswordHash("password123");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setCreatedAt(LocalDateTime.now());
        newUser = userRepository.save(newUser);

        // When
        Page<Transaction> transactions = transactionRepository.findByUserWithCategoryAndAccounts(
            newUser, PageRequest.of(0, 10));

        // Then
        assertEquals(0, transactions.getTotalElements());
    }
}
