package com.example.sneakershop.product;

import com.example.sneakershop.exception.ResourceNotFoundException;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product getProductById(Long id) {
       if (productRepository.findByProductId(id) == null) {
           throw new ResourceNotFoundException("Product with id " + id + " not found");
       }
       return productRepository.findByProductId(id);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product createProduct(Product product) {
       return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product product) {
        if (productRepository.findByProductId(id) == null) {
            throw new ResourceNotFoundException("Product with id " + id + " not found");
        }
        Product productToUpdate = productRepository.findByProductId(id);
        productToUpdate.setProductName(product.getProductName());
        productToUpdate.setProductDescription(product.getProductDescription());
        productToUpdate.setProductPrice(product.getProductPrice());
        return productRepository.save(productToUpdate);
    }

    public void deleteProduct(Long id) {
        if (productRepository.findByProductId(id) == null) {
            throw new ResourceNotFoundException("Product with id " + id + " not found");
        }
        productRepository.delete(productRepository.findByProductId(id));
    }
}
