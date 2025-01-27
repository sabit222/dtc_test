package order_service.controller;


import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import order_service.model.Order;
import order_service.model.Role;
import order_service.model.Status;
import order_service.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Получить список заказов с фильтрацией.
     * Доступно для ADMIN.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('admin:read')")
    public ResponseEntity<List<Order>> getOrders(
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        List<Order> orders = orderService.getOrders(status, minPrice, maxPrice);
        return ResponseEntity.ok(orders);
    }

    /**
     * Получить заказ по ID.
     * Доступно для ADMIN и USER.
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('user:read')")
    public ResponseEntity<Order> getOrderById(@PathVariable Long orderId) {
        Order order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    /**
     * Создать новый заказ.
     * Доступно для USER.
     */
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasAuthority('user:create')")
    public ResponseEntity<Order> createOrder(
            @RequestBody Order order,
            @RequestParam String firstname) {
        Order createdOrder = orderService.createOrder(order, firstname);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    /**
     * Обновить существующий заказ.
     * Доступно для USER и ADMIN.
     */
    @PutMapping("/{orderId}")
    @PreAuthorize("hasRole('USER') or hasAuthority('user:update') or hasAuthority('admin:update')")
    public ResponseEntity<Order> updateOrder(
            @PathVariable Long orderId,
            @RequestBody Order updatedOrder,
            @RequestParam String firstname) {
        Order updated = orderService.updateOrder(orderId, updatedOrder, firstname);
        return ResponseEntity.ok(updated);
    }

    /**
     * Удалить заказ (мягкое удаление).
     * Доступно только для ADMIN.
     */
    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or hasAnyAuthority('admin:delete')")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }
}
