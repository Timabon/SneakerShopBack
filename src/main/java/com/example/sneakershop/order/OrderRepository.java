package com.example.sneakershop.order;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
  Order findByUserId(Long userId);

  Order findByOrderId(Long orderId);

  List<Order> findAllByUserId(Long userId);
}
