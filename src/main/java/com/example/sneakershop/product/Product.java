package com.example.sneakershop.product;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
    @NotBlank(message = "product name cannot be empty")
    @Column (name = "product_name")
    private String productName;
    @Column (name = "product_description")
    private String productDescription;
    @Min(10)
    @NotBlank(message = "product price cannot be empty")
    @Column (name = "product_price")
    private BigDecimal productPrice; //example: so we can set the price 54.75

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
