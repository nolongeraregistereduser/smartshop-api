package com.smartshop.api.config;

import com.smartshop.api.entity.Client;
import com.smartshop.api.entity.Order;
import com.smartshop.api.enums.UserRole;
import com.smartshop.api.repository.ClientRepository;
import com.smartshop.api.repository.OrderRepository;
import com.smartshop.api.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SessionAuthInterceptor implements HandlerInterceptor {

    private final AuthService authService;
    private final ClientRepository clientRepository;
    private final OrderRepository orderRepository;

    private final AntPathMatcher matcher = new AntPathMatcher();

    private void deny(HttpServletResponse response, int status, String message) throws IOException {
        response.sendError(status, message);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        // Allow auth endpoints
        if (matcher.matchStart("/api/auth/**", uri)) {
            return true;
        }

        // Allow public product browsing (GET list/details)
        if (matcher.matchStart("/api/products/**", uri) && "GET".equalsIgnoreCase(method)) {
            return true;
        }

        // All other API endpoints require authentication
        if (!authService.isAuthenticated(request.getSession())) {
            deny(response, HttpServletResponse.SC_UNAUTHORIZED, "Not authenticated");
            return false;
        }

        UserRole role = authService.getAuthenticatedUserRole(request.getSession());
        Long userId = authService.getAuthenticatedUserId(request.getSession());

        // Admin can do everything
        if (role == UserRole.ADMIN) {
            return true;
        }

        // From here on, role is CLIENT (only allowed limited operations)
        // CLIENT can only read own client profile, stats and order history, and view products (handled above)

        // /api/clients/{id} -> GET own profile only
        if (matcher.match("/api/clients/{id}", uri)) {
            if (!"GET".equalsIgnoreCase(method)) {
                deny(response, HttpServletResponse.SC_FORBIDDEN, "Clients cannot modify other resources");
                return false;
            }
            Map<String, String> vars = matcher.extractUriTemplateVariables("/api/clients/{id}", uri);
            Long clientId = Long.parseLong(vars.get("id"));
            return checkClientOwnership(response, userId, clientId);
        }

        // /api/clients (list) -> forbidden for client
        if (matcher.match("/api/clients", uri)) {
            deny(response, HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return false;
        }

        // Orders
        // GET /api/orders/{id} -> client can view only own orders
        if (matcher.match("/api/orders/{id}", uri)) {
            if (!"GET".equalsIgnoreCase(method)) {
                deny(response, HttpServletResponse.SC_FORBIDDEN, "Clients cannot modify orders");
                return false;
            }
            Map<String, String> vars = matcher.extractUriTemplateVariables("/api/orders/{id}", uri);
            Long orderId = Long.parseLong(vars.get("id"));
            return checkOrderOwnership(response, userId, orderId);
        }

        // GET /api/orders/client/{clientId} -> client can view only if clientId belongs to them
        if (matcher.match("/api/orders/client/{clientId}", uri)) {
            Map<String, String> vars = matcher.extractUriTemplateVariables("/api/orders/client/{clientId}", uri);
            Long clientId = Long.parseLong(vars.get("clientId"));
            return checkClientOwnership(response, userId, clientId);
        }

        // /api/orders or /api/orders/** other operations -> forbidden for client
        if (matcher.matchStart("/api/orders/**", uri)) {
            deny(response, HttpServletResponse.SC_FORBIDDEN, "Clients cannot create or manage orders");
            return false;
        }

        // Payments: GET allowed only if order belongs to client; creation and state changes forbidden
        if (matcher.match("/api/orders/{orderId}/payments", uri) && "GET".equalsIgnoreCase(method)) {
            Map<String, String> vars = matcher.extractUriTemplateVariables("/api/orders/{orderId}/payments", uri);
            Long orderId = Long.parseLong(vars.get("orderId"));
            return checkOrderOwnership(response, userId, orderId);
        }

        if (matcher.matchStart("/api/orders/{orderId}/payments/**", uri) || matcher.match("/api/orders/{orderId}/payments", uri)) {
            // any POST/PUT on payments is forbidden for clients
            deny(response, HttpServletResponse.SC_FORBIDDEN, "Clients cannot record or change payments");
            return false;
        }

        // Default: deny
        deny(response, HttpServletResponse.SC_FORBIDDEN, "Access denied");
        return false;
    }

    private boolean checkClientOwnership(HttpServletResponse response, Long userId, Long clientId) {
        return clientRepository.findById(clientId)
                .map(Client::getUser)
                .map(u -> u.getId().equals(userId))
                .orElseGet(() -> {
                    try {
                        deny(response, HttpServletResponse.SC_FORBIDDEN, "Access denied");
                    } catch (IOException e) {
                        // ignore
                    }
                    return false;
                });
    }

    private boolean checkOrderOwnership(HttpServletResponse response, Long userId, Long orderId) {
        return orderRepository.findById(orderId)
                .map(Order::getClient)
                .map(client -> client.getUser().getId().equals(userId))
                .orElseGet(() -> {
                    try {
                        deny(response, HttpServletResponse.SC_FORBIDDEN, "Access denied");
                    } catch (IOException e) {
                        // ignore
                    }
                    return false;
                });
    }
}
