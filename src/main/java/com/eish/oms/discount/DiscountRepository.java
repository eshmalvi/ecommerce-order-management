package com.eish.oms.discount;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Data access for the {@code discount} table. The SQL lives in {@link DiscountSql}.
 */
@Repository
public class DiscountRepository {

    private final JdbcClient jdbc;

    public DiscountRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Inserts a discount code. A duplicate code fails on the primary key. */
    public Discount insert(String code, BigDecimal percentOff) {
        jdbc.sql(DiscountSql.INSERT)
                .param("code", code)
                .param("percentOff", percentOff)
                .update();
        return new Discount(code, percentOff);
    }

    public List<Discount> findAll() {
        return jdbc.sql(DiscountSql.FIND_ALL).query(Discount.class).list();
    }

    public Optional<Discount> findByCode(String code) {
        return jdbc.sql(DiscountSql.FIND_BY_CODE)
                .param("code", code)
                .query(Discount.class)
                .optional();
    }
}
