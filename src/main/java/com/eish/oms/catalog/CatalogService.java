package com.eish.oms.catalog;

import java.util.List;

import org.springframework.stereotype.Service;

import com.eish.oms.common.NotFoundException;

/**
 * Catalog use cases: admins create categories and products, everyone browses them.
 */
@Service
public class CatalogService {

    private final CategoryRepository categories;
    private final ProductRepository products;

    public CatalogService(CategoryRepository categories, ProductRepository products) {
        this.categories = categories;
        this.products = products;
    }

    public Category createCategory(CreateCategoryRequest request) {
        return categories.insert(request.name());
    }

    public List<Category> listCategories() {
        return categories.findAll();
    }

    /** Creates a product in an existing category. */
    public Product createProduct(CreateProductRequest request) {
        categories.findById(request.categoryId())
                .orElseThrow(() -> NotFoundException.of("Category", request.categoryId()));
        return products.insert(request.sku(), request.name(), request.price(), request.categoryId());
    }

    /** Lists all products, or only those in one category when {@code categoryId} is given. */
    public List<Product> listProducts(Long categoryId) {
        return categoryId == null ? products.findAll() : products.findByCategory(categoryId);
    }

    public Product getProduct(long id) {
        return products.findById(id).orElseThrow(() -> NotFoundException.of("Product", id));
    }
}
