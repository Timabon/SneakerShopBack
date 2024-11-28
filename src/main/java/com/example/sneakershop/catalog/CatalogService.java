package com.example.sneakershop.catalog;

import com.example.sneakershop.exception.ResourceNotFoundException;
import com.example.sneakershop.product.Product;
import com.example.sneakershop.product.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CatalogService {

    ProductRepository productRepository;
    CatalogRepository catalogRepository;

    public CatalogService(ProductRepository productRepository, CatalogRepository catalogRepository) {
        this.productRepository = productRepository;
        this.catalogRepository = catalogRepository;
    }

    public Catalog createCatalog(String catalogName) {
        Catalog catalog = new Catalog();
        catalog.setCatalogName(catalogName);
        return catalogRepository.save(catalog);
    }

    public List<Catalog> getAllCatalogs() {
        return catalogRepository.findAll();
    }

    public ResponseEntity<Catalog> getCatalog(String catalogName) {
        return ResponseEntity.ok(catalogRepository.findByCatalogName(catalogName));
    }

    public ResponseEntity<Catalog> addProductsToCatalog(Long catalogId, List<Product> products) {
        Optional<Catalog> optionalCatalog = this.catalogRepository.findById(catalogId);
        if(optionalCatalog.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        Catalog catalog = optionalCatalog.get();
        catalog.addProducts(products);
        Catalog savedCatalog = this.catalogRepository.save(catalog);
        return ResponseEntity.ok(savedCatalog);
    }

    public ResponseEntity<Catalog> removeProductsFromCatalog(Long catalogId, List<Product> products) {
        Optional<Catalog> optionalCatalog = this.catalogRepository.findById(catalogId);
        if(optionalCatalog.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        Catalog catalog = optionalCatalog.get();
        catalog.removeProducts(products);
        Catalog savedCatalog = this.catalogRepository.save(catalog);
        return ResponseEntity.ok(savedCatalog);
    }

    public ResponseEntity<Catalog> removeProductFromCatalog(Long catalogId, Product product) {
        Optional<Catalog> optionalCatalog = this.catalogRepository.findById(catalogId);
        if(optionalCatalog.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        Catalog catalog = optionalCatalog.get();
        catalog.getProducts().remove(product);
        Catalog savedCatalog = this.catalogRepository.save(catalog);
        return ResponseEntity.ok(savedCatalog);
    }

    public ResponseEntity<List<Product>> getAllProductsFromCatalog(Long catalogId) {
        Optional<Catalog> optionalCatalog = this.catalogRepository.findById(catalogId);
        if(optionalCatalog.isEmpty()){
            throw new ResourceNotFoundException("Catalog not found");
        }

        Catalog catalog = optionalCatalog.get();
        return ResponseEntity.ok(catalog.getProducts());
    }


    public ResponseEntity<Catalog> addProductToCatalog(long id, Product product) {
        Optional<Catalog> optionalCatalog = this.catalogRepository.findById(id);
        if(optionalCatalog.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        Catalog catalog = optionalCatalog.get();
        catalog.addProduct(product);
        Catalog savedCatalog = this.catalogRepository.save(catalog);
        return ResponseEntity.ok(savedCatalog);
    }
}
