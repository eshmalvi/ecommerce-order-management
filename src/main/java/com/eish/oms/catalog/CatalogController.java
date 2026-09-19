package com.eish.oms.catalog;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public browsing of the catalog. No authentication required.
 */
@RestController
@RequestMapping("/api")
public class CatalogController {

    private final CatalogService catalog;

    public CatalogController(CatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping("/categories")
    public List<Category> listCategories() {
        return catalog.listCategories();
    }

    @GetMapping("/products")
    public List<Product> listProducts(@RequestParam(required = false) Long categoryId) {
        return catalog.listProducts(categoryId);
    }

    @GetMapping("/products/{id}")
    public Product getProduct(@PathVariable long id) {
        return catalog.getProduct(id);
    }
}
