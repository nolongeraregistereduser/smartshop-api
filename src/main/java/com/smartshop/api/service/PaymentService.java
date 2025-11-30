package com.smartshop.api.service;

import com.smartshop.api.dto.request.PaymentRequestDTO;
import com.smartshop.api.dto.response.PaymentResponseDTO;

import java.util.List;

public interface PaymentService {

    PaymentResponseDTO createPayment(Long orderId, PaymentRequestDTO requestDTO);

    List<PaymentResponseDTO> getPaymentsByOrderId(Long orderId);

    PaymentResponseDTO markPaymentAsEncaisse(Long orderId, Long paymentId);

    PaymentResponseDTO rejectPayment(Long orderId, Long paymentId);
}
