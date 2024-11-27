package com.example.sneakershop.basket;

import com.example.sneakershop.product.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/v1/basket")
public class BasketController {

    private final Basket basket;

    public BasketController(Basket basket) {
        this.basket = basket;
    }

    @GetMapping
    public ResponseEntity<Map<Product, Integer>> getBasket() {
        return ResponseEntity.ok(basket.getProductMap());
    }

    @PostMapping
    public ResponseEntity<String> addProduct(@RequestBody Product product, @RequestParam int amount) {
        basket.addProduct(product, amount);
        return ResponseEntity.ok("Product added successfully");
    }

    @PutMapping
    public ResponseEntity<String> updateProduct(@RequestBody Product product, @RequestParam int amount) {
        basket.updateProductAmount(product, amount);
        return ResponseEntity.ok("Product updated successfully");
    }

    @GetMapping("/total")
    public ResponseEntity<String> calculateTotal() {
        var total = basket.calculateTotal();
        return ResponseEntity.ok("Total price is: " + total);
    }

    @DeleteMapping
    public ResponseEntity<String> removeProduct(@RequestParam Product product) {
        basket.removeProduct(product);

        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<String> clearBasket() {
        basket.clearBasket();
        return ResponseEntity.noContent().build();
    }

}
