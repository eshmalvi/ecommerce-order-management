package com.eish.oms.inventory;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.eish.oms.config.ApiPaths;

import jakarta.validation.Valid;

/**
 * Warehouse and stock management for admins. Access is restricted to the ADMIN role by the security path rules.
 */
@RestController
public class AdminInventoryController {

    private final InventoryService inventoryService;

    public AdminInventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping(ApiPaths.ADMIN_WAREHOUSES)
    @ResponseStatus(HttpStatus.CREATED)
    public Warehouse createWarehouse(@Valid @RequestBody CreateWarehouseRequest request) {
        return inventoryService.createWarehouse(request);
    }

    @GetMapping(ApiPaths.ADMIN_WAREHOUSES)
    public List<Warehouse> listWarehouses() {
        return inventoryService.listWarehouses();
    }

    /** Idempotent: sets the absolute stock level, creating the row on first use. */
    @PutMapping(ApiPaths.ADMIN_INVENTORY)
    public Inventory setStock(@Valid @RequestBody SetStockRequest request) {
        return inventoryService.setStock(request);
    }

    @GetMapping(ApiPaths.ADMIN_INVENTORY)
    public List<Inventory> listStock(@RequestParam(required = false) Long productId) {
        return inventoryService.listStock(productId);
    }
}
