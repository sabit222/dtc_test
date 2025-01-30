package order_service.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import order_service.dto.UserDTO;
import order_service.log.LogService;
import order_service.model.Order;
import order_service.model.Status;
import order_service.repository.OrderRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;


import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;

    private final RestTemplate restTemplate;
    private final HttpServletRequest request;
    private final LogService logService;

    private static final String USER_SERVICE_URL = "http://host.docker.internal:8081/users";

    @Cacheable(value = "orders", key = "#status + #minPrice + #maxPrice")
    public List<Order> getOrders(Status status, BigDecimal minPrice, BigDecimal maxPrice) {
        log.info("Fetching orders with status: {}, minPrice: {}, maxPrice: {}", status, minPrice, maxPrice);

        String jwtToken = getJwtToken();
        List<String> roles = getRolesFromToken(jwtToken);

        if (!roles.contains("ROLE_ADMIN")) {
            log.warn("Access denied. User does not have ADMIN role.");
            throw new RuntimeException("Access denied. Only admins can view all orders.");
        }

        List<Order> orders = (status != null) ?
                orderRepository.findOrdersByStatusAndPriceRange(status, minPrice, maxPrice) :
                orderRepository.findOrdersByPriceRange(minPrice, maxPrice);

        log.info("Found {} orders", orders.size());
        return orders;
    }


    public Order getOrderById(Long orderId) {
        log.info("Fetching order with ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Order not found with ID: {}", orderId);
                    return new RuntimeException("Order not found with ID: " + orderId);
                });

        String jwtToken = getJwtToken();
        List<String> roles = getRolesFromToken(jwtToken);

        if (!roles.contains("ROLE_ADMIN")) {
            String currentFirstname = getFirstnameFromToken(jwtToken);
            log.info("Checking access for user: {}", currentFirstname);

            if (!Objects.equals(order.getCustomerName(), currentFirstname)) {
                log.warn("Access denied for user: {} on order ID: {}", currentFirstname, orderId);
                throw new RuntimeException("Access denied: not your order");
            }
        }

        log.info("Order with ID: {} fetched successfully", orderId);
        return order;
    }


    public String getFirstnameFromToken(String jwtToken) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Decoders.BASE64.decode("404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"))
                .build()
                .parseClaimsJws(jwtToken)
                .getBody();

        return claims.get("firstname", String.class);
    }

    @Transactional
    public Order createOrder(Order order, String firstname) {
        log.info("Creating order for user: {}", firstname);

        String jwtToken = getJwtToken();
        List<String> roles = getRolesFromToken(jwtToken);

        if (!roles.contains("ROLE_USER") && !roles.contains("ROLE_ADMIN")) {
            log.warn("Access denied. Only users or admins can create orders.");
            throw new RuntimeException("Access denied. Only users or admins can create orders.");
        }

        UserDTO userDTO = getUserByFirstname(firstname);
        order.setCustomerName(userDTO.getFirstname());
        Order savedOrder = orderRepository.save(order);

        log.info("Order created successfully with ID: {}", savedOrder.getOrderId());

        // Добавляем запись в таблицу логов
        logService.logAction(
                "CREATE_ORDER",
                "Order created with ID: " + savedOrder.getOrderId(),
                userDTO.getFirstname()
        );

        return savedOrder;
    }

    @Transactional
    public Order updateOrder(Long orderId, Order updatedOrder, String firstname) {
        log.info("Updating order with ID: {} for user: {}", orderId, firstname);

        // 1. Извлекаем токен и роли пользователя
        String jwtToken = getJwtToken();
        List<String> roles = getRolesFromToken(jwtToken);

        // 2. Находим существующий заказ
        Order existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Order not found with ID: {}", orderId);
                    return new RuntimeException("Order not found");
                });

        // 3. Проверяем, имеет ли пользователь права обновлять заказ
        if (!roles.contains("ROLE_ADMIN")) {
            String currentFirstname = getFirstnameFromToken(jwtToken);
            log.info("Checking access for user: {}", currentFirstname);

            // Проверяем, является ли пользователь владельцем заказа
            if (!Objects.equals(existingOrder.getCustomerName(), currentFirstname)) {
                log.warn("Access denied for user: {} on order ID: {}", currentFirstname, orderId);
                throw new RuntimeException("Access denied: not your order");
            }
        }

        // 4. Обновляем данные заказа
        log.info("Updating order with new data: {}", updatedOrder);
        existingOrder.setStatus(updatedOrder.getStatus());
        existingOrder.setProducts(updatedOrder.getProducts());
        existingOrder.setTotalPrice(updatedOrder.getTotalPrice());

        // Если обновление имени владельца разрешено
        UserDTO userDTO = getUserByFirstname(firstname);
        existingOrder.setCustomerName(userDTO.getFirstname());

        // 5. Сохраняем обновленный заказ
        Order savedOrder = orderRepository.save(existingOrder);
        log.info("Order with ID: {} updated successfully", savedOrder.getOrderId());

        // 6. Логируем операцию в базу данных
        logService.logAction(
                "UPDATE_ORDER",
                "Order updated with ID: " + savedOrder.getOrderId(),
                getFirstnameFromToken(jwtToken)
        );

        return savedOrder;
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        log.info("Deleting order with ID: {}", orderId);

        String jwtToken = getJwtToken();
        List<String> roles = getRolesFromToken(jwtToken);

        if (!roles.contains("ROLE_ADMIN")) {
            log.warn("Access denied. User does not have permission to delete order with ID: {}", orderId);
            throw new RuntimeException("Access denied. Only admins can delete orders.");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Order not found with ID: {}", orderId);
                    return new RuntimeException("Order not found");
                });

        order.setDeleted(true);
        orderRepository.save(order);

        log.info("Order with ID: {} deleted successfully", orderId);

        // Добавляем запись в таблицу логов
        logService.logAction(
                "DELETE_ORDER",
                "Order deleted with ID: " + orderId,
                getUsernameFromToken(jwtToken)
        );
    }


    public UserDTO getUserByFirstname(String firstname) {
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
    String getJwtToken() {
        String authHeader = request.getHeader("Authorization");

        log.info("Received Authorization header: {}", authHeader); // Логируем заголовок

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("JWT token is missing or invalid.");
        }

        String token = authHeader.substring(7); // Убираем "Bearer "

        if (!token.contains(".")) {
            throw new RuntimeException("Malformed JWT token: " + token);
        }

        return token;
    }


    public List<String> getRolesFromToken(String jwtToken) {
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
