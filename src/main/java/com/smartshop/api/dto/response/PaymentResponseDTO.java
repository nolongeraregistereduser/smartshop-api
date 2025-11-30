package com.smartshop.api.dto.response;

import com.smartshop.api.enums.PaymentMethod;
import com.smartshop.api.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {
    private Long id;
    private Integer numeroPaiement;
    private Double montant;
    private PaymentMethod methodePaiement;
    private PaymentStatus statut;
    private LocalDateTime datePaiement;
    private LocalDateTime dateEncaissement;
    private LocalDate dateEcheance;
    private String reference;
    private String numeroCheque;
    private String banque;
    private String notes;
}

