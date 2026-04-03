package com.expenseapp.category.mapper;

import com.expenseapp.category.domain.Category;
import com.expenseapp.category.domain.Category.CategoryType;
import com.expenseapp.category.dto.CategoryRequest;
import com.expenseapp.category.dto.CategoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Mapper for converting between Category entities and DTOs.
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Category toEntity(CategoryRequest request);

    CategoryResponse toResponse(Category category);

    default CategoryType mapStringToCategoryType(String type) {
        if (type == null) {
            return null;
        }
        return CategoryType.valueOf(type.toUpperCase());
    }

    default String mapCategoryTypeToString(CategoryType type) {
        if (type == null) {
            return null;
        }
        return type.name();
    }
}