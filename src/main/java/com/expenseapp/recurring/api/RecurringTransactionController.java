package com.expenseapp.recurring.api;

import com.expenseapp.recurring.domain.RecurringTransaction;
import com.expenseapp.recurring.dto.RecurringTransactionRequest;
import com.expenseapp.recurring.dto.RecurringTransactionResponse;
import com.expenseapp.recurring.service.RecurringTransactionService;
import com.expenseapp.recurring.mapper.RecurringTransactionMapper;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for recurring transaction management operations.
 */
@RestController
@RequestMapping("/api/recurring-transactions")
@Tag(name = "Recurring Transactions", description = "Recurring transaction management APIs")
public class RecurringTransactionController {

    private final RecurringTransactionService recurringTransactionService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final AccountService accountService;
    private final RecurringTransactionMapper recurringTransactionMapper;

    public RecurringTransactionController(RecurringTransactionService recurringTransactionService,
                                        UserService userService,
                                        CategoryService categoryService,
                                        AccountService accountService,
                                        RecurringTransactionMapper recurringTransactionMapper) {
        this.recurringTransactionService = recurringTransactionService;
        this.userService = userService;
        this.categoryService = categoryService;
        this.accountService = accountService;
        this.recurringTransactionMapper = recurringTransactionMapper;
    }

    /**
     * Create a new recurring transaction.
     *
     * @param request the recurring transaction request
     * @param authentication the current authentication
     * @return RecurringTransactionResponse
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Transactional
    @Operation(summary = "Create recurring transaction", description = "Creates a new recurring transaction for the authenticated user")
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> createRecurringTransaction(
            @Valid @RequestBody RecurringTransactionRequest request,
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

        RecurringTransaction recurringTransaction = recurringTransactionMapper.toEntity(request);
        recurringTransaction.setUser(user);
        recurringTransaction.setCategory(category);
        recurringTransaction.setFromAccount(fromAccount);
        recurringTransaction.setToAccount(toAccount);

        RecurringTransaction savedRecurringTransaction = recurringTransactionService.createRecurringTransaction(recurringTransaction);
        RecurringTransactionResponse response = recurringTransactionMapper.toResponseWithAccounts(
            savedRecurringTransaction, user, category, fromAccount, toAccount);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Recurring transaction created successfully", response));
    }

    /**
     * Get all recurring transactions for the authenticated user.
     *
     * @param authentication the current authentication
     * @return Page of RecurringTransactionResponse
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    @Operation(summary = "Get user recurring transactions", description = "Retrieves paginated recurring transactions for the authenticated user")
    public ResponseEntity<ApiResponse<List<RecurringTransactionResponse>>> getUserRecurringTransactions(
            Authentication authentication) {

        String email = authentication.getName();
        User user = userService.getUserEntityByEmail(email);

        List<RecurringTransaction> recurringTransactions = recurringTransactionService.getRecurringTransactionsByUser(user);
        List<RecurringTransactionResponse> response = recurringTransactions.stream()
            .map(rt -> recurringTransactionMapper.toResponseWithAccounts(
                rt, user, rt.getCategory(), rt.getFromAccount(), rt.getToAccount()))
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Recurring transactions retrieved successfully", response));
    }

    /**
     * Get recurring transaction by ID.
     *
     * @param id the recurring transaction ID
     * @param authentication the current authentication
     * @return RecurringTransactionResponse
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    @Operation(summary = "Get recurring transaction by ID", description = "Retrieves a specific recurring transaction by ID")
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> getRecurringTransactionById(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();
        User user = userService.getUserEntityByEmail(email);

        RecurringTransaction recurringTransaction = recurringTransactionService.getRecurringTransactionById(id);

        // Security check: ensure user owns the recurring transaction
        if (!recurringTransaction.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied"));
        }

        RecurringTransactionResponse response = recurringTransactionMapper.toResponseWithAccounts(
            recurringTransaction, user, recurringTransaction.getCategory(), 
            recurringTransaction.getFromAccount(), recurringTransaction.getToAccount());

        return ResponseEntity.ok(ApiResponse.success("Recurring transaction retrieved successfully", response));
    }

    /**
     * Update an existing recurring transaction.
     *
     * @param id the recurring transaction ID
     * @param request the recurring transaction request
     * @param authentication the current authentication
     * @return RecurringTransactionResponse
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    @Operation(summary = "Update recurring transaction", description = "Updates an existing recurring transaction")
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> updateRecurringTransaction(
            @PathVariable Long id,
            @Valid @RequestBody RecurringTransactionRequest request,
            Authentication authentication) {

        String email = authentication.getName();
        User user = userService.getUserEntityByEmail(email);

        RecurringTransaction existingRecurringTransaction = recurringTransactionService.getRecurringTransactionById(id);

        // Security check: ensure user owns the recurring transaction
        if (!existingRecurringTransaction.getUser().getId().equals(user.getId())) {
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

        RecurringTransaction recurringTransactionDetails = recurringTransactionMapper.toEntity(request);
        recurringTransactionDetails.setUser(user);
        recurringTransactionDetails.setCategory(category);
        recurringTransactionDetails.setFromAccount(fromAccount);
        recurringTransactionDetails.setToAccount(toAccount);

        RecurringTransaction updatedRecurringTransaction = recurringTransactionService.updateRecurringTransaction(id, recurringTransactionDetails);
        RecurringTransactionResponse response = recurringTransactionMapper.toResponseWithAccounts(
            updatedRecurringTransaction, user, category, fromAccount, toAccount);

        return ResponseEntity.ok(ApiResponse.success("Recurring transaction updated successfully", response));
    }

    /**
     * Delete a recurring transaction.
     *
     * @param id the recurring transaction ID
     * @param authentication the current authentication
     * @return ApiResponse
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete recurring transaction", description = "Deletes a recurring transaction")
    public ResponseEntity<ApiResponse<Void>> deleteRecurringTransaction(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();
        User user = userService.getUserEntityByEmail(email);

        RecurringTransaction recurringTransaction = recurringTransactionService.getRecurringTransactionById(id);

        // Security check: ensure user owns the recurring transaction
        if (!recurringTransaction.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied"));
        }

        recurringTransactionService.deleteRecurringTransaction(id);

        return ResponseEntity.ok(ApiResponse.success("Recurring transaction deleted successfully", null));
    }
}