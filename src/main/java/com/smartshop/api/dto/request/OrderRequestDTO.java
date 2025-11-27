package com.smartshop.api.dto.request;

import java.util.List;

public class OrderRequestDTO {

    private Long clientId;

    private List<OrderItemRequestDTO> items;

    private String codePromo;


}
