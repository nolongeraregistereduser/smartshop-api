package com.smartshop.api.dto.response;

import java.time.LocalDateTime;
import java.util.List;

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
