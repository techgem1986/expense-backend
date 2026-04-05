package com.expenseapp.category.repository;

import com.expenseapp.category.domain.Category;
import com.expenseapp.user.domain.User;
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

    /**
     * Find categories by user.
     *
     * @param user the user
     * @return List of categories for the user
     */
    List<Category> findByUser(User user);

    /**
     * Find categories by user and type.
     *
     * @param user the user
     * @param type the category type
     * @return List of categories for the user of the specified type
     */
    List<Category> findByUserAndType(User user, Category.CategoryType type);

    /**
     * Check if a category exists with the given name for a specific user.
     *
     * @param name the category name
     * @param user the user
     * @return true if category exists for the user, false otherwise
     */
    boolean existsByNameAndUser(String name, User user);
}
