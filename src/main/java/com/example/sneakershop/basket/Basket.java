package com.example.sneakershop.basket;

import com.example.sneakershop.product.Product;
import com.example.sneakershop.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Entity
@Getter
@Setter
@Table(name = "basket")
public class Basket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ElementCollection
    private Map<Product, Integer> productMap;
    @OneToOne(mappedBy = "basket", cascade = CascadeType.ALL)
    @JsonIgnore
    private User user;

    public Basket() {
        this.productMap = new HashMap<>();
    }

    public void addProduct(Product product, int amount) {
        this.productMap.put(product, amount);
    }

    public void updateProductAmount(Product product, int amount) {
        if(amount <= 0){
            this.productMap.remove(product);
        }else{
            this.productMap.put(product, amount);
        }
    }

    public BigDecimal calculateTotal() {

        BigDecimal totalPrice = productMap.entrySet().stream()
                .map(entry -> entry.getKey().getProductPrice().multiply(BigDecimal.valueOf(entry.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalPrice;
    }



    public void removeProduct(Product product) {
        this.productMap.remove(product);
    }

    public void clearBasket(){
        productMap.clear();
    }

    //TODO Create a new function, for creating order with products from basket

}
