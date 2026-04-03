package com.expenseapp.category.dto;

import com.expenseapp.category.domain.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for category creation/update requests.
 */
public class CategoryRequest {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private Category.CategoryType type;

    // Constructors
    public CategoryRequest() {}

    public CategoryRequest(String name, String description, Category.CategoryType type) {
        this.name = name;
        this.description = description;
        this.type = type;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category.CategoryType getType() {
        return type;
    }

    public void setType(Category.CategoryType type) {
        this.type = type;
    }
}