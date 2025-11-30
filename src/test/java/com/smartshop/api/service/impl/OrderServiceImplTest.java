package com.smartshop.api.service.impl;

import com.smartshop.api.dto.request.OrderItemRequestDTO;
import com.smartshop.api.dto.request.OrderRequestDTO;
import com.smartshop.api.dto.response.OrderResponseDTO;
import com.smartshop.api.entity.Client;
import com.smartshop.api.entity.Order;
import com.smartshop.api.entity.OrderItem;
import com.smartshop.api.entity.Product;
import com.smartshop.api.enums.CustomerTier;
import com.smartshop.api.enums.OrderStatus;
import com.smartshop.api.exception.ResourceNotFoundException;
import com.smartshop.api.repository.ClientRepository;
import com.smartshop.api.repository.OrderRepository;
import com.smartshop.api.repository.ProductRepository;
import com.smartshop.api.repository.PaymentRepository;
import com.smartshop.api.service.Impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private PaymentRepository paymentRepository;

    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orderService = new OrderServiceImpl(orderRepository, clientRepository, productRepository, paymentRepository);
    }

    @Test
    void createOrder_appliesPromoAndLoyalty() {
        Long clientId = 1L;
        Long productId = 2L;

        Client client = Client.builder()
                .id(clientId)
                .nom("Client Test")
                .tier(CustomerTier.SILVER)
                .build();

        Product product = Product.builder()
                .id(productId)
                .nom("SSD")
                .prixUnitaire(1000.0)
                .stock(10)
                .build();

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        List<OrderItemRequestDTO> items = new ArrayList<>();
        items.add(OrderItemRequestDTO.builder().productId(productId).quantity(1).build());

        OrderRequestDTO req = OrderRequestDTO.builder()
                .clientId(clientId)
                .items(items)
                .codePromo("PROMO-AB12")
                .build();

        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponseDTO resp = orderService.createOrder(req);

        assertNotNull(resp);
        // sousTotal = 1000, silver discount eligible (>=500) => 5% = 50, promo 5% => 50, total remise = 100
        assertEquals(1000.0, resp.getSousTotal());
        assertEquals(100.0, resp.getMontantRemise());
        assertEquals(OrderStatus.PENDING, resp.getStatus());
    }

    @Test
    void confirmOrder_requiresFullPayment() {
        Long orderId = 10L;
        Client client = Client.builder().id(1L).nom("C").totalOrders(0).totalSpent(0.0).build();

        Order order = Order.builder()
                .id(orderId)
                .client(client)
                .totalTTC(2000.0)
                .montantRestant(1000.0)
                .status(OrderStatus.PENDING)
                .orderItems(new ArrayList<OrderItem>())
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(paymentRepository.sumTotalPaidByOrderId(orderId)).thenReturn(2000.0);
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Now confirm should recompute remaining to 0 and allow confirm
        OrderResponseDTO resp = orderService.confirmOrder(orderId);
        assertEquals(OrderStatus.CONFIRMED, resp.getStatus());
    }

    @Test
    void getOrderById_notFound_throws() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById(99L));
    }
}
