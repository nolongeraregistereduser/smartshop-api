package com.smartshop.api.repository;

import com.smartshop.api.entity.Client;
import com.smartshop.api.entity.Order;
import com.smartshop.api.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // find orders for a spesefic client

     List<Order> findByClientId(Long clientId);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByClientIdAndStatus(Long clientId, OrderStatus status);


    @Query("SELECT COUNT(o) FROM Order o WHERE o.client.id = :clientId AND o.status = 'CONFIRMED'")
    Long countConfirmedOrdersByClientId(@Param("clientId") Long clientId);


    @Query("SELECT COALESCE(SUM(o.totalTTC), 0.0) FROM Order o WHERE o.client.id = :clientId AND o.status = 'CONFIRMED'")
    Double sumTotalSpentByClientId(@Param("clientId") Long clientId);






}
