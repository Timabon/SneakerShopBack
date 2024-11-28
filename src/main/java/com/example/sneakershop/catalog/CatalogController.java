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
    public List<Catalog> getAllCatalogs() {
        return catalogService.getAllCatalogs();
    }

    @GetMapping("{id}")
    public ResponseEntity<List<Product>> getAllProductsFromCatalog(@PathVariable Long id) {
        return catalogService.getAllProductsFromCatalog(id);
    }

    @PostMapping
    public ResponseEntity<Catalog> createCatalog(@RequestBody String catalogName) {
        return ResponseEntity.ok(catalogService.createCatalog(catalogName));
    }

    @PostMapping("/{id}/addproduct")
    public ResponseEntity<Catalog> addProductToCatalog(@PathVariable Long id, @RequestBody Product product) {
        return catalogService.addProductToCatalog(id, product);
    }

    @PostMapping("{id}/addproducts")
    public ResponseEntity<Catalog> addProductsToCatalog(@PathVariable Long id, @RequestBody List<Product> products) {
        return catalogService.addProductsToCatalog(id, products);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Catalog> removeProductFromCatalog(@PathVariable Long id, @RequestBody Product product) {
        return catalogService.removeProductFromCatalog(id, product);
    }
}
