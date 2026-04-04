package com.expenseapp.transaction.api;

import com.expenseapp.transaction.domain.Transaction;
import com.expenseapp.transaction.dto.TransactionRequest;
import com.expenseapp.transaction.dto.TransactionResponse;
import com.expenseapp.transaction.service.TransactionService;
import com.expenseapp.transaction.mapper.TransactionMapper;
import com.expenseapp.user.service.UserService;
import com.expenseapp.user.domain.User;
import com.expenseapp.category.domain.Category;
import com.expenseapp.category.service.CategoryService;
import com.expenseapp.account.domain.Account;
import com.expenseapp.account.service.AccountService;
import com.expenseapp.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for transaction management operations.
 */
@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "Transaction management APIs")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final TransactionMapper transactionMapper;

    private final AccountService accountService;

    public TransactionController(TransactionService transactionService,
                                UserService userService,
                                CategoryService categoryService,
                                TransactionMapper transactionMapper,
                                AccountService accountService) {
        this.transactionService = transactionService;
        this.userService = userService;
        this.categoryService = categoryService;
        this.transactionMapper = transactionMapper;
        this.accountService = accountService;
    }

    /**
     * Create a new transaction.
     *
     * @param request the transaction request
     * @param authentication the current authentication
     * @return TransactionResponse
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create transaction", description = "Creates a new transaction for the authenticated user")
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(
            @Valid @RequestBody TransactionRequest request,
            Authentication authentication) {

        String email = authentication.getName();
        User user = userService.getUserEntityByEmail(email);

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryService.getCategoryEntityById(request.getCategoryId());
        }

        Account fromAccount = null;
        if (request.getFromAccountId() != null) {
            fromAccount = accountService.getAccountEntityById(request.getFromAccountId());
        }

        Account toAccount = null;
        if (request.getToAccountId() != null) {
            toAccount = accountService.getAccountEntityById(request.getToAccountId());
        }

        Transaction transaction = transactionMapper.toEntity(request);
        transaction.setUser(user);
        transaction.setCategory(category);
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);

        Transaction savedTransaction = transactionService.createTransaction(transaction);

        TransactionResponse response = transactionMapper.toResponseWithUserAndCategory(
            savedTransaction, user, category);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transaction created successfully", response));
    }

    /**
     * Get all transactions for the authenticated user.
     *
     * @param page page number (0-based)
     * @param size page size
     * @param sort sort criteria
     * @param authentication the current authentication
     * @return Page of TransactionResponse
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user transactions", description = "Retrieves paginated transactions for the authenticated user")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getUserTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "transactionDate,desc") String sort,
            Authentication authentication) {

        String email = authentication.getName();
        User user = userService.getUserEntityByEmail(email);

        Sort sortBy = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortBy);
        Page<Transaction> transactions = transactionService.getTransactionsByUser(user, pageable);

        Page<TransactionResponse> response = transactions.map(t ->
            transactionMapper.toResponseWithUserAndCategory(t, user, t.getCategory())
        );

        return ResponseEntity.ok(ApiResponse.success("Transactions retrieved successfully", response));
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        String[] parts = sort.split(",");
        if (parts.length == 2) {
            try {
                Sort.Direction direction = Sort.Direction.fromString(parts[1].trim());
                return Sort.by(direction, parts[0].trim());
            } catch (IllegalArgumentException ex) {
                // Fall back to default sort if invalid direction is provided
            }
        }

        // If invalid format, use default
        return Sort.by(Sort.Direction.DESC, "createdAt");
    }

    /**
     * Get transaction by ID.
     *
     * @param id the transaction ID
     * @param authentication the current authentication
     * @return TransactionResponse
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get transaction by ID", description = "Retrieves a specific transaction by ID")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionById(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();
        User user = userService.getUserEntityByEmail(email);

        Transaction transaction = transactionService.getTransactionById(id);

        // Security check: ensure user owns the transaction
        if (!transaction.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied"));
        }

        TransactionResponse response = transactionMapper.toResponseWithUserAndCategory(
            transaction, user, transaction.getCategory());

        return ResponseEntity.ok(ApiResponse.success("Transaction retrieved successfully", response));
    }

    /**
     * Update an existing transaction.
     *
     * @param id the transaction ID
     * @param request the transaction request
     * @param authentication the current authentication
     * @return TransactionResponse
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update transaction", description = "Updates an existing transaction")
    public ResponseEntity<ApiResponse<TransactionResponse>> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request,
            Authentication authentication) {

        String email = authentication.getName();
        User user = userService.getUserEntityByEmail(email);

        Transaction existingTransaction = transactionService.getTransactionById(id);

        // Security check: ensure user owns the transaction
        if (!existingTransaction.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied"));
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryService.getCategoryEntityById(request.getCategoryId());
        }

        Account fromAccount = null;
        if (request.getFromAccountId() != null) {
            fromAccount = accountService.getAccountEntityById(request.getFromAccountId());
        }

        Account toAccount = null;
        if (request.getToAccountId() != null) {
            toAccount = accountService.getAccountEntityById(request.getToAccountId());
        }

        Transaction transactionDetails = transactionMapper.toEntity(request);
        transactionDetails.setUser(user);
        transactionDetails.setCategory(category);
        transactionDetails.setFromAccount(fromAccount);
        transactionDetails.setToAccount(toAccount);

        Transaction updatedTransaction = transactionService.updateTransaction(id, transactionDetails);

        TransactionResponse response = transactionMapper.toResponseWithUserAndCategory(
            updatedTransaction, user, category);

        return ResponseEntity.ok(ApiResponse.success("Transaction updated successfully", response));
    }

    /**
     * Delete a transaction.
     *
     * @param id the transaction ID
     * @param authentication the current authentication
     * @return ApiResponse
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete transaction", description = "Deletes a transaction")
    public ResponseEntity<ApiResponse<Void>> deleteTransaction(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();
        User user = userService.getUserEntityByEmail(email);

        Transaction transaction = transactionService.getTransactionById(id);

        // Security check: ensure user owns the transaction
        if (!transaction.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied"));
        }

        transactionService.deleteTransaction(id);

        return ResponseEntity.ok(ApiResponse.success("Transaction deleted successfully", null));
    }
}