package com.smartshop.api.service.Impl;

import com.smartshop.api.dto.ProductListDTO;
import com.smartshop.api.dto.request.ProductRequestDTO;
import com.smartshop.api.dto.response.ProductResponseDTO;
import com.smartshop.api.entity.Product;
import com.smartshop.api.exception.ResourceNotFoundException;
import com.smartshop.api.mapper.ProductMapper;
import com.smartshop.api.repository.ProductRepository;
import com.smartshop.api.service.ProductService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final ProductRepository productRepository;

    @Transactional
    @Override
    public ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO) {
        Product product = productMapper.toEntity(productRequestDTO);
        Product savedProduct = productRepository.save(product);
        return productMapper.toResponseDTO(savedProduct);
    }

    @Transactional
    @Override
    public ProductResponseDTO getProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));


        return productMapper.toResponseDTO(product);
    }



    @Transactional
    @Override
    public Page<ProductListDTO> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(product -> ProductListDTO.builder()
                        .id(product.getId())
                        .nom(product.getNom())
                        .prixUnitaire(product.getPrixUnitaire())
                        .stock(product.getStock())
                        .build());
    }

    @Override
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO productRequestDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        productMapper.updateEntity(product, productRequestDTO);
        Product updatedProduct = productRepository.save(product);
        return productMapper.toResponseDTO(updatedProduct);
    }

    @Transactional
    @Override
    public void deleteProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        productRepository.delete(product);
    }
}
