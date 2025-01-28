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

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "API для управления заказами") // Описание контроллера
public class OrderController {

    private final OrderService orderService;

    /**
     * Получить список заказов с фильтрацией.
     * Доступно для ADMIN.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('admin:read')")
    @Operation(summary = "Получить список заказов", description = "Фильтрация по статусу и цене доступна только ADMIN")
    public ResponseEntity<List<Order>> getOrders(
            @Parameter(description = "Статус заказа (например, CONFIRMED, PENDING)") @RequestParam(required = false) Status status,
            @Parameter(description = "Минимальная цена заказа") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Максимальная цена заказа") @RequestParam(required = false) BigDecimal maxPrice) {
        List<Order> orders = orderService.getOrders(status, minPrice, maxPrice);
        return ResponseEntity.ok(orders);
    }

    /**
     * Получить заказ по ID.
     * Доступно для ADMIN и USER.
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('user:read')")
    @Operation(summary = "Получить заказ по ID", description = "Доступно для ADMIN и владельца заказа (USER)")
    public ResponseEntity<Order> getOrderById(
            @Parameter(description = "ID заказа", required = true) @PathVariable Long orderId) {
        Order order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    /**
     * Создать новый заказ.
     * Доступно для USER.
     */
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasAuthority('user:create')")
    @Operation(summary = "Создать новый заказ", description = "Только пользователи могут создавать заказы")
    public ResponseEntity<Order> createOrder(
            @RequestBody Order order,
            @Parameter(description = "Имя пользователя", required = true) @RequestParam String firstname) {
        Order createdOrder = orderService.createOrder(order, firstname);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    /**
     * Обновить существующий заказ.
     * Доступно для USER и ADMIN.
     */
    @PutMapping("/{orderId}")
    @PreAuthorize("hasRole('USER') or hasAuthority('user:update') or hasAuthority('admin:update')")
    @Operation(summary = "Обновить заказ", description = "Доступно пользователю, создавшему заказ, или ADMIN")
    public ResponseEntity<Order> updateOrder(
            @Parameter(description = "ID заказа", required = true) @PathVariable Long orderId,
            @RequestBody Order updatedOrder,
            @Parameter(description = "Имя пользователя", required = true) @RequestParam String firstname) {
        Order updated = orderService.updateOrder(orderId, updatedOrder, firstname);
        return ResponseEntity.ok(updated);
    }

    /**
     * Удалить заказ (мягкое удаление).
     * Доступно только для ADMIN.
     */
    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or hasAnyAuthority('admin:delete')")
    @Operation(summary = "Удалить заказ", description = "Доступно только для ADMIN")
    public ResponseEntity<Void> deleteOrder(
            @Parameter(description = "ID заказа", required = true) @PathVariable Long orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }
}
