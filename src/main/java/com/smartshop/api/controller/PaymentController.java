package com.smartshop.api.controller;

import com.smartshop.api.dto.request.PaymentRequestDTO;
import com.smartshop.api.dto.response.PaymentResponseDTO;
import com.smartshop.api.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/api/orders/{orderId}/payments")
    public ResponseEntity<PaymentResponseDTO> createPayment(@PathVariable Long orderId,
                                                            @Valid @RequestBody PaymentRequestDTO request) {
        PaymentResponseDTO p = paymentService.createPayment(orderId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(p);
    }

    @GetMapping("/api/orders/{orderId}/payments")
    public ResponseEntity<List<PaymentResponseDTO>> getPayments(@PathVariable Long orderId) {
        List<PaymentResponseDTO> list = paymentService.getPaymentsByOrderId(orderId);
        return ResponseEntity.ok(list);
    }

    @PutMapping("/api/orders/{orderId}/payments/{paymentId}/encaisse")
    public ResponseEntity<PaymentResponseDTO> encaisse(@PathVariable Long orderId, @PathVariable Long paymentId) {
        PaymentResponseDTO p = paymentService.markPaymentAsEncaisse(orderId, paymentId);
        return ResponseEntity.ok(p);
    }

    @PutMapping("/api/orders/{orderId}/payments/{paymentId}/reject")
    public ResponseEntity<PaymentResponseDTO> reject(@PathVariable Long orderId, @PathVariable Long paymentId) {
        PaymentResponseDTO p = paymentService.rejectPayment(orderId, paymentId);
        return ResponseEntity.ok(p);
    }
}
