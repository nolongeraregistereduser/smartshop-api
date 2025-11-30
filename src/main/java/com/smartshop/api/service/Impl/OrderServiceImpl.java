package com.smartshop.api.service.Impl;

import com.smartshop.api.dto.request.OrderItemRequestDTO;
import com.smartshop.api.dto.request.OrderRequestDTO;
import com.smartshop.api.dto.response.OrderHistoryDTO;
import com.smartshop.api.dto.response.OrderItemResponseDTO;
import com.smartshop.api.dto.response.OrderResponseDTO;
import com.smartshop.api.entity.Client;
import com.smartshop.api.entity.Order;
import com.smartshop.api.entity.OrderItem;
import com.smartshop.api.entity.Product;
import com.smartshop.api.enums.CustomerTier;
import com.smartshop.api.enums.OrderStatus;
import com.smartshop.api.exception.BusinessRuleViolationException;
import com.smartshop.api.exception.InsufficientStockException;
import com.smartshop.api.exception.ResourceNotFoundException;
import com.smartshop.api.repository.ClientRepository;
import com.smartshop.api.repository.OrderRepository;
import com.smartshop.api.repository.ProductRepository;
import com.smartshop.api.repository.PaymentRepository;
import com.smartshop.api.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.round;


@Data
@Service
@RequiredArgsConstructor
@Transactional

