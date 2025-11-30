package com.smartshop.api.dto.request;

import com.smartshop.api.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class PaymentRequestDTO {

    private String orderId;
    private Double montant;
    private PaymentMethod paymentMethod;
    private String reference;
    private String numeroCheque;
    private String banque;
    private String dateEcheance;
    private String notes;
}
