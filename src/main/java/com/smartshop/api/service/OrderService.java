package com.smartshop.api.service;

import com.smartshop.api.dto.request.OrderRequestDTO;
import com.smartshop.api.dto.response.OrderHistoryDTO;
import com.smartshop.api.dto.response.OrderResponseDTO;

import java.util.List;

public interface OrderService {

    // create order

    OrderResponseDTO createOrder(OrderRequestDTO orderRequestDTO);

    OrderResponseDTO getOrderById(Long id);

    // get all orders for admin

    List<OrderResponseDTO> getAllOrders();

    // get client order history (for specific client)

    List<OrderHistoryDTO> getClientOrderHistory(Long clientId);

    // confirm order

    OrderResponseDTO confirmOrder(Long id);

    OrderResponseDTO cancelOrder(Long id);

    // automatic par systeme si quantity insuffisante

    OrderResponseDTO rejectOrder(Long id);
}
