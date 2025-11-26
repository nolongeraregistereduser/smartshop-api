package com.smartshop.api.service.Impl;

import com.smartshop.api.dto.request.LoginRequestDTO;
import com.smartshop.api.dto.response.LoginResponseDTO;
import com.smartshop.api.entity.User;
import com.smartshop.api.enums.UserRole;
import com.smartshop.api.repository.UserRepository;
import com.smartshop.api.service.AuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String SESSION_USER_ID = "SESSION_USER_ID";
    private static final String SESSION_USER_ROLE = "SESSION_USER_ROLE";

    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO, HttpSession session) {
        String email = loginRequestDTO.getEmail();
        String password = loginRequestDTO.getPassword();

        if (!userRepository.existsByEmail(email)) {
            return LoginResponseDTO.builder()
                    .message("Invalid email or password")
                    .build();
        }

        User user = userRepository.findByEmail(email).get();

        // Use BCrypt to verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return LoginResponseDTO.builder()
                    .message("Invalid email or password")
                    .build();
        }

        // Store user id and role in session
        session.setAttribute(SESSION_USER_ID, user.getId());
        session.setAttribute(SESSION_USER_ROLE, user.getRole());

        return LoginResponseDTO.builder()
                .userId(user.getId())
                .email(email)
                .role(user.getRole())
                .message("Login successful")
                .build();
    }


    @Override
    public boolean isAuthenticated(HttpSession session) {

        // check if user id and role are present in session
        if (session.getAttribute(SESSION_USER_ID) == null || session.getAttribute(SESSION_USER_ROLE) == null) {
            return false;
                      }
        return true;
                      }
    @Override
    public Long getAuthenticatedUserId(HttpSession session) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            return null;  }
        return userId;
    }

    @Override
    public UserRole getAuthenticatedUserRole(HttpSession session) {
        UserRole role = (UserRole) session.getAttribute(SESSION_USER_ROLE);
        if (role == null) {
            return null;
        }
        return role;
    }

    @Override
    public void logout(HttpSession session) {

        session.invalidate();

    }
}
