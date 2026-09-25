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
 * Lightweight DTO for Order list responses.
 * Contains essential order information with status details.
 * Used for getAllOrders() endpoint to avoid N+1 queries.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {
    private Long orderId;
    private Long accountId;
    private Long instrumentId;
    private String side;
    private String orderType;
    private BigDecimal limitPrice;
    private BigDecimal quantity;
    private LocalDateTime placedAt;
    private LocalDateTime executedAt;
    private String statusName;
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
                .statusId(order.getStatusId())
                .build();
    }
}
