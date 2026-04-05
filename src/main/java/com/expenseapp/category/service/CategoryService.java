package com.expenseapp.category.service;

import com.expenseapp.category.domain.Category;
import com.expenseapp.category.dto.CategoryRequest;
import com.expenseapp.category.dto.CategoryResponse;
import com.expenseapp.category.repository.CategoryRepository;
import com.expenseapp.shared.exception.ResourceNotFoundException;
import com.expenseapp.shared.exception.ValidationException;
import com.expenseapp.user.domain.User;
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
     * Get all categories for the authenticated user.
     * Users can only see their own categories.
     *
     * @param user the authenticated user
     * @return List of CategoryResponse
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories(User user) {
        return categoryRepository.findByUser(user).stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get categories by type for the authenticated user.
     * Users can only see their own categories.
     *
     * @param user the authenticated user
     * @param type the category type (INCOME or EXPENSE)
     * @return List of CategoryResponse
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoriesByType(User user, Category.CategoryType type) {
        return categoryRepository.findByUserAndType(user, type).stream()
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
     * Get category by name for the authenticated user.
     *
     * @param name the category name
     * @param user the authenticated user
     * @return CategoryResponse
     */
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryByName(String name, User user) {
        Category category = categoryRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with name: " + name));
        
        if (!category.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Category not found with name: " + name);
        }
        
        return mapToCategoryResponse(category);
    }

    /**
     * Create a new category for the authenticated user.
     *
     * @param request the category request
     * @param user the authenticated user
     * @return CategoryResponse
     */
    public CategoryResponse createCategory(CategoryRequest request, User user) {
        // Check if category already exists for this user
        if (categoryRepository.existsByNameAndUser(request.getName(), user)) {
            throw new ValidationException("Category with name '" + request.getName() + "' already exists");
        }

        // Create new category
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setType(request.getType());
        category.setUser(user);

        Category savedCategory = categoryRepository.save(category);
        return mapToCategoryResponse(savedCategory);
    }

    /**
     * Update an existing category for the authenticated user.
     * Users can only update their own categories.
     *
     * @param categoryId the category ID
     * @param request the category request
     * @param user the authenticated user
     * @return CategoryResponse
     */
    public CategoryResponse updateCategory(Long categoryId, CategoryRequest request, User user) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        // Check if category belongs to the current user (compare by user ID)
        if (!category.getUser().getId().equals(user.getId())) {
            throw new ValidationException("You can only update categories that you created");
        }

        // Check if name is being changed and if it conflicts
        if (!category.getName().equals(request.getName()) &&
            categoryRepository.existsByNameAndUser(request.getName(), user)) {
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
     * Delete a category for the authenticated user.
     * Users can only delete their own categories.
     *
     * @param categoryId the category ID
     * @param user the authenticated user
     */
    public void deleteCategory(Long categoryId, User user) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        // Check if category belongs to the current user (compare by user ID)
        if (!category.getUser().getId().equals(user.getId())) {
            throw new ValidationException("You can only delete categories that you created");
        }

        categoryRepository.deleteById(categoryId);
    }

    /**
     * Get category entity by ID for the authenticated user.
     *
     * @param categoryId the category ID
     * @return Category entity
     */
    @Transactional(readOnly = true)
    public Category getCategoryEntityById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        // Note: User ownership check is done at the controller level
        return category;
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