package com.smartshop.api.repository;

import com.smartshop.api.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find products by name (for search functionality)
    List<Product> findByNomContainingIgnoreCase(String nom);

    // Find products with stock greater than a certain amount
    List<Product> findByStockGreaterThan(Integer stock);

    // Find products by stock availability
    List<Product> findByStockGreaterThanEqual(Integer minStock);
}
