package com.example.sneakershop.catalog;

import com.example.sneakershop.product.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Catalog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long catalogId;

    @Column
    private String catalogName;

    @ManyToMany
    List<Product> products;

    public void addProduct(Product product) {
        products.add(product);
    }

    public void removeProduct(Long productId) {
        if (this.products != null) {
            this.products.removeIf(product -> product.getProductId() == productId);
        }
    }

    public void addProducts(List<Product> products) {
        this.products.addAll(products);
    }
//TODO fix this bullshit
    public void removeProducts(List<Product> productsToRemove) {
        if (this.products != null) {
            this.products.removeAll(productsToRemove);
        }
    }
}
