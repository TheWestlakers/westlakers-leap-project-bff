package com.westlakers.leap_bff.services;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.westlakers.leap_bff.entities.Order;
import com.westlakers.leap_bff.exceptions.ApiException;
import com.westlakers.leap_bff.exceptions.ErrorCode;
import com.westlakers.leap_bff.mappers.OrderMapper;

@Service
public class OrderRejectionService {

    private final OrderMapper orderMapper;

    public OrderRejectionService(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void reject(Order order) {
        order.setStatus(4L);
        order.setExecutedAt(LocalDateTime.now());
        if (orderMapper.update(order) == 0) {
            throw new ApiException(ErrorCode.OPERATION_FAILED, "Failed to reject order with id: " + order.getOrderId());
        }
    }
}