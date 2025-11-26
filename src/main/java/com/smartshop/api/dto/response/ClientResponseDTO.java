package com.smartshop.api.dto.response;

import com.smartshop.api.enums.CustomerTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponseDTO {

    private Long id;
    private String nom;
    private String email;
    private String telephone;
    private String adresse;
    private CustomerTier tier;
    private Integer totalOrders;
    private Double totalSpent;
    private LocalDateTime firstOrderDate;
    private LocalDateTime lastOrderDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}