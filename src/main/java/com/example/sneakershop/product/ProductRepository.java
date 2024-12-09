package com.example.sneakershop.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
  Product findByProductId(Long productId);

  Product deleteByProductId(Long productId);
}
