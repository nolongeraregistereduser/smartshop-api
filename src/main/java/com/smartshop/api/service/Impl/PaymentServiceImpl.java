package com.smartshop.api.service.Impl;

import com.smartshop.api.dto.request.PaymentRequestDTO;
import com.smartshop.api.dto.response.PaymentResponseDTO;
import com.smartshop.api.entity.Order;
import com.smartshop.api.entity.Payment;
import com.smartshop.api.enums.PaymentMethod;
import com.smartshop.api.enums.PaymentStatus;
import com.smartshop.api.exception.BusinessRuleViolationException;
import com.smartshop.api.exception.ResourceNotFoundException;
import com.smartshop.api.repository.OrderRepository;
import com.smartshop.api.repository.PaymentRepository;
import com.smartshop.api.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    @Override
    public PaymentResponseDTO createPayment(Long orderId, PaymentRequestDTO requestDTO) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (order.getStatus().isFinal()) {
            throw new BusinessRuleViolationException("Cannot add payment to a final order with status: " + order.getStatus());
        }

        double paidSoFar = paymentRepository.sumTotalPaidByOrderId(orderId) != null ? paymentRepository.sumTotalPaidByOrderId(orderId) : 0.0;
        double remaining = round2(order.getTotalTTC() - paidSoFar);

        if (requestDTO.getMontant() == null || requestDTO.getMontant() <= 0) {
            throw new BusinessRuleViolationException("Montant du paiement invalide");
        }

        if (requestDTO.getMontant() > remaining + 0.001) {
            throw new BusinessRuleViolationException("Le montant du paiement dépasse le montant restant: " + remaining);
        }

        PaymentStatus statut;

        // Validate method specific rules
        PaymentMethod method = requestDTO.getPaymentMethod();
        if (method == null) {
            throw new BusinessRuleViolationException("Methode de paiement requise");
        }

        switch (method) {
            case ESPECES:
                // legal limit
                if (requestDTO.getMontant() > 20000.0) {
                    throw new BusinessRuleViolationException("Paiement en espèces dépasse la limite légale de 20,000 DH");
                }
                statut = PaymentStatus.ENCAISSE;
                break;
            case CHEQUE:
                if (requestDTO.getNumeroCheque() == null || requestDTO.getBanque() == null || requestDTO.getDateEcheance() == null) {
                    throw new BusinessRuleViolationException("Les informations du chèque sont requises (numeroCheque, banque, dateEcheance)");
                }
                LocalDate echeance;
                try {
                    echeance = LocalDate.parse(requestDTO.getDateEcheance());
                } catch (Exception e) {
                    throw new BusinessRuleViolationException("Format dateEcheance invalide. Format attendu: yyyy-MM-dd");
                }
                if (echeance.isAfter(LocalDate.now())) {
                    statut = PaymentStatus.EN_ATTENTE;
                } else {
                    statut = PaymentStatus.ENCAISSE;
                }
                break;
            case VIREMENT:
                if (requestDTO.getReference() == null || requestDTO.getBanque() == null) {
                    throw new BusinessRuleViolationException("Les informations du virement sont requises (reference, banque)");
                }
                // For simplicity, mark virement as ENCAISSE
                statut = PaymentStatus.ENCAISSE;
                break;
            default:
                throw new BusinessRuleViolationException("Methode de paiement inconnue");
        }

        int numeroPaiement = paymentRepository.findByOrderId(orderId).size() + 1;

        Payment payment = Payment.builder()
                .order(order)
                .numeroPaiement(numeroPaiement)
                .montant(requestDTO.getMontant())
                .methodePaiement(method)
                .statut(statut)
                .datePaiement(LocalDateTime.now())
                .reference(requestDTO.getReference())
                .numeroCheque(requestDTO.getNumeroCheque())
                .banque(requestDTO.getBanque())
                .notes(requestDTO.getNotes())
                .build();

        if (statut == PaymentStatus.ENCAISSE) {
            payment.setDateEncaissement(LocalDateTime.now());
        }

        Payment saved = paymentRepository.save(payment);

        // If encaisse, update order.montantRestant
        if (saved.getStatut() == PaymentStatus.ENCAISSE) {
            double newPaid = paymentRepository.sumTotalPaidByOrderId(orderId) != null ? paymentRepository.sumTotalPaidByOrderId(orderId) : 0.0;
            double newRemaining = round2(order.getTotalTTC() - newPaid);
            order.setMontantRestant(newRemaining);
            orderRepository.save(order);
        }

        return convertToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getPaymentsByOrderId(Long orderId) {
        orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        return paymentRepository.findByOrderId(orderId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentResponseDTO markPaymentAsEncaisse(Long orderId, Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));

        if (!payment.getOrder().getId().equals(orderId)) {
            throw new BusinessRuleViolationException("Payment does not belong to the provided order");
        }

        if (payment.getStatut() == PaymentStatus.ENCAISSE) {
            throw new BusinessRuleViolationException("Payment is already encaisse");
        }

        payment.setStatut(PaymentStatus.ENCAISSE);
        payment.setDateEncaissement(LocalDateTime.now());
        Payment saved = paymentRepository.save(payment);

        // Update order remaining
        double newPaid = paymentRepository.sumTotalPaidByOrderId(orderId) != null ? paymentRepository.sumTotalPaidByOrderId(orderId) : 0.0;
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        double newRemaining = round2(order.getTotalTTC() - newPaid);
        order.setMontantRestant(newRemaining);
        orderRepository.save(order);

        return convertToDTO(saved);
    }

    @Override
    public PaymentResponseDTO rejectPayment(Long orderId, Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));

        if (!payment.getOrder().getId().equals(orderId)) {
            throw new BusinessRuleViolationException("Payment does not belong to the provided order");
        }

        if (payment.getStatut() == PaymentStatus.REJETE) {
            throw new BusinessRuleViolationException("Payment is already rejected");
        }

        boolean wasEncaisse = payment.getDateEncaissement() != null && payment.getStatut() == PaymentStatus.ENCAISSE;

        payment.setStatut(PaymentStatus.REJETE);
        Payment saved = paymentRepository.save(payment);

        // If payment was ENCAISSE before, we need to update order remaining
        if (wasEncaisse) {
            double newPaid = paymentRepository.sumTotalPaidByOrderId(orderId) != null ? paymentRepository.sumTotalPaidByOrderId(orderId) : 0.0;
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
            double newRemaining = round2(order.getTotalTTC() - newPaid);
            order.setMontantRestant(newRemaining);
            orderRepository.save(order);
        }

        return convertToDTO(saved);
    }

    private PaymentResponseDTO convertToDTO(Payment p) {
        return PaymentResponseDTO.builder()
                .id(p.getId())
                .numeroPaiement(p.getNumeroPaiement())
                .montant(p.getMontant())
                .methodePaiement(p.getMethodePaiement())
                .statut(p.getStatut())
                .datePaiement(p.getDatePaiement())
                .dateEncaissement(p.getDateEncaissement())
                .dateEcheance(p.getDateEcheance())
                .reference(p.getReference())
                .numeroCheque(p.getNumeroCheque())
                .banque(p.getBanque())
                .notes(p.getNotes())
                .build();
    }
}
