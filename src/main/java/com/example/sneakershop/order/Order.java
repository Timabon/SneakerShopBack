package com.example.sneakershop.order;

import com.example.sneakershop.product.Product;
import com.example.sneakershop.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Temporal(TemporalType.DATE)
    @Column(name = "date")
    private Date orderDate;

    @Column(name = "description")
    private String orderDescription;

    @ElementCollection
    @MapKeyJoinColumn(name = "product_id")
    @Column(name = "product_amount")
    @CollectionTable(name = "products_table", joinColumns = @JoinColumn(name = "order_id"))
    Map<Product, Integer> productMap = new HashMap<>();


    public void addProduct(Product product) {
        this.productMap.merge(product, 1, Integer::sum);
    }


}
