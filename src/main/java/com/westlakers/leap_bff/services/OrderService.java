package com.westlakers.leap_bff.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.westlakers.leap_bff.mappers.OrderMapper;
import com.westlakers.leap_bff.mappers.OrderStatusMapper;
import com.westlakers.leap_bff.entities.Order;
import com.westlakers.leap_bff.entities.OrderStatus;
import com.westlakers.leap_bff.dtos.OrderDTO;
import com.westlakers.leap_bff.dtos.OrderProfileDTO;

@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderStatusMapper orderStatusMapper;

    public OrderService(OrderMapper orderMapper, OrderStatusMapper orderStatusMapper) {
        this.orderMapper = orderMapper;
        this.orderStatusMapper = orderStatusMapper;
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
    public OrderDTO createOrder(Order order) {
        // Validate required fields
        if(order.getAccountId() == null) {
            throw new RuntimeException("Account ID is required");
        }
        if(order.getInstrumentId() == null) {
            throw new RuntimeException("Instrument ID is required");
        }
        if(order.getSide() == null || order.getSide().isEmpty()) {
            throw new RuntimeException("Side is required (BUY or SELL)");
        }
        if(order.getOrderType() == null || order.getOrderType().isEmpty()) {
            throw new RuntimeException("Order Type is required");
        }
        if(order.getQuantity() == null) {
            throw new RuntimeException("Quantity is required");
        }
        if(order.getStatus() == null) {
            throw new RuntimeException("Status ID is required");
        }

        // Insert the order
        int result = this.orderMapper.insert(order);
        if(result == 0) {
            throw new RuntimeException("Failed to create order");
        }

        // Fetch and return the created order
        OrderStatus orderStatus = orderStatusMapper.findById(order.getStatus());
        return OrderDTO.fromEntity(order, orderStatus);
    }

    @Transactional
    public OrderDTO updateOrder(Long orderId, Order updatedOrder) {
        // Verify order exists
        Order existingOrder = this.orderMapper.findById(orderId);
        if(existingOrder == null) {
            throw new RuntimeException("Order not found with id: " + orderId);
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
}
