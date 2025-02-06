package com.example.sneakershop.product;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.example.sneakershop.s3.S3Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/v1/products")
public class ProductController {

  private final IProductService productService;
  private final S3Service s3Service;

  public ProductController(IProductService productService,S3Service s3Service) {
    this.productService = productService;
    this.s3Service = s3Service;
  }



  // 🔹 Upload a file to S3
  @PostMapping("/image-upload")
  public ResponseEntity<List<String>> uploadFiles(@RequestParam("files") MultipartFile[] files, @RequestParam("productId") Long productId) {
    try {
      Product product = productService.getProductById(productId);

      List<String> fileUrls = new ArrayList<>();
      for (MultipartFile file : files) {
        String fileUrl = s3Service.uploadFile(file.getOriginalFilename(), file.getBytes());
        fileUrls.add(fileUrl);
        product.getImageUrls().add(fileUrl);
      }

      productService.updateProduct(productId,product);
      return ResponseEntity.ok(fileUrls);
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
    }
  }


  @GetMapping("/{id}")
  public ResponseEntity<Product> getProductById(@PathVariable("id") Long id) {
    Product product = productService.getProductById(id);
    return ResponseEntity.ok(product);
  }

  @GetMapping("/all")
  public ResponseEntity<List<Product>> getAllProducts() {
    List<Product> products = productService.getAllProducts();
    return ResponseEntity.ok(products);
  }

  @PostMapping
  public ResponseEntity<Product> createProduct(@RequestBody Product product) {
    Product createdProduct = productService.createProduct(product);
    return ResponseEntity.ok(createdProduct);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Product> updateProduct(
      @PathVariable("id") Long id, @RequestBody Product product) {
    Product updatedProduct = productService.updateProduct(id, product);
    return ResponseEntity.ok(updatedProduct);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteProduct(@PathVariable("id") Long id) {
    productService.deleteProduct(id);
    return ResponseEntity.noContent().build(); // HTTP 204 for successful deletion
  }
}
