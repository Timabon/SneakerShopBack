package com.example.sneakershop.basket;

import com.example.sneakershop.product.Product;
import com.example.sneakershop.user.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

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
  @MapKeyJoinColumn(name = "product_id") // Join column for the product
  @Column(name = "product_amount") // Column for the quantity
  @CollectionTable(
      name = "basket_product", // Define the name of the join table
      joinColumns = @JoinColumn(name = "basket_id") // Foreign key for Basket
      )
  private Map<Product, Integer> productMap;

  @OneToOne(mappedBy = "basket")
  private User user;

  public Basket() {
    this.productMap = new HashMap<>();
  }

  public void addProduct(Product product, int amount) {
    this.productMap.put(product, amount);
  }

  public void updateProductAmount(Product product, int amount) {
    if (amount <= 0) {
      this.productMap.remove(product);
    } else {
      this.productMap.put(product, amount);
    }
  }

  public BigDecimal calculateTotal() {

    BigDecimal totalPrice =
        productMap.entrySet().stream()
            .map(
                entry ->
                    entry.getKey().getProductPrice().multiply(BigDecimal.valueOf(entry.getValue())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    return totalPrice;
  }

  public void removeProduct(Product product) {
    this.productMap.remove(product);
  }

  public void clearBasket() {
    productMap.clear();
  }
}
