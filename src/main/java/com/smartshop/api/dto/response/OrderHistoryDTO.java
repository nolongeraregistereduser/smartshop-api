package com.smartshop.api.dto.response;

import java.time.LocalDateTime;

public class OrderHistoryDTO {

    private Long id;
    private LocalDateTime createdAt;
    private Double montantTTC;
    private String status;
}
