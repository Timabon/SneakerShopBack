package com.example.sneakershop.catalog;

import com.example.sneakershop.product.Product;
import java.util.List;

public interface ICatalogService {
  Catalog createCatalog(String catalogName);

  List<Catalog> getAllCatalogs();

  Catalog getCatalogById(Long catalogId);

  Catalog addProductsToCatalog(Long catalogId, List<Product> products);

  void removeProductsFromCatalog(Long catalogId, List<Product> products);

  void removeProductFromCatalog(Long catalogId, Long productId);

  List<Product> getAllProductsFromCatalog(Long catalogId);

  Catalog addProductToCatalog(Long catalogId, Product product);
}
