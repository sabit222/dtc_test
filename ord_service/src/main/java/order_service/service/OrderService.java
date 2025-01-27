package order_service.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import order_service.dto.UserDTO;
import order_service.model.Order;
import order_service.model.Status;
import order_service.repository.OrderRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;
    private final HttpServletRequest request;

    private static final String USER_SERVICE_URL = "http://localhost:8081/users";

    @Cacheable(value = "orders", key = "#status + #minPrice + #maxPrice")
    public List<Order> getOrders(Status status, BigDecimal minPrice, BigDecimal maxPrice) {
        String jwtToken = getJwtToken();
        List<String> roles = getRolesFromToken(jwtToken);

        // Только администратор может получить все заказы
        if (!roles.contains("ROLE_ADMIN")) {
            throw new RuntimeException("Access denied. Only admins can view all orders.");
        }

        if (status != null) {
            return orderRepository.findOrdersByStatusAndPriceRange(status, minPrice, maxPrice);
        } else {
            return orderRepository.findOrdersByPriceRange(minPrice, maxPrice);
        }
    }

    public Order getOrderById(Long orderId) {
        // 1. Находим заказ
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        // 2. Извлекаем токен из заголовка Authorization
        String jwtToken = getJwtToken();

        // 3. Извлекаем роли из токена
        List<String> roles = getRolesFromToken(jwtToken);

        // 4. Если у пользователя нет роли ADMIN, сверяем firstname владельца заказа
        if (!roles.contains("ROLE_ADMIN")) {
            // Получаем firstname из токена
            String currentFirstname = getFirstnameFromToken(jwtToken);

            // Сравниваем с полем firstname в заказе
            if (!Objects.equals(order.getCustomerName(), currentFirstname)) {
                throw new RuntimeException("Access denied: not your order");
            }
        }

        return order;
    }

    private String getFirstnameFromToken(String jwtToken) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Decoders.BASE64.decode("404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"))
                .build()
                .parseClaimsJws(jwtToken)
                .getBody();

        return claims.get("firstname", String.class);
    }

    @Transactional
    public Order createOrder(Order order, String firstname) {
        String jwtToken = getJwtToken();
        List<String> roles = getRolesFromToken(jwtToken);

        // Только пользователь или администратор может создавать заказы
        if (!roles.contains("ROLE_USER") && !roles.contains("ROLE_ADMIN")) {
            throw new RuntimeException("Access denied. Only users or admins can create orders.");
        }

        UserDTO userDTO = getUserByFirstname(firstname);
        order.setCustomerName(userDTO.getFirstname());

        return orderRepository.save(order);
    }

    @Transactional
    public Order updateOrder(Long orderId, Order updatedOrder, String firstname) {
        String jwtToken = getJwtToken();
        List<String> roles = getRolesFromToken(jwtToken);

        // Только пользователь может обновлять свои заказы
        Order existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!roles.contains("ROLE_ADMIN") && !existingOrder.getCustomerName().equals(getUsernameFromToken(jwtToken))) {
            throw new RuntimeException("Access denied. Only admins or order owners can update this order.");
        }

        UserDTO userDTO = getUserByFirstname(firstname);
        existingOrder.setCustomerName(userDTO.getFirstname());
        existingOrder.setStatus(updatedOrder.getStatus());
        existingOrder.setProducts(updatedOrder.getProducts());
        existingOrder.setTotalPrice(updatedOrder.getTotalPrice());

        return orderRepository.save(existingOrder);
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        String jwtToken = getJwtToken();
        List<String> roles = getRolesFromToken(jwtToken);

        // Только администратор может удалять заказы
        if (!roles.contains("ROLE_ADMIN")) {
            throw new RuntimeException("Access denied. Only admins can delete orders.");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setDeleted(true);
        orderRepository.save(order);
    }

    private UserDTO getUserByFirstname(String firstname) {
        String url = USER_SERVICE_URL + "/firstname/{firstname}";
        String jwtToken = getJwtToken();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<UserDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    UserDTO.class,
                    firstname
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new RuntimeException("User not found for username: " + firstname);
            }
            throw new RuntimeException("Failed to fetch user: " + e.getMessage(), e);
        }
    }

    private String getJwtToken() {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("JWT token not found in Authorization header");
    }

    private List<String> getRolesFromToken(String jwtToken) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Decoders.BASE64.decode("404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"))
                .build()
                .parseClaimsJws(jwtToken)
                .getBody();

        Object roles = claims.get("roles");
        if (roles instanceof List) {
            return ((List<?>) roles).stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private String getUsernameFromToken(String token) {
        // Предположим, что sub хранит email/username
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Decoders.BASE64.decode("404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"))
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();

    }
}
