package com.expenseapp.service;

import com.expenseapp.category.domain.Category;
import com.expenseapp.category.dto.CategoryRequest;
import com.expenseapp.category.dto.CategoryResponse;
import com.expenseapp.category.repository.CategoryRepository;
import com.expenseapp.category.service.CategoryService;
import com.expenseapp.shared.exception.ResourceNotFoundException;
import com.expenseapp.shared.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.expenseapp.category.domain.Category.CategoryType.EXPENSE;
import static com.expenseapp.category.domain.Category.CategoryType.INCOME;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category("Food", "Food expenses", EXPENSE);
        testCategory.setId(1L);
    }

    @Test
    void shouldGetAllCategories() {
        when(categoryRepository.findAll()).thenReturn(List.of(testCategory));

        List<CategoryResponse> categories = categoryService.getAllCategories();

        assertNotNull(categories);
        assertEquals(1, categories.size());
        assertEquals("Food", categories.get(0).getName());
    }

    @Test
    void shouldGetCategoriesByType() {
        when(categoryRepository.findByType(EXPENSE)).thenReturn(List.of(testCategory));

        List<CategoryResponse> categories = categoryService.getCategoriesByType(EXPENSE);

        assertNotNull(categories);
        assertEquals(1, categories.size());
    }

    @Test
    void shouldGetCategoryById() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        CategoryResponse response = categoryService.getCategoryById(1L);

        assertNotNull(response);
        assertEquals("Food", response.getName());
    }

    @Test
    void shouldThrowExceptionWhenCategoryNotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            categoryService.getCategoryById(1L);
        });
    }

    @Test
    void shouldGetCategoryByName() {
        when(categoryRepository.findByName("Food")).thenReturn(Optional.of(testCategory));

        CategoryResponse response = categoryService.getCategoryByName("Food");

        assertNotNull(response);
        assertEquals("Food", response.getName());
    }

    @Test
    void shouldThrowExceptionWhenCategoryNameNotFound() {
        when(categoryRepository.findByName("Food")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            categoryService.getCategoryByName("Food");
        });
    }

    @Test
    void shouldCreateCategory() {
        CategoryRequest request = new CategoryRequest("Food", "Food expenses", EXPENSE);
        when(categoryRepository.existsByName("Food")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        CategoryResponse response = categoryService.createCategory(request);

        assertNotNull(response);
        assertEquals("Food", response.getName());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void shouldThrowExceptionWhenCategoryNameExists() {
        CategoryRequest request = new CategoryRequest("Food", "Food expenses", EXPENSE);
        when(categoryRepository.existsByName("Food")).thenReturn(true);

        assertThrows(ValidationException.class, () -> {
            categoryService.createCategory(request);
        });
    }

    @Test
    void shouldUpdateCategory() {
        CategoryRequest request = new CategoryRequest("Updated Food", "Updated description", EXPENSE);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.existsByName("Updated Food")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        CategoryResponse response = categoryService.updateCategory(1L, request);

        assertNotNull(response);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatedNameExists() {
        Category testCat = new Category("Food", "Food expenses", EXPENSE);
        testCat.setId(1L);
        CategoryRequest request = new CategoryRequest("Updated Food", "Updated description", EXPENSE);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCat));
        when(categoryRepository.existsByName("Updated Food")).thenReturn(true);

        assertThrows(ValidationException.class, () -> {
            categoryService.updateCategory(1L, request);
        });
    }

    @Test
    void shouldDeleteCategory() {
        when(categoryRepository.existsById(1L)).thenReturn(true);

        categoryService.deleteCategory(1L);

        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonexistentCategory() {
        when(categoryRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            categoryService.deleteCategory(1L);
        });
    }

    @Test
    void shouldGetCategoryEntityById() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        Category category = categoryService.getCategoryEntityById(1L);

        assertNotNull(category);
        assertEquals(1L, category.getId());
    }

    @Test
    void shouldThrowExceptionWhenCategoryEntityNotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            categoryService.getCategoryEntityById(1L);
        });
    }

    @Test
    void shouldMapToCategoryResponse() {
        CategoryResponse response = categoryService.mapToCategoryResponse(testCategory);

        assertNotNull(response);
        assertEquals("Food", response.getName());
        assertEquals("Food expenses", response.getDescription());
        assertEquals(EXPENSE, response.getType());
    }
}