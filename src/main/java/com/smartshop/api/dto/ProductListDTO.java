package com.smartshop.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductListDTO {

    private Long id;
    private String nom;
    private Double prixUnitaire;
    private Integer stock;


}
