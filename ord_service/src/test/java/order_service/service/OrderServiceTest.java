package order_service.service;


import jakarta.servlet.http.HttpServletRequest;
import order_service.dto.UserDTO;
import order_service.log.LogService;
import order_service.model.Order;
import order_service.repository.OrderRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private OrderService orderService;

    @Mock
    private LogService logService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // ✅ Мокируем заголовок Authorization с правильным токеном
        when(request.getHeader("Authorization")).thenReturn("Bearer mock.jwt.token");

        // ✅ Правильный вариант - создаем Spy-объект
        orderService = Mockito.spy(new OrderService(orderRepository, restTemplate, request, logService));

        // ✅ Теперь можно мокировать конкретные методы через doReturn()
        doReturn(List.of("ROLE_USER")).when(orderService).getRolesFromToken(anyString());
        doReturn("Sabit").when(orderService).getFirstnameFromToken(anyString());

        // ✅ Мокируем ответ от UserService
        UserDTO mockUser = new UserDTO();
        mockUser.setFirstname("Sabit");

        ResponseEntity<UserDTO> mockResponse = ResponseEntity.ok(mockUser);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(UserDTO.class),
                anyString()
        )).thenReturn(mockResponse);
    }

    /**
     * ✅ Тест успешного создания заказа
     */
    @Test
    void testCreateOrder_Success() {
        Order newOrder = new Order();
        newOrder.setCustomerName("Sabit");

        Order savedOrder = new Order();
        savedOrder.setOrderId(200L);
        savedOrder.setCustomerName("Sabit");

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        Order result = orderService.createOrder(newOrder, "Sabit");

        assertNotNull(result);
        assertEquals(200L, result.getOrderId());
        assertEquals("Sabit", result.getCustomerName());

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    /**
     * ✅ Тест получения заказа по ID (успешный)
     */
    @Test
    void testGetOrderById_Success() {
        Order order = new Order();
        order.setOrderId(100L);
        order.setCustomerName("Sabit");

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        Order result = orderService.getOrderById(100L);

        assertNotNull(result);
        assertEquals("Sabit", result.getCustomerName());

        verify(orderRepository, times(1)).findById(100L);
    }

    /**
     * ❌ Тест получения заказа по ID (ошибка: заказ не найден)
     */
    @Test
    void testGetOrderById_NotFound() {
        when(orderRepository.findById(100L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> orderService.getOrderById(100L));

        assertEquals("Order not found with ID: 100", exception.getMessage());

        verify(orderRepository, times(1)).findById(100L);
    }

    /**
     * ✅ Тест обновления заказа
     */
    @Test
    void testUpdateOrder_Success() {
        Order existingOrder = new Order();
        existingOrder.setOrderId(100L);
        existingOrder.setCustomerName("Sabit");
        existingOrder.setTotalPrice(new BigDecimal("100.00"));

        Order updatedOrder = new Order();
        updatedOrder.setTotalPrice(new BigDecimal("200.00"));

        when(orderRepository.findById(100L)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(existingOrder);

        Order result = orderService.updateOrder(100L, updatedOrder, "Sabit");

        assertNotNull(result);
        assertEquals(new BigDecimal("200.00"), result.getTotalPrice());

        verify(orderRepository, times(1)).findById(100L);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    /**
     * ❌ Тест обновления заказа (ошибка: заказ не найден)
     */
    @Test
    void testUpdateOrder_NotFound() {
        when(orderRepository.findById(100L)).thenReturn(Optional.empty());

        Order updatedOrder = new Order();
        updatedOrder.setTotalPrice(new BigDecimal("200.00"));

        Exception exception = assertThrows(RuntimeException.class, () -> orderService.updateOrder(100L, updatedOrder, "Sabit"));

        assertEquals("Order not found", exception.getMessage());

        verify(orderRepository, times(1)).findById(100L);
    }

    /**
     * ✅ Тест удаления заказа (мягкое удаление)
     */
    @Test
    void testDeleteOrder_Success() {
        Order order = new Order();
        order.setOrderId(100L);
        order.setCustomerName("Sabit");
        order.setDeleted(false);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        orderService.deleteOrder(100L);

        assertTrue(order.isDeleted());

        verify(orderRepository, times(1)).findById(100L);
        verify(orderRepository, times(1)).save(order);
    }

    /**
     * ❌ Тест удаления заказа (ошибка: заказ не найден)
     */
    @Test
    void testDeleteOrder_NotFound() {
        when(orderRepository.findById(100L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> orderService.deleteOrder(100L));

        assertEquals("Order not found", exception.getMessage());

        verify(orderRepository, times(1)).findById(100L);
    }
}