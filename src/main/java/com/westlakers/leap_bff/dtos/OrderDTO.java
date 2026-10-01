package com.westlakers.leap_bff.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.westlakers.leap_bff.entities.Order;
import com.westlakers.leap_bff.entities.OrderStatus;

/**
 * Lightweight DTO for Order list responses.
 * Contains only essential order information with status details.
 * Used for getAllOrders() endpoint to avoid N+1 queries.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {
    @NotNull(message = "Order ID cannot be null")
    @Positive(message = "Order ID must be positive")
    private Long orderId;
    
    @NotNull(message = "Account ID cannot be null")
    @Positive(message = "Account ID must be positive")
    private Long accountId;
    
    @NotNull(message = "Instrument ID cannot be null")
    @Positive(message = "Instrument ID must be positive")
    private Long instrumentId;
    
    @NotBlank(message = "Side cannot be blank")
    private String side;
    
    @NotBlank(message = "Order type cannot be blank")
    private String orderType;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Limit price must be greater than 0")
    private BigDecimal limitPrice;
    
    @NotNull(message = "Quantity cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Quantity must be greater than 0")
    private BigDecimal quantity;
    
    private LocalDateTime placedAt;
    private LocalDateTime executedAt;
    
    @NotBlank(message = "Status name cannot be blank")
    private String statusName;
    
    @NotNull(message = "Status ID cannot be null")
    @Positive(message = "Status ID must be positive")
    private Long statusId;

    /**
     * Factory method to convert Order entity to OrderDTO.
     * Note: statusName must be fetched separately using OrderStatusMapper.
     */
    public static OrderDTO fromEntity(Order order, OrderStatus orderStatus) {
        return OrderDTO.builder()
                .orderId(order.getOrderId())
                .accountId(order.getAccountId())
                .instrumentId(order.getInstrumentId())
                .side(order.getSide())
                .orderType(order.getOrderType())
                .limitPrice(order.getLimitPrice())
                .quantity(order.getQuantity())
                .placedAt(order.getPlacedAt())
                .executedAt(order.getExecutedAt())
                .statusName(orderStatus != null ? orderStatus.getStatusName() : null)
                .statusId(order.getStatus())
                .build();
    }
}
