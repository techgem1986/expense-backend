package com.expenseapp.category.repository;

import com.expenseapp.category.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Category entity operations.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Find category by name.
     *
     * @param name the category name
     * @return Optional containing the category if found
     */
    Optional<Category> findByName(String name);

    /**
     * Find categories by type.
     *
     * @param type the category type (INCOME or EXPENSE)
     * @return List of categories of the specified type
     */
    List<Category> findByType(Category.CategoryType type);

    /**
     * Check if a category exists with the given name.
     *
     * @param name the category name
     * @return true if category exists, false otherwise
     */
    boolean existsByName(String name);
}