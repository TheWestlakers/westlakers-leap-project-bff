package com.westlakers.leap_bff.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.westlakers.leap_bff.dtos.OrderDTO;
import com.westlakers.leap_bff.services.OrderService;


@RestController
@RequestMapping("/api")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/orders")
    public List<OrderDTO> getAllOrders() {
        return this.orderService.getAllOrders();
    }

    @GetMapping("/orders/{id}")
    public OrderDTO getOrderById(@PathVariable Long id) {
        return this.orderService.getOrderById(id);
    }

    @GetMapping("/accounts/{accountId}/orders")
    public List<OrderDTO> getOrdersByAccountId(@PathVariable Long accountId) {
        return this.orderService.getOrdersByAccountId(accountId);
    }
}
