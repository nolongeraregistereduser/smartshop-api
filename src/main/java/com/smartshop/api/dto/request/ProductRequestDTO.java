package com.smartshop.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ProductRequestDTO {

    @NotBlank(message = "Le nom du produit est obligatoire")
    private String nom;

    @NotBlank(message = "La description du produit est obligatoire")
    private String description;

    @Positive(message = "Le prix unitaire doit être un nombre positif")
    @NotBlank(message = "Le prix unitaire du produit est obligatoire")
    private Double prixUnitaire;


    @Positive
    @NotBlank(message = "Le stock du produit est obligatoire")
    private Integer stock;


}
