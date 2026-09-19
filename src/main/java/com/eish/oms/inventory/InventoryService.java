package com.eish.oms.inventory;

import java.util.List;

import org.springframework.stereotype.Service;

import com.eish.oms.catalog.ProductRepository;
import com.eish.oms.common.NotFoundException;

/**
 * Warehouse and stock management for admins.
 */
@Service
public class InventoryService {

    private final WarehouseRepository warehouses;
    private final InventoryRepository inventory;
    private final ProductRepository products;

    public InventoryService(WarehouseRepository warehouses, InventoryRepository inventory, ProductRepository products) {
        this.warehouses = warehouses;
        this.inventory = inventory;
        this.products = products;
    }

    public Warehouse createWarehouse(CreateWarehouseRequest request) {
        return warehouses.insert(request.name());
    }

    public List<Warehouse> listWarehouses() {
        return warehouses.findAll();
    }

    /** Sets the stock of an existing product in an existing warehouse. */
    public Inventory setStock(SetStockRequest request) {
        products.findById(request.productId())
                .orElseThrow(() -> NotFoundException.of("Product", request.productId()));
        warehouses.findById(request.warehouseId())
                .orElseThrow(() -> NotFoundException.of("Warehouse", request.warehouseId()));
        return inventory.upsert(request.productId(), request.warehouseId(), request.quantity());
    }

    /** Lists all stock rows, or only those of one product when {@code productId} is given. */
    public List<Inventory> listStock(Long productId) {
        return productId == null ? inventory.findAll() : inventory.findByProduct(productId);
    }
}
