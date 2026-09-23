package com.eish.oms.inventory;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.eish.oms.common.Params;

/**
 * Data access for the {@code warehouse} table. The SQL lives in {@link WarehouseSql}.
 */
@Repository
public class WarehouseRepository {

    private final JdbcClient jdbc;

    public WarehouseRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Inserts a warehouse and returns it with its generated id. Duplicate names fail on the unique constraint. */
    public Warehouse insert(String name) {
        Long id = jdbc.sql(WarehouseSql.INSERT)
                .param(Params.NAME, name)
                .query(Long.class)
                .single();
        return new Warehouse(id, name);
    }

    public List<Warehouse> findAll() {
        return jdbc.sql(WarehouseSql.FIND_ALL).query(Warehouse.class).list();
    }

    public Optional<Warehouse> findById(long id) {
        return jdbc.sql(WarehouseSql.FIND_BY_ID)
                .param(Params.ID, id)
                .query(Warehouse.class)
                .optional();
    }
}
