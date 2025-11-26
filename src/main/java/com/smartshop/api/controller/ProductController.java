package com.smartshop.api.controller;

import com.smartshop.api.dto.ProductListDTO;
import com.smartshop.api.dto.request.ProductRequestDTO;
import com.smartshop.api.dto.response.ProductResponseDTO;
import com.smartshop.api.service.ProductService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@AllArgsConstructor
public class ProductController {

    private final ProductService productService;


    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody ProductRequestDTO productRequestDTO) {

        ProductResponseDTO responseDTO = productService.createProduct(productRequestDTO);

        return ResponseEntity.ok(responseDTO);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(Long id) {

        ProductResponseDTO responseDTO = productService.getProductById(id);

        return ResponseEntity.ok(responseDTO);
    }


    @GetMapping
    public ResponseEntity<Page<ProductListDTO>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nom") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        // Create Pageable object
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<ProductListDTO> response = productService.getAllProducts(pageable);
        return ResponseEntity.ok(response);
    }


    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDTO productRequest) {

        ProductResponseDTO response = productService.updateProduct(id, productRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }




}
