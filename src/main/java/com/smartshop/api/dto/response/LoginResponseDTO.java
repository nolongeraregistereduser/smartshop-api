package com.smartshop.api.dto.response;


import com.smartshop.api.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {


    private Long userId;
    private String email;
    private UserRole role;
    private String message;
}
