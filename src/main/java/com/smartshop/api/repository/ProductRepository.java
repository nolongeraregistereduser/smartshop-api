package com.smartshop.api.repository;

import com.smartshop.api.entity.Order;
import com.smartshop.api.entity.Product;
import com.smartshop.api.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find all orders for a specific client (for order history)
    List<Order> findByClientIdOrderByCreatedAtDesc(Long clientId);

    // Find orders by status
    List<Order> findByStatus(OrderStatus status);

    // Find orders by client and status
    List<Order> findByClientIdAndStatus(Long clientId, OrderStatus status);


    // Count confirmed orders for a client (for loyalty calculation)
    @Query("SELECT COUNT(o) FROM Order o WHERE o.client.id = :clientId AND o.status = 'CONFIRMED'")
    Long countConfirmedOrdersByClientId(@Param("clientId") Long clientId);

    // Calculate total spent by client (confirmed orders only)
    @Query("SELECT COALESCE(SUM(o.totalTTC), 0.0) FROM Order o WHERE o.client.id = :clientId AND o.status = 'CONFIRMED'")
    Double sumTotalSpentByClientId(@Param("clientId") Long clientId);

    // Find orders within date range
    List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}
