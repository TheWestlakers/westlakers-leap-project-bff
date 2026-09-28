package com.westlakers.leap_bff.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.westlakers.leap_bff.entities.Order;
import com.westlakers.leap_bff.entities.OrderStatus;

/**
 * DTO that flattens Order and OrderStatus into a single response object.
 * This allows API endpoints to return the complete order profile in a single object,
 * matching the domain model diagram expectations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderProfileDTO {
    // Order fields
    private Long orderId;
    private Long accountId;
    private Long instrumentId;
    private String side;
    private String orderType;
    private BigDecimal limitPrice;
    private BigDecimal quantity;
    private LocalDateTime placedAt;
    private LocalDateTime executedAt;

    // OrderStatus fields
    private String status;
    private Long statusId;

    /**
     * Convenience factory method to build an OrderProfileDTO from related entities.
     * This demonstrates the flattening of separated entities for API responses.
     */
    public static OrderProfileDTO fromEntities(Order order, OrderStatus orderStatus) {
        return OrderProfileDTO.builder()
                .orderId(order.getOrderId())
                .accountId(order.getAccountId())
                .instrumentId(order.getInstrumentId())
                .side(order.getSide())
                .orderType(order.getOrderType())
                .limitPrice(order.getLimitPrice())
                .quantity(order.getQuantity())
                .placedAt(order.getPlacedAt())
                .executedAt(order.getExecutedAt())
                .status(orderStatus != null ? orderStatus.getStatusName() : null)
                .statusId(order.getStatus())
                .build();
    }
}
