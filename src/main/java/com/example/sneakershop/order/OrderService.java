package com.example.sneakershop.order;

import com.example.sneakershop.basket.IBasketService;
import com.example.sneakershop.exception.ResourceNotFoundException;
import com.example.sneakershop.product.Product;
import com.example.sneakershop.user.User;
import com.example.sneakershop.user.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OrderService implements IOrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final IBasketService IBasketService;

    public OrderService(@Lazy OrderRepository orderRepository, @Lazy UserRepository userRepository, @Lazy IBasketService IBasketService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.IBasketService = IBasketService;
    }

    //when user creates order it has to add all the products from basket instantly
    @Override
    public Order createOrder(OrderDTO orderDTO) {
        Optional<User> user = userRepository.findById(orderDTO.getUserId());
        Order order = new Order();
        order.setUser(user.get());
        order.setOrderDescription(orderDTO.getOrderDescription());

        return orderRepository.save(order);
    }

    @Override
    public Order getOrder(Long userId, Long orderId) {
        List<Order> userOrders = getAllOrdersOfUser(userId);
        for (Order order : userOrders) {
            if (order.getOrderId().equals(orderId)) {
                return order;
            }
        }
        throw new ResourceNotFoundException("Order not found");
    }

    @Override
    public List<Order> getAllOrdersOfUser(Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new ResourceNotFoundException("User with id " + userId + " not found");
        }
        return orderRepository.findAllByUserId(userId);
    }

    //TODO Probably delete it
    @Override
    public Order updateOrder(Long orderId, OrderDTO orderDTO) {
        if (orderRepository.findByOrderId(orderId) == null) {
            throw new ResourceNotFoundException("Order with id " + orderId + " not found");
        }
        Order updatedOrder = orderRepository.findByOrderId(orderId);
        updatedOrder.setOrderDescription(orderDTO.getOrderDescription());
        return orderRepository.save(updatedOrder);
    }

    @Override
    public void deleteOrder(Long id) {
        if (orderRepository.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Order with id " + id + " not found");
        }
        orderRepository.deleteById(id);
    }


    @Override
    public void addProductsToOrder(Long id, Map<Product, Integer> products) {
        if (orderRepository.findByOrderId(id) == null) {
            throw new ResourceNotFoundException("Order with id " + id + " not found");
        }
        Order order = orderRepository.findByOrderId(id);
        order.addProducts(products);
        orderRepository.save(order);

    }
}
