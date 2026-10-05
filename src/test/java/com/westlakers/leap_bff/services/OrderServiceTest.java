package com.westlakers.leap_bff.services;

import com.westlakers.leap_bff.dtos.OrderDTO;
import com.westlakers.leap_bff.dtos.OrderProfileDTO;
import com.westlakers.leap_bff.entities.Order;
import com.westlakers.leap_bff.entities.OrderStatus;
import com.westlakers.leap_bff.mappers.OrderMapper;
import com.westlakers.leap_bff.mappers.OrderStatusMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderStatusMapper orderStatusMapper;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;
    private OrderDTO testOrderDTO;
    private OrderStatus testStatus;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup test data
        testStatus = new OrderStatus();
        testStatus.setOrderStatusId(1L);
        testStatus.setStatusName("PENDING");

        testOrder = new Order();
        testOrder.setOrderId(1L);
        testOrder.setAccountId(1L);
        testOrder.setInstrumentId(1L);
        testOrder.setSide("BUY");
        testOrder.setOrderType("MARKET");
        testOrder.setQuantity(new BigDecimal("100"));
        testOrder.setStatus(1L);
        testOrder.setPlacedAt(LocalDateTime.now());

        testOrderDTO = new OrderDTO();
        testOrderDTO.setOrderId(1L);
        testOrderDTO.setAccountId(1L);
        testOrderDTO.setInstrumentId(1L);
        testOrderDTO.setSide("BUY");
        testOrderDTO.setQuantity(new BigDecimal("100"));
    }

    @Test
    void testGetAllOrders_Success() {
        // Arrange
        List<Order> orders = Arrays.asList(testOrder);
        when(orderMapper.findAll()).thenReturn(orders);
        when(orderStatusMapper.findById(1L)).thenReturn(testStatus);

        // Act
        List<OrderDTO> result = orderService.getAllOrders();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderMapper, times(1)).findAll();
    }

    @Test
    void testGetAllOrders_EmptyList_ThrowsException() {
        // Arrange
        when(orderMapper.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> orderService.getAllOrders());
    }

    @Test
    void testGetOrderById_Success() {
        // Arrange
        when(orderMapper.findById(1L)).thenReturn(testOrder);
        when(orderStatusMapper.findById(1L)).thenReturn(testStatus);

        // Act
        OrderDTO result = orderService.getOrderById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getOrderId());
        assertEquals("BUY", result.getSide());
        verify(orderMapper, times(1)).findById(1L);
    }

    @Test
    void testGetOrderById_NotFound_ThrowsException() {
        // Arrange
        when(orderMapper.findById(999L)).thenReturn(null);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> orderService.getOrderById(999L));
    }

    @Test
    void testGetOrdersByAccountId_Success() {
        // Arrange
        List<Order> orders = Arrays.asList(testOrder);
        when(orderMapper.findByAccountId(1L)).thenReturn(orders);
        when(orderStatusMapper.findById(1L)).thenReturn(testStatus);

        // Act
        List<OrderDTO> result = orderService.getOrdersByAccountId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderMapper, times(1)).findByAccountId(1L);
    }

    @Test
    void testGetOrdersByAccountId_NoOrders_ThrowsException() {
        // Arrange
        when(orderMapper.findByAccountId(999L)).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> orderService.getOrdersByAccountId(999L));
    }

    @Test
    void testGetOrderProfile_Success() {
        // Arrange
        when(orderMapper.findById(1L)).thenReturn(testOrder);
        when(orderStatusMapper.findById(1L)).thenReturn(testStatus);

        // Act
        OrderProfileDTO result = orderService.getOrderProfile(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getOrderId());
        verify(orderMapper, times(1)).findById(1L);
    }

    @Test
    void testCreateOrder_ValidBuyOrder() {
        // Arrange
        when(orderMapper.insert(testOrder)).thenReturn(1);
        when(orderStatusMapper.findById(1L)).thenReturn(testStatus);

        // Act
        OrderDTO result = orderService.createOrder(testOrder);

        // Assert
        assertNotNull(result);
        verify(orderMapper, times(1)).insert(testOrder);
    }

    @Test
    void testCreateOrder_ValidSellOrder() {
        // Arrange
        testOrder.setSide("SELL");
        when(orderMapper.insert(testOrder)).thenReturn(1);
        when(orderStatusMapper.findById(1L)).thenReturn(testStatus);

        // Act
        OrderDTO result = orderService.createOrder(testOrder);

        // Assert
        assertNotNull(result);
        assertEquals("SELL", result.getSide());
        verify(orderMapper, times(1)).insert(testOrder);
    }

    @Test
    void testCreateOrder_InvalidQuantity_ThrowsException() {
        // Arrange
        testOrder.setQuantity(new BigDecimal("0"));
        // Set up mocks to succeed IF validation is skipped - test should still fail if validation doesn't run
        when(orderMapper.insert(testOrder)).thenReturn(1);
        when(orderStatusMapper.findById(1L)).thenReturn(testStatus);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> orderService.createOrder(testOrder));
    }

    @Test
    void testCreateOrder_InvalidSide_ThrowsException() {
        // Arrange
        testOrder.setSide("INVALID");
        // Set up mocks to succeed IF validation is skipped - test should still fail if validation doesn't run
        when(orderMapper.insert(testOrder)).thenReturn(1);
        when(orderStatusMapper.findById(1L)).thenReturn(testStatus);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> orderService.createOrder(testOrder));
    }

    @Test
    void testCreateOrder_NegativeQuantity_ThrowsException() {
        // Arrange
        testOrder.setQuantity(new BigDecimal("-100"));
        // Set up mocks to succeed IF validation is skipped - test should still fail if validation doesn't run
        when(orderMapper.insert(testOrder)).thenReturn(1);
        when(orderStatusMapper.findById(1L)).thenReturn(testStatus);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> orderService.createOrder(testOrder));
    }

    @Test
    void testUpdateOrder_Success() {
        // Arrange
        Order updatedOrder = new Order();
        updatedOrder.setQuantity(new BigDecimal("150"));
        updatedOrder.setSide("BUY");

        when(orderMapper.findById(1L)).thenReturn(testOrder);
        when(orderMapper.update(any())).thenReturn(1);
        when(orderStatusMapper.findById(1L)).thenReturn(testStatus);

        // Act
        OrderDTO result = orderService.updateOrder(1L, updatedOrder);

        // Assert
        assertNotNull(result);
        verify(orderMapper, times(1)).update(any());
    }

    @Test
    void testUpdateOrder_NotFound_ThrowsException() {
        // Arrange
        when(orderMapper.findById(999L)).thenReturn(null);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> orderService.updateOrder(999L, testOrder));
        verify(orderMapper, never()).update(any());
    }

    @Test
    void testDeleteOrder_Success() {
        // Arrange
        when(orderMapper.findById(1L)).thenReturn(testOrder);
        when(orderMapper.delete(1L)).thenReturn(1);

        // Act
        assertDoesNotThrow(() -> orderService.deleteOrder(1L));

        // Assert
        verify(orderMapper, times(1)).delete(1L);
    }

    @Test
    void testDeleteOrder_NotFound_ThrowsException() {
        // Arrange
        when(orderMapper.findById(999L)).thenReturn(null);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> orderService.deleteOrder(999L));
        verify(orderMapper, never()).delete(any());
    }
}
