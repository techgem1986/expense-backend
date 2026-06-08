package com.expenseapp.category.repository;

import com.expenseapp.category.domain.Category;
import com.expenseapp.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
     * Find category by name for a specific user.
     *
     * @param name the category name
     * @param user the user
     * @return Optional containing the category if found for the user
     */
    Optional<Category> findByNameAndUser(String name, User user);

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
     * Find categories by user (user-specific only), with pagination.
     *
     * @param user the user
     * @param pageable the pagination information
     * @return Page of categories for the user
     */
    Page<Category> findByUser(User user, Pageable pageable);

    /**
     * Find categories by user and type (user-specific only).
     *
     * @param user the user
     * @param type the category type
     * @return List of categories for the user of the specified type
     */
    List<Category> findByUserAndType(User user, Category.CategoryType type);

    /**
     * Find categories by user OR default user (user_id=1).
     * This allows system-wide categories (like "Money Transfer") to be visible to all users.
     *
     * @param userId the user's ID
     * @param defaultUserId the default/system user's ID (typically 1)
     * @return List of categories for the user or the default user
     */
    @Query("SELECT c FROM Category c WHERE c.user.id = :userId OR c.user.id = :defaultUserId")
    List<Category> findByUserOrDefaultUser(@Param("userId") Long userId, @Param("defaultUserId") Long defaultUserId);

    /**
     * Find categories by user OR default user with pagination.
     *
     * @param userId the user's ID
     * @param defaultUserId the default/system user's ID (typically 1)
     * @param pageable the pagination information
     * @return Page of categories for the user or the default user
     */
    @Query("SELECT c FROM Category c WHERE c.user.id = :userId OR c.user.id = :defaultUserId")
    Page<Category> findByUserOrDefaultUser(@Param("userId") Long userId, @Param("defaultUserId") Long defaultUserId, Pageable pageable);

    /**
     * Find categories by user OR default user and type.
     *
     * @param userId the user's ID
     * @param defaultUserId the default/system user's ID (typically 1)
     * @param type the category type
     * @return List of categories for the user or the default user of the specified type
     */
    @Query("SELECT c FROM Category c WHERE (c.user.id = :userId OR c.user.id = :defaultUserId) AND c.type = :type")
    List<Category> findByUserOrDefaultUserAndType(@Param("userId") Long userId, @Param("defaultUserId") Long defaultUserId, @Param("type") Category.CategoryType type);

    /**
     * Check if a category exists with the given name for a specific user.
     *
     * @param name the category name
     * @param user the user
     * @return true if category exists for the user, false otherwise
     */
    boolean existsByNameAndUser(String name, User user);
}