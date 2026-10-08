package com.westlakers.leap_bff.services;

import com.westlakers.leap_bff.dtos.*;
import com.westlakers.leap_bff.entities.*;
import com.westlakers.leap_bff.mappers.*;
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

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private InstrumentMapper instrumentMapper;

    @Mock
    private HoldingService holdingService;

    @Mock
    private HoldingMapper holdingMapper;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;
    private OrderDTO testOrderDTO;
    private OrderStatus testStatus;
    private Account testAccount;
    private Instrument testInstrument;
    private Holding testHolding;
    private MarketOrderRequest testMarketOrderRequest;
    private LimitOrderRequest testLimitOrderRequest;
    private TradeExecutionResponse testTradeExecutionResponse;

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

        testAccount = new Account();
        testAccount.setAccountId(1L);
        testAccount.setSettledCash(new BigDecimal("50000.00"));

        testInstrument = new Instrument();
        testInstrument.setInstrumentId(1L);
        testInstrument.setMarketId(1L);
        testInstrument.setAssetClassId(1L);
        testInstrument.setTicker("AAPL");
        testInstrument.setName("Apple Inc.");

        testHolding = new Holding();
        testHolding.setHoldingId(1L);
        testHolding.setAccountId(1L);
        testHolding.setInstrumentId(1L);
        testHolding.setQuantity(new BigDecimal("100.00"));
        testHolding.setAveragePrice(new BigDecimal("150.00"));

        testMarketOrderRequest = new MarketOrderRequest();
        testMarketOrderRequest.setAccountId(1L);
        testMarketOrderRequest.setInstrumentId(1L);
        testMarketOrderRequest.setSide("BUY");
        testMarketOrderRequest.setQuantity(new BigDecimal("100"));
        testMarketOrderRequest.setEstimatedPrice(new BigDecimal("150.00"));

        testLimitOrderRequest = new LimitOrderRequest();
        testLimitOrderRequest.setAccountId(1L);
        testLimitOrderRequest.setInstrumentId(1L);
        testLimitOrderRequest.setSide("BUY");
        testLimitOrderRequest.setQuantity(new BigDecimal("100"));
        testLimitOrderRequest.setLimitPrice(new BigDecimal("140.00"));

        testTradeExecutionResponse = new TradeExecutionResponse();
        testTradeExecutionResponse.setOrderId(1L);
        testTradeExecutionResponse.setAccountId(1L);
        testTradeExecutionResponse.setInstrumentId(1L);
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

    // /*
    //  * ==================== NEW TESTS FOR createMarketTrade() ====================
    //  */

    
    @Test
    void testCreateMarketTrade_ValidBuyOrder_Success() {
        // Arrange
        when(accountMapper.findById(1L)).thenReturn(testAccount);
        when(instrumentMapper.findById(1L)).thenReturn(testInstrument);
        when(orderMapper.insert(any())).thenReturn(1);
        when(orderStatusMapper.findById(1L)).thenReturn(testStatus);

        // Act
        TradeExecutionResponse result = orderService.createMarketTrade(testMarketOrderRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getAccountId());
        assertEquals(1L, result.getInstrumentId());
        assertEquals("BUY", result.getSide());
        assertEquals("MARKET", result.getOrderType());
        verify(orderMapper, times(1)).insert(any());
    }

    @Test
    void testCreateMarketTrade_ValidSellOrder_Success() {
        // Arrange
        testMarketOrderRequest.setSide("SELL");
        when(accountMapper.findById(1L)).thenReturn(testAccount);
        when(instrumentMapper.findById(1L)).thenReturn(testInstrument);
        when(holdingMapper.findByAccountAndInstrument(1L, 1L)).thenReturn(testHolding);
        when(orderMapper.insert(any())).thenReturn(1);
        when(orderStatusMapper.findById(1L)).thenReturn(testStatus);

        // Act
        TradeExecutionResponse result = orderService.createMarketTrade(testMarketOrderRequest);

        // Assert
        assertNotNull(result);
        assertEquals("SELL", result.getSide());
        verify(orderMapper, times(1)).insert(any());
    }

    @Test
    void testCreateMarketTrade_AccountNotFound_ThrowsException() {
        // Arrange
        when(accountMapper.findById(1L)).thenReturn(null);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> orderService.createMarketTrade(testMarketOrderRequest));
        verify(orderMapper, never()).insert(any());
    }

    // @Test
    // void testCreateMarketTrade_InstrumentNotFound_ThrowsException() {
    //     // Arrange
    //     when(accountMapper.findById(1L)).thenReturn(testAccount);
    //     when(instrumentMapper.findById(1L)).thenReturn(null);

    //     // Act & Assert
    //     assertThrows(RuntimeException.class, () -> orderService.createMarketTrade(testMarketOrderRequest));
    //     verify(orderMapper, never()).insert(any());
    // }

    // @Test
    // void testCreateMarketTrade_InsufficientFunds_ThrowsException() {
    //     // Arrange
    //     testAccount.setSettledCash(new BigDecimal("1000.00")); // Less than required
    //     when(accountMapper.findById(1L)).thenReturn(testAccount);
    //     when(instrumentMapper.findById(1L)).thenReturn(testInstrument);

    //     // Act & Assert
    //     assertThrows(RuntimeException.class, () -> orderService.createMarketTrade(testMarketOrderRequest));
    //     verify(orderMapper, never()).insert(any());
    // }

    // @Test
    // void testCreateMarketTrade_SellWithoutHoldings_ThrowsException() {
    //     // Arrange
    //     testMarketOrderRequest.setSide("SELL");
    //     when(accountMapper.findById(1L)).thenReturn(testAccount);
    //     when(instrumentMapper.findById(1L)).thenReturn(testInstrument);
    //     when(holdingMapper.findByAccountAndInstrument(1L, 1L)).thenReturn(null);

    //     // Act & Assert
    //     assertThrows(RuntimeException.class, () -> orderService.createMarketTrade(testMarketOrderRequest));
    //     verify(orderMapper, never()).insert(any());
    // }

    // /*
    //  * ==================== NEW TESTS FOR createLimitTrade() ====================
    //  */

    // @Test
    // void testCreateLimitTrade_ValidBuyOrder_Success() {
    //     // Arrange
    //     when(accountMapper.findById(1L)).thenReturn(testAccount);
    //     when(instrumentMapper.findById(1L)).thenReturn(testInstrument);
    //     when(orderMapper.insert(any())).thenReturn(1);
    //     when(orderStatusMapper.findById(1L)).thenReturn(testStatus);

    //     // Act
    //     TradeExecutionResponse result = orderService.createLimitTrade(testLimitOrderRequest);

    //     // Assert
    //     assertNotNull(result);
    //     assertEquals(1L, result.getAccountId());
    //     assertEquals(1L, result.getInstrumentId());
    //     assertEquals("BUY", result.getSide());
    //     assertEquals("LIMIT", result.getOrderType());
    //     assertEquals(new BigDecimal("140.00"), result.getLimitPrice());
    //     verify(orderMapper, times(1)).insert(any());
    // }

    // @Test
    // void testCreateLimitTrade_ValidSellOrder_Success() {
    //     // Arrange
    //     testLimitOrderRequest.setSide("SELL");
    //     testLimitOrderRequest.setLimitPrice(new BigDecimal("160.00"));
    //     when(accountMapper.findById(1L)).thenReturn(testAccount);
    //     when(instrumentMapper.findById(1L)).thenReturn(testInstrument);
    //     when(holdingMapper.findByAccountAndInstrument(1L, 1L)).thenReturn(testHolding);
    //     when(orderMapper.insert(any())).thenReturn(1);
    //     when(orderStatusMapper.findById(1L)).thenReturn(testStatus);

    //     // Act
    //     TradeExecutionResponse result = orderService.createLimitTrade(testLimitOrderRequest);

    //     // Assert
    //     assertNotNull(result);
    //     assertEquals("SELL", result.getSide());
    //     assertEquals("LIMIT", result.getOrderType());
    //     verify(orderMapper, times(1)).insert(any());
    // }

    // @Test
    // void testCreateLimitTrade_AccountNotFound_ThrowsException() {
    //     // Arrange
    //     when(accountMapper.findById(1L)).thenReturn(null);

    //     // Act & Assert
    //     assertThrows(RuntimeException.class, () -> orderService.createLimitTrade(testLimitOrderRequest));
    //     verify(orderMapper, never()).insert(any());
    // }

    // @Test
    // void testCreateLimitTrade_InstrumentNotFound_ThrowsException() {
    //     // Arrange
    //     when(accountMapper.findById(1L)).thenReturn(testAccount);
    //     when(instrumentMapper.findById(1L)).thenReturn(null);

    //     // Act & Assert
    //     assertThrows(RuntimeException.class, () -> orderService.createLimitTrade(testLimitOrderRequest));
    //     verify(orderMapper, never()).insert(any());
    // }

    // @Test
    // void testCreateLimitTrade_InvalidLimitPrice_ThrowsException() {
    //     // Arrange
    //     testLimitOrderRequest.setLimitPrice(new BigDecimal("0"));
    //     when(accountMapper.findById(1L)).thenReturn(testAccount);
    //     when(instrumentMapper.findById(1L)).thenReturn(testInstrument);

    //     // Act & Assert
    //     assertThrows(RuntimeException.class, () -> orderService.createLimitTrade(testLimitOrderRequest));
    //     verify(orderMapper, never()).insert(any());
    // }

    // @Test
    // void testCreateLimitTrade_InsufficientFunds_ThrowsException() {
    //     // Arrange
    //     testAccount.setSettledCash(new BigDecimal("500.00")); // Less than required for limit order
    //     when(accountMapper.findById(1L)).thenReturn(testAccount);
    //     when(instrumentMapper.findById(1L)).thenReturn(testInstrument);

    //     // Act & Assert
    //     assertThrows(RuntimeException.class, () -> orderService.createLimitTrade(testLimitOrderRequest));
    //     verify(orderMapper, never()).insert(any());
    // }

    // /*
    //  * ==================== NEW TESTS FOR executeTrade() ====================
    //  */

    // @Test
    // void testExecuteTrade_BuyOrder_Success() {
    //     // Arrange
    //     BigDecimal executionPrice = new BigDecimal("145.00");
    //     when(orderMapper.findById(1L)).thenReturn(testOrder);
    //     when(accountMapper.findById(1L)).thenReturn(testAccount);
    //     when(orderMapper.update(any())).thenReturn(1);
    //     when(orderStatusMapper.findById(2L)).thenReturn(new OrderStatus()); // EXECUTED status
    //     doNothing().when(holdingService).upsertHoldingOnTrade(any(), any(), any(), any(), any());

    //     // Act
    //     TradeExecutionResponse result = orderService.executeTrade(1L, executionPrice);

    //     // Assert
    //     assertNotNull(result);
    //     assertEquals(1L, result.getOrderId());
    //     assertEquals("EXECUTED", result.getStatus());
    //     assertEquals(executionPrice, result.getExecutionPrice());
    //     verify(orderMapper, times(1)).update(any());
    //     verify(holdingService, times(1)).upsertHoldingOnTrade(1L, 1L, new BigDecimal("100"), executionPrice, "BUY");
    // }

    // @Test
    // void testExecuteTrade_SellOrder_Success() {
    //     // Arrange
    //     testOrder.setSide("SELL");
    //     BigDecimal executionPrice = new BigDecimal("155.00");
    //     when(orderMapper.findById(1L)).thenReturn(testOrder);
    //     when(accountMapper.findById(1L)).thenReturn(testAccount);
    //     when(holdingMapper.findByAccountAndInstrument(1L, 1L)).thenReturn(testHolding);
    //     when(orderMapper.update(any())).thenReturn(1);
    //     when(orderStatusMapper.findById(2L)).thenReturn(new OrderStatus()); // EXECUTED status
    //     doNothing().when(holdingService).upsertHoldingOnTrade(any(), any(), any(), any(), any());

    //     // Act
    //     TradeExecutionResponse result = orderService.executeTrade(1L, executionPrice);

    //     // Assert
    //     assertNotNull(result);
    //     assertEquals("SELL", result.getSide());
    //     assertEquals("EXECUTED", result.getStatus());
    //     verify(orderMapper, times(1)).update(any());
    // }

    // @Test
    // void testExecuteTrade_OrderNotFound_ThrowsException() {
    //     // Arrange
    //     when(orderMapper.findById(999L)).thenReturn(null);

    //     // Act & Assert
    //     assertThrows(RuntimeException.class, () -> orderService.executeTrade(999L, new BigDecimal("150.00")));
    //     verify(orderMapper, never()).update(any());
    // }

    // @Test
    // void testExecuteTrade_NotPendingOrder_ThrowsException() {
    //     // Arrange
    //     testOrder.setStatus(2L); // EXECUTED status
    //     when(orderMapper.findById(1L)).thenReturn(testOrder);

    //     // Act & Assert
    //     assertThrows(RuntimeException.class, () -> orderService.executeTrade(1L, new BigDecimal("150.00")));
    //     verify(orderMapper, never()).update(any());
    // }

    // @Test
    // void testExecuteTrade_InvalidExecutionPrice_ThrowsException() {
    //     // Arrange
    //     when(orderMapper.findById(1L)).thenReturn(testOrder);

    //     // Act & Assert
    //     assertThrows(RuntimeException.class, () -> orderService.executeTrade(1L, new BigDecimal("0")));
    //     verify(orderMapper, never()).update(any());
    // }

    // @Test
    // void testExecuteTrade_BuyOrderInsufficientFunds_ThrowsException() {
    //     // Arrange
    //     testAccount.setSettledCash(new BigDecimal("100.00")); // Insufficient for 100 shares at 150
    //     when(orderMapper.findById(1L)).thenReturn(testOrder);
    //     when(accountMapper.findById(1L)).thenReturn(testAccount);

    //     // Act & Assert
    //     assertThrows(RuntimeException.class, () -> orderService.executeTrade(1L, new BigDecimal("150.00")));
    //     verify(orderMapper, never()).update(any());
    // }

    // @Test
    // void testExecuteTrade_SellOrderNoHoldings_ThrowsException() {
    //     // Arrange
    //     testOrder.setSide("SELL");
    //     when(orderMapper.findById(1L)).thenReturn(testOrder);
    //     when(accountMapper.findById(1L)).thenReturn(testAccount);
    //     when(holdingMapper.findByAccountAndInstrument(1L, 1L)).thenReturn(null);

    //     // Act & Assert
    //     assertThrows(RuntimeException.class, () -> orderService.executeTrade(1L, new BigDecimal("150.00")));
    //     verify(orderMapper, never()).update(any());
    // }

    // /*
    //  * ==================== NEW TESTS FOR cancelTrade() ====================
    //  */

    // @Test
    // void testCancelTrade_PendingOrder_Success() {
    //     // Arrange
    //     testOrder.setStatus(1L); // PENDING
    //     when(orderMapper.findById(1L)).thenReturn(testOrder);
    //     when(orderMapper.update(any())).thenReturn(1);
    //     when(orderStatusMapper.findById(3L)).thenReturn(new OrderStatus()); // CANCELLED status

    //     // Act
    //     TradeExecutionResponse result = orderService.cancelTrade(1L);

    //     // Assert
    //     assertNotNull(result);
    //     assertEquals(1L, result.getOrderId());
    //     assertEquals("CANCELLED", result.getStatus());
    //     verify(orderMapper, times(1)).update(any());
    // }

    // @Test
    // void testCancelTrade_OrderNotFound_ThrowsException() {
    //     // Arrange
    //     when(orderMapper.findById(999L)).thenReturn(null);

    //     // Act & Assert
    //     assertThrows(RuntimeException.class, () -> orderService.cancelTrade(999L));
    //     verify(orderMapper, never()).update(any());
    // }

    // @Test
    // void testCancelTrade_ExecutedOrder_ThrowsException() {
    //     // Arrange
    //     testOrder.setStatus(2L); // EXECUTED
    //     when(orderMapper.findById(1L)).thenReturn(testOrder);

    //     // Act & Assert
    //     assertThrows(RuntimeException.class, () -> orderService.cancelTrade(1L));
    //     verify(orderMapper, never()).update(any());
    // }

    // @Test
    // void testCancelTrade_AlreadyCancelledOrder_ThrowsException() {
    //     // Arrange
    //     testOrder.setStatus(3L); // CANCELLED
    //     when(orderMapper.findById(1L)).thenReturn(testOrder);

    //     // Act & Assert
    //     assertThrows(RuntimeException.class, () -> orderService.cancelTrade(1L));
    //     verify(orderMapper, never()).update(any());
    // }
}
