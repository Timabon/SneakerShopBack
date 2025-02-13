package com.example.sneakershop.product;

import com.example.sneakershop.exception.ResourceNotFoundException;
import java.util.List;

import com.example.sneakershop.s3.S3Service;
import org.springframework.stereotype.Service;

@Service
public class ProductService implements IProductService {
  private final ProductRepository productRepository;
  private final S3Service s3Service;

  public ProductService(ProductRepository productRepository, S3Service s3Service) {
    this.productRepository = productRepository;
      this.s3Service = s3Service;
  }

  @Override
  public Product getProductById(Long id) {
    return productRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Product with id " + id + " not found"));
  }

  @Override
  public List<Product> getAllProducts() {
    return productRepository.findAll();
  }

  @Override
  public Product createProduct(Product product) {
    return productRepository.save(product);
  }

  @Override
  public Product updateProduct(Long id, Product product) {
    Product productToUpdate =
        productRepository
            .findById(id)
            .orElseThrow(
                () -> new ResourceNotFoundException("Product with id " + id + " not found"));

    productToUpdate.setProductName(product.getProductName());
    productToUpdate.setProductDescription(product.getProductDescription());
    productToUpdate.setProductPrice(product.getProductPrice());

    return productRepository.save(productToUpdate);
  }

  @Override
  public void deleteProduct(Long id) {
    Product productToDelete =
        productRepository
            .findById(id)
            .orElseThrow(
                () -> new ResourceNotFoundException("Product with id " + id + " not found"));

    productRepository.delete(productToDelete);
  }
}
