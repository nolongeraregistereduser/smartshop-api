package com.smartshop.api.service;

import com.smartshop.api.dto.ProductListDTO;
import com.smartshop.api.dto.request.ProductRequestDTO;
import com.smartshop.api.dto.response.ProductResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO);

    ProductResponseDTO getProductById(Long id);

    // read all products with pagination
    Page<ProductListDTO> getAllProducts(Pageable pageable);

    ProductResponseDTO updateProduct(Long id, ProductRequestDTO productRequestDTO);

    void deleteProduct(Long id);


}
