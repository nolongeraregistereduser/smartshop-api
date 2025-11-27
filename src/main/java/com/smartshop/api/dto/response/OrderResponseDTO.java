package com.smartshop.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class OrderResponseDTO {

    private Long id;
    private Long clientId;
    private String clientName;
    private List<OrderItemResponseDTO> items;
    private Double sousTotal;
    private Double montantRemise;
    private Double montantHT;
    private Double montantTVA;
    private Double montantTTC;
    private Double montantRestant;
    private String CodePromo;
    private boolean promoApplied;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
