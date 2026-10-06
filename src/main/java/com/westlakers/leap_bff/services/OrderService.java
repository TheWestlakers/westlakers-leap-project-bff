package com.westlakers.leap_bff.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.westlakers.leap_bff.mappers.OrderMapper;
import com.westlakers.leap_bff.mappers.OrderStatusMapper;
import com.westlakers.leap_bff.mappers.AccountMapper;
import com.westlakers.leap_bff.mappers.InstrumentMapper;
import com.westlakers.leap_bff.mappers.HoldingMapper;
import com.westlakers.leap_bff.entities.Order;
import com.westlakers.leap_bff.entities.OrderStatus;
import com.westlakers.leap_bff.entities.Account;
import com.westlakers.leap_bff.entities.Instrument;
import com.westlakers.leap_bff.entities.Holding;
import com.westlakers.leap_bff.dtos.OrderDTO;
import com.westlakers.leap_bff.dtos.OrderProfileDTO;
import com.westlakers.leap_bff.dtos.MarketOrderRequest;
import com.westlakers.leap_bff.dtos.LimitOrderRequest;
import com.westlakers.leap_bff.dtos.TradeExecutionResponse;

@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderStatusMapper orderStatusMapper;
    private final AccountMapper accountMapper;
    private final InstrumentMapper instrumentMapper;
    private final HoldingService holdingService;
    private final HoldingMapper holdingMapper;

    public OrderService(OrderMapper orderMapper, OrderStatusMapper orderStatusMapper, 
                        AccountMapper accountMapper, InstrumentMapper instrumentMapper,
                        HoldingService holdingService, HoldingMapper holdingMapper) {
        this.orderMapper = orderMapper;
        this.orderStatusMapper = orderStatusMapper;
        this.accountMapper = accountMapper;
        this.instrumentMapper = instrumentMapper;
        this.holdingService = holdingService;
        this.holdingMapper = holdingMapper;
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        List<Order> orders = this.orderMapper.findAll();

        if(orders.size() == 0) {
            throw new RuntimeException("List was zero");
        }
        // Convert Order entities to DTOs, fetching status for each order
        return orders.stream()
                .map(order -> {
                    OrderStatus orderStatus = orderStatusMapper.findById(order.getStatus());
                    return OrderDTO.fromEntity(order, orderStatus);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long id) {
        Order order = this.orderMapper.findById(id);

        if(order == null) {
            throw new RuntimeException("Order not found with id: " + id);
        }

        OrderStatus orderStatus = orderStatusMapper.findById(order.getStatus());
        return OrderDTO.fromEntity(order, orderStatus);
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByAccountId(Long accountId) {
        List<Order> orders = this.orderMapper.findByAccountId(accountId);

        if(orders.size() == 0) {
            throw new RuntimeException("No orders found for account id: " + accountId);
        }
        // Convert Order entities to DTOs, fetching status for each order
        return orders.stream()
                .map(order -> {
                    OrderStatus orderStatus = orderStatusMapper.findById(order.getStatus());
                    return OrderDTO.fromEntity(order, orderStatus);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get complete order profile as a flattened DTO containing all order information
     * including order status details.
     */
    @Transactional(readOnly = true)
    public OrderProfileDTO getOrderProfile(Long orderId) {
        Order order = this.orderMapper.findById(orderId);

        if(order == null) {
            throw new RuntimeException("Order not found with id: " + orderId);
        }

        OrderStatus orderStatus = orderStatusMapper.findById(order.getStatus());
        if(orderStatus == null) {
            throw new RuntimeException("Order status not found for order id: " + orderId);
        }

        return OrderProfileDTO.fromEntities(order, orderStatus);
    }

    @Transactional
    public OrderDTO updateOrder(Long orderId, Order updatedOrder) {
        // Verify order exists
        Order existingOrder = this.orderMapper.findById(orderId);
        if(existingOrder == null) {
            throw new RuntimeException("Order not found with id: " + orderId);
        }

        // Prevent updates to orders that are already executed or cancelled
        if (existingOrder.getStatus() == 2L || existingOrder.getStatus() == 3L) {
            throw new RuntimeException(
                "Cannot update order with id: " + orderId + ". Order status is " + 
                (existingOrder.getStatus() == 2L ? "EXECUTED" : "CANCELLED")
            );
        }

        // Set the order ID to ensure we're updating the correct record
        updatedOrder.setOrderId(orderId);

        // Update the order
        int result = this.orderMapper.update(updatedOrder);
        if(result == 0) {
            throw new RuntimeException("Failed to update order with id: " + orderId);
        }

        // Fetch and return the updated order
        Order updated = this.orderMapper.findById(orderId);
        OrderStatus orderStatus = orderStatusMapper.findById(updated.getStatus());
        return OrderDTO.fromEntity(updated, orderStatus);
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        // Verify order exists
        Order order = this.orderMapper.findById(orderId);
        if(order == null) {
            throw new RuntimeException("Order not found with id: " + orderId);
        }

        // Delete the order
        int result = this.orderMapper.delete(orderId);
        if(result == 0) {
            throw new RuntimeException("Failed to delete order with id: " + orderId);
        }
    }

    @Transactional
    public TradeExecutionResponse createMarketTrade(MarketOrderRequest request) {
        // Validation
        validateMarketOrderRequest(request);
        
        // Fetch and validate account exists
        Account account = accountMapper.findById(request.getAccountId());
        if (account == null) {
            throw new RuntimeException("Account not found with id: " + request.getAccountId());
        }
        
        // Fetch and validate instrument exists
        Instrument instrument = instrumentMapper.findById(request.getInstrumentId());
        if (instrument == null) {
            throw new RuntimeException("Instrument not found with id: " + request.getInstrumentId());
        }
        
        // Validate account has sufficient funds based on estimated price
        validateAccountBalance(account, request.getSide(), request.getQuantity(), request.getEstimatedPrice());
        
        // For SELL orders, validate that account holds the instrument
        if ("SELL".equalsIgnoreCase(request.getSide())) {
            validateSellHoldings(account, request.getInstrumentId(), request.getQuantity());
        }
        
        // Create order entity
        Order order = new Order();
        order.setAccountId(request.getAccountId());
        order.setInstrumentId(request.getInstrumentId());
        order.setSide(request.getSide().toUpperCase());
        order.setOrderType("MARKET");
        order.setQuantity(request.getQuantity());
        order.setLimitPrice(null); // No limit price for market orders
        order.setStatus(1L); // PENDING status
        order.setPlacedAt(LocalDateTime.now());
        
        // Insert the order
        int result = orderMapper.insert(order);
        if (result == 0) {
            throw new RuntimeException("Failed to create market order");
        }
        
        // Build and return response
        return buildTradeExecutionResponse(order, "MARKET order created successfully. Awaiting execution.");
    }

    /**
     * Create a limit order.
     * 
     * Limit orders wait for execution until market price reaches the specified limit price with these characteristics:
     * - Execute only when market price reaches or passes limit price
     * - BUY orders: wait for price to drop to limit price or lower
     * - SELL orders: wait for price to rise to limit price or higher
     * - Status set to PENDING (waiting for price conditions)
     * - Provides price protection against unfavorable fills
     * 
     * @param request LimitOrderRequest containing account, instrument, side, quantity, and limit price
     * @return TradeExecutionResponse with order details
     * @throws RuntimeException if validation fails or order creation fails
     */
    @Transactional
    public TradeExecutionResponse createLimitTrade(LimitOrderRequest request) {
        // Validation
        validateLimitOrderRequest(request);
        
        // Fetch and validate account exists
        Account account = accountMapper.findById(request.getAccountId());
        if (account == null) {
            throw new RuntimeException("Account not found with id: " + request.getAccountId());
        }
        
        // Fetch and validate instrument exists
        Instrument instrument = instrumentMapper.findById(request.getInstrumentId());
        if (instrument == null) {
            throw new RuntimeException("Instrument not found with id: " + request.getInstrumentId());
        }
        
        // Validate limit price makes sense for the side
        validateLimitPriceForSide(request.getSide(), request.getLimitPrice());
        
        // Validate account has sufficient funds to cover the limit order cost
        validateAccountBalance(account, request.getSide(), request.getQuantity(), request.getLimitPrice());
        
        // For SELL orders, validate that account holds the instrument
        if ("SELL".equalsIgnoreCase(request.getSide())) {
            validateSellHoldings(account, request.getInstrumentId(), request.getQuantity());
        }
        
        // Create order entity
        Order order = new Order();
        order.setAccountId(request.getAccountId());
        order.setInstrumentId(request.getInstrumentId());
        order.setSide(request.getSide().toUpperCase());
        order.setOrderType("LIMIT");
        order.setQuantity(request.getQuantity());
        order.setLimitPrice(request.getLimitPrice());
        order.setStatus(1L); // PENDING status
        order.setPlacedAt(LocalDateTime.now());
        
        // Insert the order
        int result = orderMapper.insert(order);
        if (result == 0) {
            throw new RuntimeException("Failed to create limit order");
        }
        
        // Build and return response
        String message = String.format(
            "LIMIT order created successfully - waiting for %s price to reach %.2f",
            request.getSide().toUpperCase(),
            request.getLimitPrice()
        );
        
        TradeExecutionResponse response = buildTradeExecutionResponse(order, message);
        response.setLimitPrice(request.getLimitPrice());
        return response;
    }

    private void validateMarketOrderRequest(MarketOrderRequest request) {
        if (request.getAccountId() == null || request.getAccountId() <= 0) {
            throw new RuntimeException("Valid Account ID is required");
        }
        if (request.getInstrumentId() == null || request.getInstrumentId() <= 0) {
            throw new RuntimeException("Valid Instrument ID is required");
        }
        if (request.getSide() == null || request.getSide().trim().isEmpty()) {
            throw new RuntimeException("Side is required (BUY or SELL)");
        }
        if (!request.getSide().toUpperCase().matches("^(BUY|SELL)$")) {
            throw new RuntimeException("Side must be either BUY or SELL");
        }
        if (request.getQuantity() == null || request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }
    }

    private void validateLimitOrderRequest(LimitOrderRequest request) {
        if (request.getAccountId() == null || request.getAccountId() <= 0) {
            throw new RuntimeException("Valid Account ID is required");
        }
        if (request.getInstrumentId() == null || request.getInstrumentId() <= 0) {
            throw new RuntimeException("Valid Instrument ID is required");
        }
        if (request.getSide() == null || request.getSide().trim().isEmpty()) {
            throw new RuntimeException("Side is required (BUY or SELL)");
        }
        if (!request.getSide().toUpperCase().matches("^(BUY|SELL)$")) {
            throw new RuntimeException("Side must be either BUY or SELL");
        }
        if (request.getQuantity() == null || request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }
        if (request.getLimitPrice() == null || request.getLimitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Limit price must be greater than 0");
        }
    }

    private void validateAccountBalance(Account account, String side, BigDecimal quantity, BigDecimal pricePerUnit) {
        // Validate account is active
        if (account == null) {
            throw new RuntimeException("Account validation failed: account is null");
        }
        
        String sideUpper = side.toUpperCase();
        
        if ("BUY".equals(sideUpper)) {
            // For BUY orders: Check if account has sufficient settled cash
            if (account.getSettledCash() == null || account.getSettledCash().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Account does not have sufficient settled cash to execute BUY order");
            }
            
            // Calculate and validate exact amount needed based on price per unit
            if (pricePerUnit != null) {
                BigDecimal totalCost = quantity.multiply(pricePerUnit);
                if (account.getSettledCash().compareTo(totalCost) < 0) {
                    throw new RuntimeException(
                        "Insufficient funds. Account has " + account.getSettledCash() + 
                        " but needs " + totalCost + " for this trade"
                    );
                }
            }
            
        } else if ("SELL".equals(sideUpper)) {
            // For SELL orders: Validate the quantity is positive (actual holdings validated separately)
            if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Quantity must be greater than 0");
            }
        }
    }

    private void validateLimitPriceForSide(String side, BigDecimal limitPrice) {
        if (limitPrice == null || limitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Limit price must be greater than 0");
        }
        
        // Ensure limit price is provided for limit orders
        String sideUpper = side.toUpperCase();
        if ("BUY".equals(sideUpper) || "SELL".equals(sideUpper)) {
            // BUY orders: Limit price represents max price willing to pay
            // SELL orders: Limit price represents min price willing to accept
            // Both are valid regardless of current market price
            // (Additional market price validation could be added if current price is available)
        }
    }

    private void validateSellHoldings(Account account, Long instrumentId, BigDecimal quantity) {
        Holding holding = holdingMapper.findByAccountAndInstrument(account.getAccountId(), instrumentId);
        
        if (holding == null) {
            throw new RuntimeException(
                "Account does not hold the specified instrument (Instrument ID: " + instrumentId + ")"
            );
        }
        
        if (holding.getQuantity() == null || holding.getQuantity().compareTo(quantity) < 0) {
            throw new RuntimeException(
                "Insufficient holdings. Account has " + (holding.getQuantity() != null ? holding.getQuantity() : "0") + 
                " shares but trying to sell " + quantity
            );
        }
    }

    private TradeExecutionResponse buildTradeExecutionResponse(Order order, String message) {
        OrderStatus orderStatus = orderStatusMapper.findById(order.getStatus());
        
        return TradeExecutionResponse.builder()
                .orderId(order.getOrderId())
                .accountId(order.getAccountId())
                .instrumentId(order.getInstrumentId())
                .side(order.getSide())
                .orderType(order.getOrderType())
                .quantity(order.getQuantity())
                .executionPrice(null) // Will be set when order executes
                .limitPrice(order.getLimitPrice())
                .totalValue(null) // Will be calculated when order executes
                .status(orderStatus != null ? orderStatus.getStatusName() : "UNKNOWN")
                .placedAt(order.getPlacedAt())
                .executedAt(order.getExecutedAt())
                .message(message)
                .build();
    }

    @Transactional
    public TradeExecutionResponse executeTrade(Long orderId, BigDecimal executionPrice) {
        // Fetch the order
        Order order = this.orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found with id: " + orderId);
        }
        
        // Validate order is in PENDING state (1L) before execution
        if (order.getStatus() != 1L) {
            String statusName = order.getStatus() == 2L ? "EXECUTED" : 
                               order.getStatus() == 3L ? "CANCELLED" : "UNKNOWN";
            throw new RuntimeException(
                "Cannot execute order with id: " + orderId + ". Order status is " + statusName + 
                ". Only PENDING orders can be executed."
            );
        }
        
        // Validate execution price
        if (executionPrice == null || executionPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Execution price must be greater than 0");
        }
        
        // For BUY orders, validate account still has sufficient funds at execution time
        if ("BUY".equalsIgnoreCase(order.getSide())) {
            Account account = accountMapper.findById(order.getAccountId());
            if (account == null) {
                throw new RuntimeException("Account not found with id: " + order.getAccountId());
            }
            
            BigDecimal totalCost = order.getQuantity().multiply(executionPrice);
            if (account.getSettledCash() == null || account.getSettledCash().compareTo(totalCost) < 0) {
                throw new RuntimeException(
                    "Insufficient funds at execution. Account has " + account.getSettledCash() + 
                    " but execution requires " + totalCost
                );
            }
        }
        
        // For SELL orders, validate account still holds the required shares at execution time
        if ("SELL".equalsIgnoreCase(order.getSide())) {
            validateSellHoldings(accountMapper.findById(order.getAccountId()), order.getInstrumentId(), order.getQuantity());
        }
        
        // Update order to EXECUTED status
        order.setStatus(2L); // EXECUTED status
        order.setExecutedAt(LocalDateTime.now());
        
        int result = this.orderMapper.update(order);
        if (result == 0) {
            throw new RuntimeException("Failed to execute order with id: " + orderId);
        }
        
        // Update holdings through upsert operation
        this.holdingService.upsertHoldingOnTrade(
            order.getAccountId(),
            order.getInstrumentId(),
            order.getQuantity(),
            executionPrice,
            order.getSide()
        );
        
        // Update account settled cash
        BigDecimal totalValue = order.getQuantity().multiply(executionPrice);
        Account account = accountMapper.findById(order.getAccountId());
        if (account != null) {
            if ("BUY".equalsIgnoreCase(order.getSide())) {
                // Decrease settled cash
                account.setSettledCash(account.getSettledCash().subtract(totalValue));
            } else if ("SELL".equalsIgnoreCase(order.getSide())) {
                // Increase settled cash
                account.setSettledCash(account.getSettledCash().add(totalValue));
            }
            accountMapper.update(account);
        }
        
        // Build and return response
        String message = String.format(
            "Trade executed successfully - %s %s shares of instrument %d at %.2f",
            order.getSide(),
            order.getQuantity(),
            order.getInstrumentId(),
            executionPrice
        );
        
        TradeExecutionResponse response = buildTradeExecutionResponse(order, message);
        response.setExecutionPrice(executionPrice);
        response.setTotalValue(totalValue);
        response.setStatus("EXECUTED");
        return response;
    }

    @Transactional
    public TradeExecutionResponse cancelTrade(Long orderId) {
        // Fetch the order
        Order order = this.orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found with id: " + orderId);
        }
        
        // Only PENDING orders can be cancelled
        if (order.getStatus() != 1L) {
            throw new RuntimeException("Only PENDING orders can be cancelled. Order status: " + order.getStatus());
        }
        
        // Update order to CANCELLED status
        order.setStatus(3L); // CANCELLED status
        order.setExecutedAt(LocalDateTime.now());
        
        int result = this.orderMapper.update(order);
        if (result == 0) {
            throw new RuntimeException("Failed to cancel order with id: " + orderId);
        }
        
        // Build and return response
        String message = "Trade cancelled successfully - no holdings were affected";
        TradeExecutionResponse response = buildTradeExecutionResponse(order, message);
        response.setStatus("CANCELLED");
        return response;
    }

}
