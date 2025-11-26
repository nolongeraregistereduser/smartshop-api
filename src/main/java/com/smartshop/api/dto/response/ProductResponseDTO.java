package com.smartshop.api.dto.response;

import java.time.LocalDateTime;

public class ProductResponseDTO {

    private Long id;
    private String nom;
    private String description;
    private Double prixUnitaire;
    private Integer stock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
