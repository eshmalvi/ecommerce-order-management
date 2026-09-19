package com.eish.oms.catalog;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eish.oms.config.ApiPaths;

/**
 * Public browsing of the catalog. No authentication required.
 */
@RestController
public class CatalogController {

    private final CatalogService catalog;

    public CatalogController(CatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping(ApiPaths.CATEGORIES)
    public List<Category> listCategories() {
        return catalog.listCategories();
    }

    @GetMapping(ApiPaths.PRODUCTS)
    public List<Product> listProducts(@RequestParam(required = false) Long categoryId) {
        return catalog.listProducts(categoryId);
    }

    @GetMapping(ApiPaths.PRODUCT_BY_ID)
    public Product getProduct(@PathVariable long id) {
        return catalog.getProduct(id);
    }
}
