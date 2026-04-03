package com.expenseapp.budget.mapper;

import com.expenseapp.budget.domain.Budget;
import com.expenseapp.budget.dto.BudgetRequest;
import com.expenseapp.budget.dto.BudgetResponse;
import com.expenseapp.category.mapper.CategoryMapper;
import com.expenseapp.category.dto.CategoryResponse;
import com.expenseapp.category.domain.Category;
import com.expenseapp.user.mapper.UserMapper;
import com.expenseapp.user.dto.UserResponse;
import com.expenseapp.user.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.List;

/**
 * Mapper for converting between Budget entities and DTOs.
 */
@Mapper(componentModel = "spring")
public interface BudgetMapper {

    BudgetMapper INSTANCE = Mappers.getMapper(BudgetMapper.class);

    CategoryMapper CATEGORY_MAPPER = Mappers.getMapper(CategoryMapper.class);
    UserMapper USER_MAPPER = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "currentSpent", ignore = true)
    @Mapping(target = "remainingAmount", ignore = true)
    @Mapping(target = "spentPercentage", ignore = true)
    @Mapping(target = "isOverBudget", ignore = true)
    @Mapping(target = "categories", ignore = true)
    BudgetResponse toResponse(Budget budget);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "budgetCategories", ignore = true)
    Budget toEntity(BudgetRequest request);

    default BudgetResponse toResponseWithCalculations(Budget budget, User user,
                                                     List<Category> categories,
                                                     BigDecimal currentSpent) {
        BudgetResponse response = toResponse(budget);
        if (user != null) {
            response.setUser(USER_MAPPER.toResponse(user));
        }
        if (categories != null) {
            response.setCategories(categories.stream()
                .map(CATEGORY_MAPPER::toResponse)
                .toList());
        }

        // Calculate derived fields
        BigDecimal limitAmount = budget.getLimitAmount();
        response.setCurrentSpent(currentSpent);
        response.setRemainingAmount(limitAmount.subtract(currentSpent));
        response.setSpentPercentage(currentSpent.divide(limitAmount, 4, BigDecimal.ROUND_HALF_UP).doubleValue());
        response.setIsOverBudget(currentSpent.compareTo(limitAmount) > 0);

        return response;
    }
}