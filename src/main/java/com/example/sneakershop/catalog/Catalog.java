package com.example.sneakershop.catalog;

import com.example.sneakershop.product.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
    @NotBlank(message = "Catalog name cannot be empty")
    @Column
    private String catalogName;

    @ManyToMany //tells db it can have ManytoMany relation -> 1 catalog can have a lot of Products, and 1 Product can be in a lot of Catalogs
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

public void removeProducts(List<Product> productsToRemove) {
    if (this.products != null) {
        List<Long> productIdsToRemove = productsToRemove.stream()
                .map(Product::getProductId)
                .toList(); //iterate through products, gets productIds and put it into new lsit
        this.products.removeIf(product -> productIdsToRemove.contains(product.getProductId()));
        //if any productId matches productId in products, delete this product
    }
}
}
