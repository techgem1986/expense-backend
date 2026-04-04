package com.expenseapp.recurring.mapper;

import com.expenseapp.recurring.domain.RecurringTransaction;
import com.expenseapp.recurring.dto.RecurringTransactionRequest;
import com.expenseapp.recurring.dto.RecurringTransactionResponse;
import com.expenseapp.category.mapper.CategoryMapper;
import com.expenseapp.category.dto.CategoryResponse;
import com.expenseapp.category.domain.Category;
import com.expenseapp.user.mapper.UserMapper;
import com.expenseapp.user.dto.UserResponse;
import com.expenseapp.user.domain.User;
import com.expenseapp.account.domain.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Mapper for converting between RecurringTransaction entities and DTOs.
 */
@Mapper(componentModel = "spring")
public interface RecurringTransactionMapper {

    RecurringTransactionMapper INSTANCE = Mappers.getMapper(RecurringTransactionMapper.class);

    CategoryMapper CATEGORY_MAPPER = Mappers.getMapper(CategoryMapper.class);
    UserMapper USER_MAPPER = Mappers.getMapper(UserMapper.class);

@Mapping(target = "user", ignore = true)
@Mapping(target = "category", ignore = true)
@Mapping(target = "fromAccount", ignore = true)
@Mapping(target = "toAccount", ignore = true)
RecurringTransactionResponse toResponse(RecurringTransaction recurringTransaction);

@Mapping(target = "user", ignore = true)
@Mapping(target = "category", ignore = true)
@Mapping(target = "isActive", ignore = true)
@Mapping(target = "fromAccount", ignore = true)
@Mapping(target = "toAccount", ignore = true)
RecurringTransaction toEntity(RecurringTransactionRequest request);

    default RecurringTransactionResponse toResponseWithUserAndCategory(RecurringTransaction recurringTransaction, User user, Category category) {
        RecurringTransactionResponse response = toResponse(recurringTransaction);
        if (user != null) {
            response.setUser(USER_MAPPER.toResponse(user));
        }
        if (category != null) {
            response.setCategory(CATEGORY_MAPPER.toResponse(category));
        }
        return response;
    }

    default RecurringTransactionResponse toResponseWithAccounts(RecurringTransaction recurringTransaction, User user, Category category, Account fromAccount, Account toAccount) {
        RecurringTransactionResponse response = toResponseWithUserAndCategory(recurringTransaction, user, category);
        if (fromAccount != null) {
            response.setFromAccount(mapAccountToResponse(fromAccount));
        }
        if (toAccount != null) {
            response.setToAccount(mapAccountToResponse(toAccount));
        }
        return response;
    }

    default RecurringTransactionResponse.AccountResponse mapAccountToResponse(Account account) {
        RecurringTransactionResponse.AccountResponse response = new RecurringTransactionResponse.AccountResponse();
        response.setId(account.getId());
        response.setName(account.getName());
        response.setAccountType(account.getAccountType().name());
        return response;
    }
}