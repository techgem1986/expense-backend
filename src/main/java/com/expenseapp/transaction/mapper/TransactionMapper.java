package com.expenseapp.transaction.mapper;

import com.expenseapp.transaction.domain.Transaction;
import com.expenseapp.transaction.domain.Transaction.TransactionType;
import com.expenseapp.transaction.dto.TransactionRequest;
import com.expenseapp.transaction.dto.TransactionResponse;
import com.expenseapp.account.domain.Account;
import com.expenseapp.account.mapper.AccountMapper;
import com.expenseapp.account.dto.AccountResponse;
import com.expenseapp.category.mapper.CategoryMapper;
import com.expenseapp.category.dto.CategoryResponse;
import com.expenseapp.category.domain.Category;
import com.expenseapp.user.mapper.UserMapper;
import com.expenseapp.user.dto.UserResponse;
import com.expenseapp.user.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.format.DateTimeFormatter;

/**
 * Mapper for converting between Transaction entities and DTOs.
 */
@Mapper(componentModel = "spring")
public interface TransactionMapper {

    TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);

    CategoryMapper CATEGORY_MAPPER = Mappers.getMapper(CategoryMapper.class);
    UserMapper USER_MAPPER = Mappers.getMapper(UserMapper.class);
    AccountMapper ACCOUNT_MAPPER = Mappers.getMapper(AccountMapper.class);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "fromAccount", ignore = true)
    @Mapping(target = "toAccount", ignore = true)
    TransactionResponse toResponse(Transaction transaction);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "fromAccount", source = "fromAccountId", ignore = true)
    @Mapping(target = "toAccount", source = "toAccountId", ignore = true)
    @Mapping(target = "isRecurringInstance", ignore = true)
    @Mapping(target = "linkedRecurringTransactionId", ignore = true)
    Transaction toEntity(TransactionRequest request);

    default TransactionResponse toResponseWithUserAndCategory(Transaction transaction, User user, Category category) {
        TransactionResponse response = toResponse(transaction);
        if (user != null) {
            response.setUser(USER_MAPPER.toResponse(user));
        }
        if (category != null) {
            response.setCategory(CATEGORY_MAPPER.toResponse(category));
        }
        // Manually map accounts to avoid LazyInitializationException from AccountMapper accessing user
        if (transaction.getFromAccount() != null) {
            response.setFromAccount(mapAccountToResponse(transaction.getFromAccount()));
        }
        if (transaction.getToAccount() != null) {
            response.setToAccount(mapAccountToResponse(transaction.getToAccount()));
        }
        return response;
    }

    default AccountResponse mapAccountToResponse(Account account) {
        if (account == null) {
            return null;
        }
        AccountResponse response = new AccountResponse();
        response.setId(account.getId());
        response.setName(account.getName());
        response.setAccountType(account.getAccountType());
        response.setBankName(account.getBankName());
        response.setAccountNumber(account.getAccountNumber());
        response.setIfscCode(account.getIfscCode());
        response.setOpeningBalance(account.getOpeningBalance());
        response.setCurrentBalance(account.getCurrentBalance());
        response.setDescription(account.getDescription());
        response.setIsActive(account.getIsActive());
        response.setCreatedAt(account.getCreatedAt());
        response.setUpdatedAt(account.getUpdatedAt());
        // Set userId from user if available, otherwise null
        // Use Hibernate.isInitialized() check or just access the id directly if user is a proxy
        try {
            if (account.getUser() != null) {
                response.setUserId(account.getUser().getId());
            }
        } catch (Exception e) {
            // User is not initialized, leave userId as null
        }
        return response;
    }

    default TransactionType mapStringToTransactionType(String type) {
        if (type == null) {
            return null;
        }
        return TransactionType.valueOf(type.toUpperCase());
    }

    default String mapTransactionTypeToString(TransactionType type) {
        if (type == null) {
            return null;
        }
        return type.name();
    }
}