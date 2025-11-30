package com.smartshop.api.service.impl;

import com.smartshop.api.dto.request.PaymentRequestDTO;
import com.smartshop.api.dto.response.PaymentResponseDTO;
import com.smartshop.api.entity.Order;
import com.smartshop.api.entity.Payment;
import com.smartshop.api.enums.OrderStatus;
import com.smartshop.api.enums.PaymentMethod;
import com.smartshop.api.enums.PaymentStatus;
import com.smartshop.api.exception.BusinessRuleViolationException;
import com.smartshop.api.repository.OrderRepository;
import com.smartshop.api.repository.PaymentRepository;
import com.smartshop.api.service.Impl.PaymentServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private OrderRepository orderRepository;

    private PaymentServiceImpl paymentService;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        paymentService = new PaymentServiceImpl(paymentRepository, orderRepository);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) mocks.close();
    }

    @Test
    void createPayment_cash_encaisse_updatesOrderRemaining() {
        Long orderId = 1L;

        Order order = Order.builder()
                .id(orderId)
                .totalTTC(2000.0)
                .montantRestant(2000.0)
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(orderId)).thenReturn(new ArrayList<>());

        // first call returns 0.0 (no paid yet), second call after save will return 1500.0
        when(paymentRepository.sumTotalPaidByOrderId(orderId)).thenReturn(0.0, 1500.0);

        PaymentRequestDTO req = PaymentRequestDTO.builder()
                .montant(1500.0)
                .paymentMethod(PaymentMethod.ESPECES)
                .reference("RECU-001")
                .notes("cash")
                .build();

        // simulate save returning the payment with id
        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        when(paymentRepository.save(captor.capture())).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            p.setId(100L);
            return p;
        });

        // simulate orderRepository.save capturing updated order
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponseDTO resp = paymentService.createPayment(orderId, req);

        assertNotNull(resp);
        assertEquals(PaymentStatus.ENCAISSE, resp.getStatut());
        assertEquals(100L, resp.getId());

        // verify orderRepository.save called and montantRestant updated to 500.0
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, atLeastOnce()).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertEquals(500.0, savedOrder.getMontantRestant());
    }

    @Test
    void createPayment_cash_over_limit_throws() {
        Long orderId = 2L;
        Order order = Order.builder()
                .id(orderId)
                .totalTTC(30000.0)
                .montantRestant(30000.0)
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(paymentRepository.sumTotalPaidByOrderId(orderId)).thenReturn(0.0);
        when(paymentRepository.findByOrderId(orderId)).thenReturn(new ArrayList<>());

        PaymentRequestDTO req = PaymentRequestDTO.builder()
                .montant(30001.0)
                .paymentMethod(PaymentMethod.ESPECES)
                .reference("RECU-OVER")
                .build();

        assertThrows(BusinessRuleViolationException.class, () -> paymentService.createPayment(orderId, req));
    }

    @Test
    void markPaymentAsEncaisse_updatesOrderRemaining() {
        Long orderId = 3L;
        Long paymentId = 10L;

        Order order = Order.builder()
                .id(orderId)
                .totalTTC(2000.0)
                .montantRestant(1000.0)
                .status(OrderStatus.PENDING)
                .build();

        Payment p = Payment.builder()
                .id(paymentId)
                .numeroPaiement(1)
                .montant(1000.0)
                .statut(com.smartshop.api.enums.PaymentStatus.EN_ATTENTE)
                .order(order)
                .datePaiement(LocalDateTime.now())
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(p));
        when(paymentRepository.sumTotalPaidByOrderId(orderId)).thenReturn(0.0).thenReturn(1000.0);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponseDTO resp = paymentService.markPaymentAsEncaisse(orderId, paymentId);

        assertNotNull(resp);
        assertEquals(com.smartshop.api.enums.PaymentStatus.ENCAISSE, resp.getStatut());
        // verify order.save called and montantRestant updated to 1000.0 (was 1000 -> after encaisse becomes 0)
        verify(orderRepository).save(any(Order.class));
    }
}
