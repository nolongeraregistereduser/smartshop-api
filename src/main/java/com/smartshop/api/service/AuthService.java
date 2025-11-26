package com.smartshop.api.service;

import com.smartshop.api.dto.request.LoginRequestDTO;
import com.smartshop.api.dto.response.LoginResponseDTO;
import com.smartshop.api.enums.UserRole;
import jakarta.servlet.http.HttpSession;

public interface AuthService {

    LoginResponseDTO login(LoginRequestDTO loginRequestDTO, HttpSession session);

    boolean isAuthenticated(HttpSession session);

    Long getAuthenticatedUserId(HttpSession session);

    UserRole getAuthenticatedUserRole(HttpSession session);

    void logout(HttpSession session);
}
