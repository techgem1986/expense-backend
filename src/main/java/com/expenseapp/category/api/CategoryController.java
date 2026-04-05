package com.expenseapp.category.api;

import com.expenseapp.category.domain.Category;
import com.expenseapp.category.dto.CategoryRequest;
import com.expenseapp.category.dto.CategoryResponse;
import com.expenseapp.category.service.CategoryService;
import com.expenseapp.shared.dto.ApiResponse;
import com.expenseapp.user.domain.User;
import com.expenseapp.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for category management operations.
 */
@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "Category management APIs")
public class CategoryController {

    private final CategoryService categoryService;
    private final UserService userService;

    public CategoryController(CategoryService categoryService, UserService userService) {
        this.categoryService = categoryService;
        this.userService = userService;
    }

    /**
     * Get all categories for the authenticated user.
     *
     * @param authentication the current authentication
     * @return List of CategoryResponse
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all categories", description = "Retrieves all categories for the authenticated user")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories(Authentication authentication) {
        String email = authentication.getName();
        User user = userService.getUserEntityByEmail(email);
        List<CategoryResponse> categories = categoryService.getAllCategories(user);
        return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", categories));
    }

    /**
     * Get categories by type for the authenticated user.
     *
     * @param type the category type (INCOME or EXPENSE)
     * @param authentication the current authentication
     * @return List of CategoryResponse
     */
    @GetMapping("/type/{type}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get categories by type", description = "Retrieves categories filtered by type for the authenticated user")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategoriesByType(
            @PathVariable Category.CategoryType type,
            Authentication authentication) {
        String email = authentication.getName();
        User user = userService.getUserEntityByEmail(email);
        List<CategoryResponse> categories = categoryService.getCategoriesByType(user, type);
        return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", categories));
    }

    /**
     * Get category by ID for the authenticated user.
     *
     * @param categoryId the category ID
     * @return CategoryResponse
     */
    @GetMapping("/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get category by ID", description = "Retrieves a category by its ID for the authenticated user")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable Long categoryId) {
        CategoryResponse category = categoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(ApiResponse.success("Category retrieved successfully", category));
    }

    /**
     * Create a new category for the authenticated user.
     *
     * @param request the category request
     * @param authentication the current authentication
     * @return CategoryResponse
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create category", description = "Creates a new category for the authenticated user")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        User user = userService.getUserEntityByEmail(email);
        CategoryResponse category = categoryService.createCategory(request, user);
        return ResponseEntity.ok(ApiResponse.success("Category created successfully", category));
    }

    /**
     * Update an existing category for the authenticated user.
     *
     * @param categoryId the category ID
     * @param request the category request
     * @param authentication the current authentication
     * @return CategoryResponse
     */
    @PutMapping("/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update category", description = "Updates an existing category for the authenticated user")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        User user = userService.getUserEntityByEmail(email);
        CategoryResponse category = categoryService.updateCategory(categoryId, request, user);
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", category));
    }

    /**
     * Delete a category for the authenticated user.
     *
     * @param categoryId the category ID
     * @param authentication the current authentication
     */
    @DeleteMapping("/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete category", description = "Deletes a category for the authenticated user")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable Long categoryId,
            Authentication authentication) {
        String email = authentication.getName();
        User user = userService.getUserEntityByEmail(email);
        categoryService.deleteCategory(categoryId, user);
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully", null));
    }
}
