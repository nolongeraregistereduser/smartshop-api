package com.smartshop.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {

    private Long id;

    @NotBlank(message = "Le nom du produit est obligatoire")
    private String nom;

    @NotBlank(message = "La description du produit est obligatoire")
    private String description;

    @Positive(message = "Le prix unitaire doit être un nombre positif")
    private Double prixUnitaire;


    @Positive
    private Integer stock;


}
