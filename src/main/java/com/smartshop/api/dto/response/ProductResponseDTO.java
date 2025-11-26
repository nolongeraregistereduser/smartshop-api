package com.smartshop.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {

    private Long id;
    private String nom;
    private String description;
    private Double prixUnitaire;
    private Integer stock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
