package com.example.sneakershop.basket;

import com.example.sneakershop.order.Order;
import com.example.sneakershop.order.OrderDTO;
import com.example.sneakershop.order.OrderService;
import com.example.sneakershop.product.Product;
import com.example.sneakershop.user.User;
import com.example.sneakershop.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class BasketService {

    private final BasketRepository basketRepository;
    private final UserRepository userRepository;
    private final OrderService orderService;

    @Autowired
    public BasketService(BasketRepository basketRepository, UserRepository userRepository, OrderService orderService) {
        this.basketRepository = basketRepository;
        this.userRepository = userRepository;
        this.orderService = orderService;
    }

    public Basket getBasketForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getBasket() != null ? user.getBasket() : createBasketForUser(user);
    }

    public Basket createBasket(){
        Basket basket = new Basket();
        return basketRepository.save(basket);
    }

    public Basket createBasketForUser(User user) {
        Basket basket = new Basket();
        basket.setUser(user);
        user.setBasket(basket);
        return basketRepository.save(basket);
    }

    public void addProductToBasket(Long userId, Product product, int amount) {
        Basket basket = getBasketForUser(userId);
        basket.addProduct(product, amount);
        basketRepository.save(basket);
    }

    public void updateProductInBasket(Long userId, Product product, int amount) {
        Basket basket = getBasketForUser(userId);
        basket.updateProductAmount(product, amount);
        basketRepository.save(basket);
    }

    public BigDecimal calculateBasketTotal(Long userId) {
        Basket basket = getBasketForUser(userId);
        return basket.calculateTotal();
    }

    public void removeProductFromBasket(Long userId, Product product) {
        Basket basket = getBasketForUser(userId);
        basket.removeProduct(product);
        basketRepository.save(basket);
    }

    public void clearBasket(Long userId) {
        Basket basket = getBasketForUser(userId);
        basket.clearBasket();
        basketRepository.save(basket);
    }

    public void checkoutBasket(OrderDTO orderDTO ) {
        Basket basket = getBasketForUser(orderDTO.getUserId());
        if(basket.getProductMap().isEmpty()) {
            throw new RuntimeException("Basket is empty");
        }
        Map<Product, Integer> products = basket.getProductMap();
        Order order = orderService.createOrder(orderDTO);
        // Assume `OrderService` has a method to create or add products to an order
        orderService.addProductsToOrder(orderDTO.getUserId(),products);

        // Clear the basket after checkout
        basket.clearBasket();
        basketRepository.save(basket);
    }
}
