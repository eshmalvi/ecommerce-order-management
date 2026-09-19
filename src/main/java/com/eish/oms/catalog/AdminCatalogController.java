package com.eish.oms.catalog;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.eish.oms.config.ApiPaths;

import jakarta.validation.Valid;

/**
 * Catalog management for admins. Access is restricted to the ADMIN role by the security path rules.
 */
@RestController
public class AdminCatalogController {

    private final CatalogService catalog;

    public AdminCatalogController(CatalogService catalog) {
        this.catalog = catalog;
    }

    @PostMapping(ApiPaths.ADMIN_CATEGORIES)
    @ResponseStatus(HttpStatus.CREATED)
    public Category createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        return catalog.createCategory(request);
    }

    @PostMapping(ApiPaths.ADMIN_PRODUCTS)
    @ResponseStatus(HttpStatus.CREATED)
    public Product createProduct(@Valid @RequestBody CreateProductRequest request) {
        return catalog.createProduct(request);
    }
}
