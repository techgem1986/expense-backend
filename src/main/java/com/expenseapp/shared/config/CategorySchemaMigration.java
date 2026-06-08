package com.expenseapp.shared.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;

/**
 * Ensures the categories table uses a per-user unique constraint on the category name.
 * This is required for setups where the database was created with a legacy global unique
 * constraint on categories.name.
 */
@Component
public class CategorySchemaMigration {

    private static final Logger log = LoggerFactory.getLogger(CategorySchemaMigration.class);

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public CategorySchemaMigration(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void migrateCategorySchema() {
        if (!isPostgresDatabase()) {
            log.debug("Skipping category schema migration because database is not PostgreSQL");
            return;
        }

        try {
            if (!hasCategoriesTable()) {
                log.debug("Skipping category schema migration because categories table does not exist yet");
                return;
            }

            dropLegacyCategoryNameUniqueConstraint();
            ensureNameUserUniqueConstraint();
        } catch (Exception ex) {
            log.error("Unable to migrate category schema", ex);
        }
    }

    private boolean isPostgresDatabase() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            return metaData.getDatabaseProductName().toLowerCase().contains("postgres");
        } catch (SQLException ex) {
            log.warn("Unable to determine database product name for category schema migration", ex);
            return false;
        }
    }

    private boolean hasCategoriesTable() {
        String sql = "SELECT 1 FROM information_schema.tables WHERE table_name = 'categories' AND table_schema = current_schema()";
        try {
            List<Integer> rows = jdbcTemplate.queryForList(sql, Integer.class);
            return !rows.isEmpty();
        } catch (Exception ex) {
            log.warn("Unable to verify categories table existence", ex);
            return false;
        }
    }

    private void dropLegacyCategoryNameUniqueConstraint() {
        String query = "SELECT tc.constraint_name " +
                "FROM information_schema.table_constraints tc " +
                "JOIN information_schema.constraint_column_usage ccu " +
                "  ON tc.constraint_schema = ccu.constraint_schema " +
                " AND tc.constraint_name = ccu.constraint_name " +
                "WHERE tc.table_name = 'categories' " +
                "  AND tc.constraint_type = 'UNIQUE' " +
                "GROUP BY tc.constraint_name " +
                "HAVING count(*) = 1 AND bool_and(ccu.column_name = 'name')";

        List<String> constraintNames = jdbcTemplate.queryForList(query, String.class);
        for (String constraintName : constraintNames) {
            String dropSql = "ALTER TABLE categories DROP CONSTRAINT IF EXISTS " + constraintName;
            log.info("Dropping legacy categories unique constraint: {}", constraintName);
            jdbcTemplate.execute(dropSql);
        }
    }

    private void ensureNameUserUniqueConstraint() {
        String checkSql = "SELECT tc.constraint_name " +
                "FROM information_schema.table_constraints tc " +
                "JOIN information_schema.constraint_column_usage ccu " +
                "  ON tc.constraint_schema = ccu.constraint_schema " +
                " AND tc.constraint_name = ccu.constraint_name " +
                "WHERE tc.table_name = 'categories' " +
                "  AND tc.constraint_type = 'UNIQUE' " +
                "GROUP BY tc.constraint_name " +
                "HAVING count(*) = 2 AND bool_and(ccu.column_name IN ('name', 'user_id'))";

        List<String> existing = jdbcTemplate.queryForList(checkSql, String.class);
        if (!existing.isEmpty()) {
            log.debug("Categories table already has a name+user_id unique constraint: {}", existing);
            return;
        }

        log.info("Adding composite unique constraint uq_categories_name_user on categories(name, user_id)");
        jdbcTemplate.execute("DELETE FROM categories c1 USING categories c2 " +
                "WHERE c1.id > c2.id AND c1.name = c2.name AND c1.user_id = c2.user_id");
        jdbcTemplate.execute("ALTER TABLE categories ADD CONSTRAINT uq_categories_name_user UNIQUE (name, user_id)");
    }
}
