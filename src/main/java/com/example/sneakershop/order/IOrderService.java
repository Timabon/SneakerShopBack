package com.example.sneakershop.order;

import com.example.sneakershop.product.Product;

import java.util.List;
import java.util.Map;

public interface IOrderService {
    //when user creates order it has to add all the products from basket instantly
    Order createOrder(OrderDTO orderDTO);

    Order getOrder(Long userId, Long orderId);

    List<Order> getAllOrdersOfUser(Long userId);

    //TODO Probably delete it
    Order updateOrder(Long orderId, OrderDTO orderDTO);

    void deleteOrder(Long id);

    void addProductsToOrder(Long id, Map<Product, Integer> products);
}
