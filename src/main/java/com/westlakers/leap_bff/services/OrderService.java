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
                    OrderStatus orderStatus = orderStatusMapper.findById(order.getStatusId());
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

        OrderStatus orderStatus = orderStatusMapper.findById(order.getStatusId());
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
                    OrderStatus orderStatus = orderStatusMapper.findById(order.getStatusId());
                    return OrderDTO.fromEntity(order, orderStatus);
                })
                .collect(Collectors.toList());
    }
}
