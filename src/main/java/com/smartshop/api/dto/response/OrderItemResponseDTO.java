package com.smartshop.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponseDTO {

    private Long productId;
    private String nomDeProduit;
    private Integer quantity;
    private Double prixUnitaire;
    private Double prixTotal;
}
