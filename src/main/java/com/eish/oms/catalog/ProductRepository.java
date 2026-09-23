package com.eish.oms.catalog;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.eish.oms.common.Params;

/**
 * Data access for the {@code product} table. The SQL lives in {@link ProductSql}.
 */
@Repository
public class ProductRepository {

    private final JdbcClient jdbc;

    public ProductRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Inserts a product and returns it with its generated id. Duplicate SKUs fail on the unique constraint. */
    public Product insert(String sku, String name, BigDecimal price, long categoryId) {
        Long id = jdbc.sql(ProductSql.INSERT)
                .param(Params.SKU, sku)
                .param(Params.NAME, name)
                .param(Params.PRICE, price)
                .param(Params.CATEGORY_ID, categoryId)
                .query(Long.class)
                .single();
        return new Product(id, sku, name, price, categoryId);
    }

    public List<Product> findAll() {
        return jdbc.sql(ProductSql.FIND_ALL).query(Product.class).list();
    }

    public List<Product> findByCategory(long categoryId) {
        return jdbc.sql(ProductSql.FIND_BY_CATEGORY)
                .param(Params.CATEGORY_ID, categoryId)
                .query(Product.class)
                .list();
    }

    public Optional<Product> findById(long id) {
        return jdbc.sql(ProductSql.FIND_BY_ID)
                .param(Params.ID, id)
                .query(Product.class)
                .optional();
    }

    public Optional<Product> findBySku(String sku) {
        return jdbc.sql(ProductSql.FIND_BY_SKU)
                .param(Params.SKU, sku)
                .query(Product.class)
                .optional();
    }
}
