package com.example.sneakershop.catalog;

import com.example.sneakershop.exception.ResourceNotFoundException;
import com.example.sneakershop.product.Product;
import com.example.sneakershop.product.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class CatalogService implements ICatalogService {

    private final ProductRepository productRepository;
    private final CatalogRepository catalogRepository;

    public CatalogService(ProductRepository productRepository, CatalogRepository catalogRepository) {
        this.productRepository = productRepository;
        this.catalogRepository = catalogRepository;
    }

    @Override
    public Catalog createCatalog(String catalogName) {
        Catalog catalog = new Catalog();
        catalog.setCatalogName(catalogName);
        return catalogRepository.save(catalog);
    }

    @Override
    public List<Catalog> getAllCatalogs() {
        return catalogRepository.findAll();
    }

    @Override
    public Catalog getCatalogById(Long catalogId) {
        return catalogRepository.findById(catalogId).orElseThrow(() -> new ResourceNotFoundException("Catalog not found"));
    }

    @Override
    public Catalog addProductsToCatalog(Long catalogId, List<Product> products) {
        Catalog catalog = catalogRepository.findById(catalogId)
                .orElseThrow(() -> new ResourceNotFoundException("Catalog not found with id: " + catalogId));
        catalog.addProducts(products);
        return catalogRepository.save(catalog);
    }

    @Override
    public void removeProductsFromCatalog(Long catalogId, List<Product> products) {
        Catalog catalog = catalogRepository.findById(catalogId)
                .orElseThrow(() -> new ResourceNotFoundException("Catalog not found with id: " + catalogId));
        catalog.removeProducts(products);
        catalogRepository.save(catalog);
    }

    @Override
    public void removeProductFromCatalog(Long catalogId, Long productId) {
        Catalog catalog = catalogRepository.findById(catalogId)
                .orElseThrow(() -> new ResourceNotFoundException("Catalog not found with id: " + catalogId));
        catalog.removeProduct(productId);
        catalogRepository.save(catalog);
    }

    @Override
    public List<Product> getAllProductsFromCatalog(Long catalogId) {
        Catalog catalog = catalogRepository.findById(catalogId)
                .orElseThrow(() -> new ResourceNotFoundException("Catalog not found with id: " + catalogId));
        return catalog.getProducts();
    }

    @Override
    public Catalog addProductToCatalog(Long catalogId, Product product) {
        Catalog catalog = catalogRepository.findById(catalogId)
                .orElseThrow(() -> new ResourceNotFoundException("Catalog not found with id: " + catalogId));
        catalog.addProduct(product);
        return catalogRepository.save(catalog);
    }
}