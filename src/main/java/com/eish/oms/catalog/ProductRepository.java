package com.eish.oms.catalog;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * SQL for the {@code product} table.
 */
@Repository
public class ProductRepository {

    private static final String COLUMNS = "id, sku, name, price, category_id";

    private final JdbcClient jdbc;

    public ProductRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Inserts a product and returns it with its generated id. Duplicate SKUs fail on the unique constraint. */
    public Product insert(String sku, String name, BigDecimal price, long categoryId) {
        Long id = jdbc.sql("""
                insert into product (sku, name, price, category_id)
                values (:sku, :name, :price, :categoryId)
                returning id
                """)
                .param("sku", sku)
                .param("name", name)
                .param("price", price)
                .param("categoryId", categoryId)
                .query(Long.class)
                .single();
        return new Product(id, sku, name, price, categoryId);
    }

    public List<Product> findAll() {
        return jdbc.sql("select " + COLUMNS + " from product order by name")
                .query(Product.class)
                .list();
    }

    public List<Product> findByCategory(long categoryId) {
        return jdbc.sql("select " + COLUMNS + " from product where category_id = :categoryId order by name")
                .param("categoryId", categoryId)
                .query(Product.class)
                .list();
    }

    public Optional<Product> findById(long id) {
        return jdbc.sql("select " + COLUMNS + " from product where id = :id")
                .param("id", id)
                .query(Product.class)
                .optional();
    }

    public Optional<Product> findBySku(String sku) {
        return jdbc.sql("select " + COLUMNS + " from product where sku = :sku")
                .param("sku", sku)
                .query(Product.class)
                .optional();
    }
}
