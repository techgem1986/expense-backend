package com.expenseapp.account.api;

import com.expenseapp.account.dto.AccountRequest;
import com.expenseapp.account.dto.AccountResponse;
import com.expenseapp.account.service.AccountService;
import com.expenseapp.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for managing user accounts.
 */
@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Accounts", description = "Account management APIs")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @Operation(summary = "Create a new account", description = "Create a new financial account for the current user")
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @Valid @RequestBody AccountRequest request,
            Authentication authentication) {
        AccountResponse response = accountService.createAccount(request, authentication);
        return ResponseEntity.ok(ApiResponse.success("Account created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all accounts", description = "Get all accounts for the current user with pagination")
    public ResponseEntity<ApiResponse<Page<AccountResponse>>> getAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            Authentication authentication) {
        
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") 
            ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));

        Long userId = getCurrentUserId(authentication);
        Page<AccountResponse> accounts = accountService.getAccounts(userId, pageable, authentication);
        return ResponseEntity.ok(ApiResponse.success("Accounts retrieved successfully", accounts));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active accounts", description = "Get all active accounts for the current user")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getActiveAccounts(
            Authentication authentication) {
        List<AccountResponse> accounts = accountService.getActiveAccounts(authentication);
        return ResponseEntity.ok(ApiResponse.success("Active accounts retrieved successfully", accounts));
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Get account by ID", description = "Get details of a specific account")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccount(
            @PathVariable Long accountId,
            Authentication authentication) {
        AccountResponse response = accountService.getAccount(accountId, authentication);
        return ResponseEntity.ok(ApiResponse.success("Account retrieved successfully", response));
    }

    @PutMapping("/{accountId}")
    @Operation(summary = "Update an account", description = "Update an existing account")
    public ResponseEntity<ApiResponse<AccountResponse>> updateAccount(
            @PathVariable Long accountId,
            @Valid @RequestBody AccountRequest request,
            Authentication authentication) {
        AccountResponse response = accountService.updateAccount(accountId, request, authentication);
        return ResponseEntity.ok(ApiResponse.success("Account updated successfully", response));
    }

    @DeleteMapping("/{accountId}")
    @Operation(summary = "Delete an account", description = "Soft delete an account (mark as inactive)")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @PathVariable Long accountId,
            Authentication authentication) {
        accountService.deleteAccount(accountId, authentication);
        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully", null));
    }

    @GetMapping("/total-balance")
    @Operation(summary = "Get total balance", description = "Get the total balance across all accounts")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTotalBalance(
            Authentication authentication) {
        java.math.BigDecimal totalBalance = accountService.getTotalBalance(authentication);
        Map<String, Object> data = new HashMap<>();
        data.put("totalBalance", totalBalance);
        return ResponseEntity.ok(ApiResponse.success("Total balance retrieved successfully", data));
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        org.springframework.security.oauth2.jwt.Jwt jwt = (org.springframework.security.oauth2.jwt.Jwt) authentication.getPrincipal();
        return jwt.getClaim("userId");
    }
}