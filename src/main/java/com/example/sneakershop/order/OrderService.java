package com.example.sneakershop.order;

import com.example.sneakershop.exception.ResourceNotFoundException;
import com.example.sneakershop.product.Product;
import com.example.sneakershop.user.User;
import com.example.sneakershop.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    public OrderService(OrderRepository orderRepository,UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }
    //when user creates order it has to add all the products from basket instantly
    public Order createOrder(OrderDTO orderDTO) {
        Optional<User> user = userRepository.findById(orderDTO.getUserId());
        Order order = new Order();
        order.setUser(user.get());
        order.setOrderDescription(orderDTO.getOrderDescription());

        return orderRepository.save(order);
    }

    public Order getOrder(Long orderId) {
        if(orderRepository.findById(orderId).isEmpty()) {
            throw new ResourceNotFoundException("Order with id " + orderId + " not found");
        }
        return orderRepository.findByOrderId(orderId);
    }

    public List<Order> getAllOrdersOfUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public Order updateOrder(Long orderId, OrderDTO orderDTO) {
        if(orderRepository.findByOrderId(orderId) == null) {
            throw new ResourceNotFoundException("Order with id " + orderId + " not found");
        }
        Order updatedOrder = orderRepository.findByOrderId(orderId);
        updatedOrder.setOrderDescription(orderDTO.getOrderDescription());
        return orderRepository.save(updatedOrder);
    }

    public ResponseEntity<Order> deleteOrder(Long id){
        if(orderRepository.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Order with id " + id + " not found");
        }
        orderRepository.deleteById(id);
        return ResponseEntity.notFound().build();
    }

    public ResponseEntity<Order> addProductToOrder(Long id, Product product){
        if(orderRepository.findByOrderId(id) == null) {
            throw new ResourceNotFoundException("Order with id " + id + " not found");
        }
        Order order = orderRepository.findByOrderId(id);
        order.addProduct(product);
        orderRepository.save(order);
        return ResponseEntity.ok().build();
    }

    public void addProductsToOrder(Long id,Map<Product, Integer> products) {
        if(orderRepository.findByOrderId(id) == null) {
            throw new ResourceNotFoundException("Order with id " + id + " not found");
        }
        Order order = orderRepository.findByOrderId(id);
        order.addProducts(products);
        orderRepository.save(order);

    }
}
