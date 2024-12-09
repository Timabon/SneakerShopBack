package com.example.sneakershop.basket;

import com.example.sneakershop.order.OrderDTO;
import com.example.sneakershop.product.Product;
import com.example.sneakershop.user.User;
import java.math.BigDecimal;

public interface IBasketService {
  Basket getBasketForUser(Long userId);

  Basket createBasket();

  Basket createBasketForUser(User user);

  void addProductToBasket(Long userId, Product product, int amount);

  void updateProductInBasket(Long userId, Product product, int amount);

  BigDecimal calculateBasketTotal(Long userId);

  void removeProductFromBasket(Long userId, Product product);

  void clearBasket(Long userId);

  void checkoutBasket(OrderDTO orderDTO);
}
