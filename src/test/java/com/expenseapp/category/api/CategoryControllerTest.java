package com.expenseapp.category.api;

import com.expenseapp.category.dto.CategoryRequest;
import com.expenseapp.category.dto.CategoryResponse;
import com.expenseapp.category.service.CategoryService;
import com.expenseapp.shared.dto.ApiResponse;
import com.expenseapp.user.domain.User;
import com.expenseapp.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.expenseapp.category.domain.Category.CategoryType.EXPENSE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = CategoryController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class,
                org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
                org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class
        })
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private UserService userService;

    private CategoryResponse testCategoryResponse;
    private CategoryRequest testCategoryRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        
        testCategoryRequest = new CategoryRequest("Food", "Food expenses", EXPENSE);
        testCategoryResponse = new CategoryResponse(1L, "Food", "Food expenses", EXPENSE, null, null);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldGetAllCategories() throws Exception {
        when(userService.getUserEntityByEmail("test@example.com")).thenReturn(testUser);
        when(categoryService.getAllCategories(testUser)).thenReturn(List.of(testCategoryResponse));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Food"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldGetCategoriesByType() throws Exception {
        when(userService.getUserEntityByEmail("test@example.com")).thenReturn(testUser);
        when(categoryService.getCategoriesByType(testUser, EXPENSE)).thenReturn(List.of(testCategoryResponse));

        mockMvc.perform(get("/api/categories/type/EXPENSE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].type").value("EXPENSE"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldGetCategoryById() throws Exception {
        when(categoryService.getCategoryById(1L)).thenReturn(testCategoryResponse);

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldCreateCategory() throws Exception {
        when(userService.getUserEntityByEmail("test@example.com")).thenReturn(testUser);
        when(categoryService.createCategory(any(CategoryRequest.class), any(User.class))).thenReturn(testCategoryResponse);

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCategoryRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Food"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldUpdateCategory() throws Exception {
        when(userService.getUserEntityByEmail("test@example.com")).thenReturn(testUser);
        when(categoryService.updateCategory(eq(1L), any(CategoryRequest.class), any(User.class))).thenReturn(testCategoryResponse);

        mockMvc.perform(put("/api/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCategoryRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldDeleteCategory() throws Exception {
        when(userService.getUserEntityByEmail("test@example.com")).thenReturn(testUser);
        doNothing().when(categoryService).deleteCategory(1L, testUser);

        mockMvc.perform(delete("/api/categories/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldFailWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isUnauthorized());
    }

}