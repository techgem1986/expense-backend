package com.expenseapp.transaction.mapper;

import com.expenseapp.transaction.domain.Transaction;
import com.expenseapp.transaction.domain.Transaction.TransactionType;
import com.expenseapp.transaction.dto.TransactionRequest;
import com.expenseapp.transaction.dto.TransactionResponse;
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

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "category", ignore = true)
    TransactionResponse toResponse(Transaction transaction);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "category", ignore = true)
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