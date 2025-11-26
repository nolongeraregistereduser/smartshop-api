package com.smartshop.api.mapper;


import com.smartshop.api.dto.request.ProductRequestDTO;
import com.smartshop.api.dto.response.ProductResponseDTO;
import com.smartshop.api.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    // requestDTO to entity when creating or updating a product

    public Product toEntity(ProductRequestDTO requestDTO) {
        return Product.builder().nom(requestDTO.getNom())
                .description(requestDTO.getDescription())
                .prixUnitaire(requestDTO.getPrixUnitaire())
                .stock(requestDTO.getStock())
                .build();
    }


    // entity to responseDTO when returning product data to the client

    public ProductResponseDTO toResponseDTO(Product product){
        return ProductResponseDTO.builder()
                .id(product.getId())
                .nom(product.getNom())
                .description(product.getDescription())
                .prixUnitaire(product.getPrixUnitaire())
                .stock(product.getStock())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }



    public void updateEntity(Product product, ProductRequestDTO requestDTO){
        product.setNom(requestDTO.getNom());
        product.setDescription(requestDTO.getDescription());
        product.setPrixUnitaire(requestDTO.getPrixUnitaire());
        product.setStock(requestDTO.getStock());

        // dates are handred by @PreUpdate in the entity no need bro to update them here
    }
}
