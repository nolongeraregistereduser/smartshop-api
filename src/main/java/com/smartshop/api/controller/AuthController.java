package com.smartshop.api.controller;

import com.smartshop.api.dto.request.LoginRequestDTO;
import com.smartshop.api.dto.response.LoginResponseDTO;
import com.smartshop.api.enums.UserRole;
import com.smartshop.api.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO loginRequest,
            HttpSession session) {

        LoginResponseDTO response = authService.login(loginRequest, session);

        // Check if login was successful
        if (response.getUserId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        return ResponseEntity.ok(response);
    }


    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpSession session) {
        authService.logout(session);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout successful");

        return ResponseEntity.ok(response);
    }


    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> checkStatus(HttpSession session) {
        boolean isAuthenticated = authService.isAuthenticated(session);

        Map<String, Object> response = new HashMap<>();
        response.put("authenticated", isAuthenticated);

        if (isAuthenticated) {
            Long userId = authService.getAuthenticatedUserId(session);
            UserRole role = authService.getAuthenticatedUserRole(session);

            response.put("userId", userId);
            response.put("role", role);
        }

        return ResponseEntity.ok(response);
    }


    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpSession session) {
        if (!authService.isAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = authService.getAuthenticatedUserId(session);
        UserRole role = authService.getAuthenticatedUserRole(session);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("role", role);

        return ResponseEntity.ok(response);
    }
}

