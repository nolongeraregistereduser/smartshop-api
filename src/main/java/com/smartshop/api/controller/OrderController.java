package com.smartshop.api.controller;

import com.smartshop.api.dto.request.OrderRequestDTO;
import com.smartshop.api.dto.response.OrderHistoryDTO;
import com.smartshop.api.dto.response.OrderResponseDTO;
import com.smartshop.api.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;


    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(
            @Valid @RequestBody OrderRequestDTO request) {

        OrderResponseDTO order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }


    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long id) {

        OrderResponseDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }


    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {

        List<OrderResponseDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }


    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<OrderHistoryDTO>> getClientOrderHistory(
            @PathVariable Long clientId) {

        List<OrderHistoryDTO> history = orderService.getClientOrderHistory(clientId);
        return ResponseEntity.ok(history);
    }


    @PutMapping("/{id}/confirm")
    public ResponseEntity<OrderResponseDTO> confirmOrder(@PathVariable Long id) {

        OrderResponseDTO order = orderService.confirmOrder(id);
        return ResponseEntity.ok(order);
    }


    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponseDTO> cancelOrder(@PathVariable Long id) {

        OrderResponseDTO order = orderService.cancelOrder(id);
        return ResponseEntity.ok(order);
    }


    @PutMapping("/{id}/reject")
    public ResponseEntity<OrderResponseDTO> rejectOrder(@PathVariable Long id) {

        OrderResponseDTO order = orderService.rejectOrder(id);
        return ResponseEntity.ok(order);
    }
}

