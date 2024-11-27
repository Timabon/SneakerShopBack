package com.example.sneakershop.product;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private long productId;
    @Column (name = "product_name")
    private String productName;
    @Column (name = "product_description")
    private String productDescription;
    @Column (name = "product_price")
    private BigDecimal productPrice;

    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", productName='" + productName + '\'' +
                ", productDetails='" + productDescription + '\'' +
                ", productPrice=" + productPrice +
                '}';
    }


}
