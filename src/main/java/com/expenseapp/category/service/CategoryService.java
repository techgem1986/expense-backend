package com.expenseapp.category.service;

import com.expenseapp.category.domain.Category;
import com.expenseapp.category.dto.CategoryRequest;
import com.expenseapp.category.dto.CategoryResponse;
import com.expenseapp.category.repository.CategoryRepository;
import com.expenseapp.shared.exception.ResourceNotFoundException;
import com.expenseapp.shared.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for category-related business logic.
 */
@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Get all categories.
     *
     * @return List of CategoryResponse
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "categories", key = "'all'")
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get categories by type.
     *
     * @param type the category type (INCOME or EXPENSE)
     * @return List of CategoryResponse
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoriesByType(Category.CategoryType type) {
        return categoryRepository.findByType(type).stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get category by ID.
     *
     * @param categoryId the category ID
     * @return CategoryResponse
     */
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
        return mapToCategoryResponse(category);
    }

    /**
     * Get category by name.
     *
     * @param name the category name
     * @return CategoryResponse
     */
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryByName(String name) {
        Category category = categoryRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with name: " + name));
        return mapToCategoryResponse(category);
    }

    /**
     * Create a new category.
     *
     * @param request the category request
     * @return CategoryResponse
     */
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse createCategory(CategoryRequest request) {
        // Check if category already exists
        if (categoryRepository.existsByName(request.getName())) {
            throw new ValidationException("Category with name '" + request.getName() + "' already exists");
        }

        // Create new category
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setType(request.getType());

        Category savedCategory = categoryRepository.save(category);
        return mapToCategoryResponse(savedCategory);
    }

    /**
     * Update an existing category.
     *
     * @param categoryId the category ID
     * @param request the category request
     * @return CategoryResponse
     */
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse updateCategory(Long categoryId, CategoryRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        // Check if name is being changed and if it conflicts
        if (!category.getName().equals(request.getName()) &&
            categoryRepository.existsByName(request.getName())) {
            throw new ValidationException("Category with name '" + request.getName() + "' already exists");
        }

        // Update category
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setType(request.getType());

        Category savedCategory = categoryRepository.save(category);
        return mapToCategoryResponse(savedCategory);
    }

    /**
     * Delete a category.
     *
     * @param categoryId the category ID
     */
    @CacheEvict(value = "categories", allEntries = true)
    public void deleteCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category not found with id: " + categoryId);
        }
        categoryRepository.deleteById(categoryId);
    }

    /**
     * Get category entity by ID.
     *
     * @param categoryId the category ID
     * @return Category entity
     */
    @Transactional(readOnly = true)
    public Category getCategoryEntityById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
    }

    /**
     * Map Category entity to CategoryResponse DTO.
     *
     * @param category the category entity
     * @return CategoryResponse DTO
     */
    public CategoryResponse mapToCategoryResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getType(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}