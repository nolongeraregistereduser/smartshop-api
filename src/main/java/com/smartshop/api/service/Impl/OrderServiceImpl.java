package com.smartshop.api.service.Impl;

import com.smartshop.api.dto.request.OrderRequestDTO;
import com.smartshop.api.dto.response.OrderResponseDTO;
import com.smartshop.api.repository.ClientRepository;
import com.smartshop.api.repository.OrderRepository;
import com.smartshop.api.repository.ProductRepository;
import com.smartshop.api.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Data
@Service
@RequiredArgsConstructor
@Transactional

public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    @Override
    public OrderResponseDTO createOrder(OrderRequestDTO orderRequestDTO) {
        return null;
    }

    @Override
    public OrderResponseDTO getOrderById(Long id) {
        return null;
    }

    @Override
    public List<OrderResponseDTO> getAllOrders() {
        return List.of();
    }

    @Override
    public OrderResponseDTO confirmOrder(Long id) {
        return null;
    }

    @Override
    public OrderResponseDTO cancelOrder(Long id) {
        return null;
    }

    @Override
    public OrderResponseDTO rejectOrder(Long id) {
        return null;
    }
}