public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    @Override
    public OrderResponseDTO createOrder(OrderRequestDTO orderRequestDTO) {

        // validation client exists

        Client client = clientRepository.findById(orderRequestDTO.getClientId()).orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        // validation products exists

        List<OrderItem> orderItems = new ArrayList<>();
        double sousTotal = 0;

        for (OrderItemRequestDTO itemDTO : orderRequestDTO.getItems()) {

            Product product = productRepository.findById(itemDTO.getProductId()).orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            // verify stock and reject order automatically if stock insufficient

            if (product.getStock() < itemDTO.getQuantity()) {
                throw new InsufficientStockException(
                    "Insufficient stock for product: " + product.getNom()
                    + ". Available: " + product.getStock()
                    + ", Requested: " + itemDTO.getQuantity());
            }

            // calculate total and create order items

            double prixUnitaire = product.getPrixUnitaire();
            double totaleLigne = prixUnitaire * itemDTO.getQuantity();
            sousTotal += totaleLigne;

            // create order item

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantite(itemDTO.getQuantity())
                    .prixUnitaire(prixUnitaire)
                    .totalLigne(totaleLigne)
                    .build();

            orderItems.add(orderItem);
        }

        // Round sous-total
        sousTotal = round(sousTotal * 100.0) / 100.0;

        // Calculate loyalty discount
        CustomerTier tier = client.getTier();
        double montantRemise = 0.0;

        if (tier.isEligibleForDiscount(sousTotal)) {
            montantRemise = tier.calculateDiscount(sousTotal);
        }

        // Apply promo code if valid
        boolean promoApplied = false;
        if (orderRequestDTO.getCodePromo() != null && isValidPromoCode(orderRequestDTO.getCodePromo())) {
            montantRemise += sousTotal * 0.05; // Add 5% promo discount
            promoApplied = true;
        }

        montantRemise = round(montantRemise * 100.0) / 100.0;
        double montantHT = round((sousTotal - montantRemise) * 100.0) / 100.0;
        double tauxTVA = 20.0; // TVA Morocco = 20%
        double montantTVA = round(montantHT * (tauxTVA / 100.0) * 100.0) / 100.0;
        double montantTTC = round((montantHT + montantTVA) * 100.0) / 100.0;

        Order order = Order.builder()
                .client(client)
                .sousTotal(sousTotal)
                .montantRemise(montantRemise)
                .montantHT(montantHT)
                .tauxTVA(tauxTVA)
                .montantTVA(montantTVA)
                .totalTTC(montantTTC)
                .montantRestant(montantTTC) // Initially, all amount is remaining
                .codePromo(orderRequestDTO.getCodePromo())
                .promoApplied(promoApplied)
                .status(OrderStatus.PENDING) // Initial status
                .build();

        // Link OrderItems to Order
        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }
        order.setOrderItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        return convertToResponseDTO(savedOrder);
    }

    @Override
    public OrderResponseDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with ID: " + id));

        return convertToResponseDTO(order);
    }

    @Override
    public List<OrderResponseDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAll();

        // returning orders

        return orders.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderHistoryDTO> getClientOrderHistory(Long clientId) {
        clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client not found with ID: " + clientId));

        List<Order> orders = orderRepository.findByClientId(clientId);

        // Convert to OrderHistoryDTO (simplified view)
        return orders.stream()
                .map(order -> OrderHistoryDTO.builder()
                        .id(order.getId())
                        .createdAt(order.getCreatedAt())
                        .montantTTC(order.getTotalTTC())
                        .status(order.getStatus().name())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponseDTO confirmOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with ID: " + id));

        if (!order.getStatus().canBeConfirmed()) {
            throw new BusinessRuleViolationException(
                    "Only PENDING orders can be confirmed. Current status: " + order.getStatus());
        }

        // Recompute remaining from paid
        double paidSoFar = paymentRepository.sumTotalPaidByOrderId(id) != null ? paymentRepository.sumTotalPaidByOrderId(id) : 0.0;
        double newRemaining = round((order.getTotalTTC() - paidSoFar) * 100.0) / 100.0;
        order.setMontantRestant(newRemaining);

        if (order.getMontantRestant() > 0.01) { // Small tolerance for floating point
            throw new BusinessRuleViolationException(
                    "Order must be fully paid before confirmation. Remaining amount: "
                            + order.getMontantRestant() + " DH");
        }

        order.setStatus(OrderStatus.CONFIRMED);

        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            int newStock = product.getStock() - item.getQuantite();
            product.setStock(newStock);
            productRepository.save(product);
        }

        // 6. Update client statistics
        Client client = order.getClient();

        // Increment total orders
        client.setTotalOrders(client.getTotalOrders() + 1);

        // Add to total spent (round to 2 decimals)
        double newTotalSpent = client.getTotalSpent() + order.getTotalTTC();
        client.setTotalSpent(Math.round(newTotalSpent * 100.0) / 100.0);

        // Update first order date if this is the first order
        if (client.getFirstOrderDate() == null) {
            client.setFirstOrderDate(order.getCreatedAt());
        }

        client.setLastOrderDate(order.getCreatedAt());

        // CRITICAL: Recalculate client tier
        updateClientTier(client);

        clientRepository.save(client);
        Order confirmedOrder = orderRepository.save(order);

        return convertToResponseDTO(confirmedOrder);
    }

    @Override
    public OrderResponseDTO cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with ID: " + id));

        if (!order.getStatus().canBeCanceled()) {
            throw new BusinessRuleViolationException(
                    "Only PENDING orders can be canceled. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELED);

        Order canceledOrder = orderRepository.save(order);

        return convertToResponseDTO(canceledOrder);
    }

    @Override
    public OrderResponseDTO rejectOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with ID: " + id));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessRuleViolationException(
                    "Only PENDING orders can be rejected. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.REJECTED);

        Order rejectedOrder = orderRepository.save(order);

        return convertToResponseDTO(rejectedOrder);
    }

    // ========== HELPER METHODS ==========

    // Validate promo code format: PROMO-XXXX

    private boolean isValidPromoCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        return code.matches("^PROMO-[A-Z0-9]{4}$");
    }

    //Convert Order entity to OrderResponseDTO

    private OrderResponseDTO convertToResponseDTO(Order order) {
        // Convert OrderItems to OrderItemResponseDTOs
        List<OrderItemResponseDTO> itemDTOs = order.getOrderItems().stream()
                .map(item -> OrderItemResponseDTO.builder()
                        .productId(item.getProduct().getId())
                        .nomDeProduit(item.getProduct().getNom())
                        .quantity(item.getQuantite())
                        .prixUnitaire(item.getPrixUnitaire())
                        .prixTotal(item.getTotalLigne())
                        .build())
                .collect(Collectors.toList());

        // Create OrderResponseDTO
        return OrderResponseDTO.builder()
                .id(order.getId())
                .clientId(order.getClient().getId())
                .clientName(order.getClient().getNom())
                .items(itemDTOs)
                .sousTotal(order.getSousTotal())
                .montantRemise(order.getMontantRemise())
                .montantHT(order.getMontantHT())
                .montantTVA(order.getMontantTVA())
                .montantTTC(order.getTotalTTC())
                .montantRestant(order.getMontantRestant())
                .CodePromo(order.getCodePromo())
                .promoApplied(order.getPromoApplied())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }


     // CRITICAL METHOD - Recalculate client tier based on orders and spending
     // Called after each order confirmation

    private void updateClientTier(Client client) {
        int totalOrders = client.getTotalOrders();
        double totalSpent = client.getTotalSpent();

        CustomerTier newTier;

        // Check from highest to lowest tier
        if (totalOrders >= 20 || totalSpent >= 15000) {
            newTier = CustomerTier.PLATINUM;
        } else if (totalOrders >= 10 || totalSpent >= 5000) {
            newTier = CustomerTier.GOLD;
        } else if (totalOrders >= 3 || totalSpent >= 1000) {
            newTier = CustomerTier.SILVER;
        } else {
            newTier = CustomerTier.BASIC;
        }

        // Update client tier
        client.setTier(newTier);
    }
}
