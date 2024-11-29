package com.example.sneakershop.catalog;

import com.example.sneakershop.product.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.ProtectionDomain;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/v1/catalogs")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public ResponseEntity<List<Catalog>> getAllCatalogs() {
        List<Catalog> catalogs = catalogService.getAllCatalogs();
        return ResponseEntity.ok(catalogs);
    }

    @GetMapping("{id}")
    public ResponseEntity<Catalog> getCatalogById(@PathVariable Long id) {
        Catalog catalog = catalogService.getCatalogById(id);
        return ResponseEntity.ok(catalog);
    }

    @GetMapping("products/{id}")
    public ResponseEntity<List<Product>> getAllProductsFromCatalog(@PathVariable Long id) {
        List<Product> products = catalogService.getAllProductsFromCatalog(id);
        return ResponseEntity.ok(products);
    }

    @PostMapping
    public ResponseEntity<Catalog> createCatalog(@RequestBody String catalogName) {
        Catalog createdCatalog = catalogService.createCatalog(catalogName);
        return ResponseEntity.ok(createdCatalog);
    }

    @PostMapping("{id}/addproduct")
    public ResponseEntity<Catalog> addProductToCatalog(@PathVariable Long id, @RequestBody Product product) {
        Catalog updatedCatalog = catalogService.addProductToCatalog(id, product);
        return ResponseEntity.ok(updatedCatalog);
    }

    @PostMapping("{id}/addproducts")
    public ResponseEntity<Catalog> addProductsToCatalog(@PathVariable Long id, @RequestBody List<Product> products) {
        Catalog updatedCatalog = catalogService.addProductsToCatalog(id, products);
        return ResponseEntity.ok(updatedCatalog);
    }

    @DeleteMapping("{id}/{productId}")
    public ResponseEntity<Void> removeProductFromCatalog(@PathVariable Long id, @PathVariable Long productId) {
        catalogService.removeProductFromCatalog(id, productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> removeProductsFromCatalog(@PathVariable Long id, @RequestBody List<Product> products) {
        catalogService.removeProductsFromCatalog(id, products);
        return ResponseEntity.noContent().build();
    }
}