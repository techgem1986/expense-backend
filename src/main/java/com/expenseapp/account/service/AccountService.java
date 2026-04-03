package com.expenseapp.account.service;

import com.expenseapp.account.domain.Account;
import com.expenseapp.account.domain.AccountType;
import com.expenseapp.account.dto.AccountRequest;
import com.expenseapp.account.dto.AccountResponse;
import com.expenseapp.account.mapper.AccountMapper;
import com.expenseapp.account.repository.AccountRepository;
import com.expenseapp.shared.exception.ResourceNotFoundException;
import com.expenseapp.shared.exception.ValidationException;
import com.expenseapp.user.domain.User;
import com.expenseapp.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing user accounts.
 */
@Service
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountMapper accountMapper;

    public AccountService(AccountRepository accountRepository, 
                         UserRepository userRepository, 
                         AccountMapper accountMapper) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.accountMapper = accountMapper;
    }

    /**
     * Get the current user ID from authentication.
     */
    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ValidationException("User not authenticated");
        }
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getClaim("userId");
    }

    /**
     * Create a new account for the current user.
     */
    public AccountResponse createAccount(AccountRequest request, Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Account account = accountMapper.toEntity(request);
        account.setUser(user);
        account.setCurrentBalance(request.getOpeningBalance());

        Account savedAccount = accountRepository.save(account);
        return accountMapper.toResponse(savedAccount);
    }

    /**
     * Get all accounts for the current user with pagination.
     */
    @Transactional(readOnly = true)
    public Page<AccountResponse> getAccounts(Long userId, Pageable pageable, Authentication authentication) {
        // Ensure user can only access their own accounts
        Long currentUserId = getCurrentUserId(authentication);
        if (!currentUserId.equals(userId)) {
            throw new ValidationException("Access denied");
        }

        return accountRepository.findByUserId(userId, pageable)
            .map(accountMapper::toResponse);
    }

    /**
     * Get all active accounts for the current user.
     */
    @Transactional(readOnly = true)
    public List<AccountResponse> getActiveAccounts(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return accountRepository.findByUserIdAndIsActiveTrue(userId)
            .stream()
            .map(accountMapper::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get accounts by type for the current user.
     */
    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByType(AccountType accountType, Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return accountRepository.findByUserIdAndAccountType(userId, accountType)
            .stream()
            .map(accountMapper::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get a specific account by ID.
     */
    @Transactional(readOnly = true)
    public AccountResponse getAccount(Long accountId, Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        // Ensure user can only access their own accounts
        if (!account.getUser().getId().equals(userId)) {
            throw new ValidationException("Access denied");
        }

        return accountMapper.toResponse(account);
    }

    /**
     * Get account entity by ID (used by other services).
     */
    @Transactional(readOnly = true)
    public Account getAccountEntityById(Long accountId) {
        return accountRepository.findById(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));
    }

    /**
     * Update an existing account.
     */
    public AccountResponse updateAccount(Long accountId, AccountRequest request, Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        // Ensure user can only update their own accounts
        if (!account.getUser().getId().equals(userId)) {
            throw new ValidationException("Access denied");
        }

        accountMapper.updateEntity(request, account);

        Account updatedAccount = accountRepository.save(account);
        return accountMapper.toResponse(updatedAccount);
    }

    /**
     * Update account balance (used by transaction service).
     */
    public void updateAccountBalance(Long accountId, BigDecimal amount, boolean isCredit) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (isCredit) {
            account.credit(amount);
        } else {
            account.debit(amount);
        }

        accountRepository.save(account);
    }

    /**
     * Soft delete an account (mark as inactive).
     */
    public void deleteAccount(Long accountId, Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        // Ensure user can only delete their own accounts
        if (!account.getUser().getId().equals(userId)) {
            throw new ValidationException("Access denied");
        }

        account.setIsActive(false);
        accountRepository.save(account);
    }

    /**
     * Get total balance for the current user.
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalBalance(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return accountRepository.getTotalBalanceByUserId(userId);
    }

    /**
     * Get accounts by bank name.
     */
    @Transactional(readOnly = true)
    public Page<AccountResponse> getAccountsByBankName(String bankName, Pageable pageable, Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return accountRepository.findByUserIdAndBankNameContainingIgnoreCase(userId, bankName, pageable)
            .map(accountMapper::toResponse);
    }
}