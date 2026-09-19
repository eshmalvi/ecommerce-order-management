package com.eish.oms.catalog;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Data access for the {@code category} table. The SQL lives in {@link CategorySql}.
 */
@Repository
public class CategoryRepository {

    private final JdbcClient jdbc;

    public CategoryRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Inserts a category and returns it with its generated id. Duplicate names fail on the unique constraint. */
    public Category insert(String name) {
        Long id = jdbc.sql(CategorySql.INSERT)
                .param("name", name)
                .query(Long.class)
                .single();
        return new Category(id, name);
    }

    public List<Category> findAll() {
        return jdbc.sql(CategorySql.FIND_ALL).query(Category.class).list();
    }

    public Optional<Category> findById(long id) {
        return jdbc.sql(CategorySql.FIND_BY_ID)
                .param("id", id)
                .query(Category.class)
                .optional();
    }
}
