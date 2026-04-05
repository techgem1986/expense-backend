package com.expenseapp.service;

import com.expenseapp.category.domain.Category;
import com.expenseapp.category.dto.CategoryRequest;
import com.expenseapp.category.dto.CategoryResponse;
import com.expenseapp.category.repository.CategoryRepository;
import com.expenseapp.category.service.CategoryService;
import com.expenseapp.shared.exception.ResourceNotFoundException;
import com.expenseapp.shared.exception.ValidationException;
import com.expenseapp.user.domain.User;
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
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        
        testCategory = new Category("Food", "Food expenses", EXPENSE, testUser);
        testCategory.setId(1L);
    }

    @Test
    void shouldGetAllCategories() {
        when(categoryRepository.findByUser(testUser)).thenReturn(List.of(testCategory));

        List<CategoryResponse> categories = categoryService.getAllCategories(testUser);

        assertNotNull(categories);
        assertEquals(1, categories.size());
        assertEquals("Food", categories.get(0).getName());
    }

    @Test
    void shouldGetCategoriesByType() {
        when(categoryRepository.findByUserAndType(testUser, EXPENSE)).thenReturn(List.of(testCategory));

        List<CategoryResponse> categories = categoryService.getCategoriesByType(testUser, EXPENSE);

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

        CategoryResponse response = categoryService.getCategoryByName("Food", testUser);

        assertNotNull(response);
        assertEquals("Food", response.getName());
    }

    @Test
    void shouldThrowExceptionWhenCategoryNameNotFound() {
        when(categoryRepository.findByName("Food")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            categoryService.getCategoryByName("Food", testUser);
        });
    }

    @Test
    void shouldThrowExceptionWhenCategoryNotOwnedByUser() {
        User otherUser = new User();
        otherUser.setId(2L);
        Category otherCategory = new Category("Food", "Food expenses", EXPENSE, otherUser);
        otherCategory.setId(1L);
        
        when(categoryRepository.findByName("Food")).thenReturn(Optional.of(otherCategory));

        assertThrows(ResourceNotFoundException.class, () -> {
            categoryService.getCategoryByName("Food", testUser);
        });
    }

    @Test
    void shouldCreateCategory() {
        CategoryRequest request = new CategoryRequest("Food", "Food expenses", EXPENSE);
        when(categoryRepository.existsByNameAndUser("Food", testUser)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        CategoryResponse response = categoryService.createCategory(request, testUser);

        assertNotNull(response);
        assertEquals("Food", response.getName());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void shouldThrowExceptionWhenCategoryNameExists() {
        CategoryRequest request = new CategoryRequest("Food", "Food expenses", EXPENSE);
        when(categoryRepository.existsByNameAndUser("Food", testUser)).thenReturn(true);

        assertThrows(ValidationException.class, () -> {
            categoryService.createCategory(request, testUser);
        });
    }

    @Test
    void shouldUpdateCategory() {
        CategoryRequest request = new CategoryRequest("Updated Food", "Updated description", EXPENSE);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.existsByNameAndUser("Updated Food", testUser)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        CategoryResponse response = categoryService.updateCategory(1L, request, testUser);

        assertNotNull(response);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatedCategoryNotOwned() {
        User otherUser = new User();
        otherUser.setId(2L);
        Category otherCategory = new Category("Food", "Food expenses", EXPENSE, otherUser);
        otherCategory.setId(1L);
        
        CategoryRequest request = new CategoryRequest("Updated Food", "Updated description", EXPENSE);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(otherCategory));

        assertThrows(ValidationException.class, () -> {
            categoryService.updateCategory(1L, request, testUser);
        });
    }

    @Test
    void shouldThrowExceptionWhenUpdatedNameExists() {
        CategoryRequest request = new CategoryRequest("Updated Food", "Updated description", EXPENSE);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.existsByNameAndUser("Updated Food", testUser)).thenReturn(true);

        assertThrows(ValidationException.class, () -> {
            categoryService.updateCategory(1L, request, testUser);
        });
    }

    @Test
    void shouldDeleteCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        categoryService.deleteCategory(1L, testUser);

        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonexistentCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            categoryService.deleteCategory(1L, testUser);
        });
    }

    @Test
    void shouldThrowExceptionWhenDeletingCategoryNotOwned() {
        User otherUser = new User();
        otherUser.setId(2L);
        Category otherCategory = new Category("Food", "Food expenses", EXPENSE, otherUser);
        otherCategory.setId(1L);
        
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(otherCategory));

        assertThrows(ValidationException.class, () -> {
            categoryService.deleteCategory(1L, testUser);
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