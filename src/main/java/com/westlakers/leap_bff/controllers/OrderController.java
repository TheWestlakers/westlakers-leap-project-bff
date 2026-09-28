package com.westlakers.leap_bff.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.westlakers.leap_bff.dtos.OrderDTO;
import com.westlakers.leap_bff.dtos.OrderProfileDTO;
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

    @GetMapping("/orders/{id}/profile")
    public OrderProfileDTO getOrderProfile(@PathVariable Long id) {
        return this.orderService.getOrderProfile(id);
    }

    @GetMapping("/accounts/{accountId}/orders")
    public List<OrderDTO> getOrdersByAccountId(@PathVariable Long accountId) {
        return this.orderService.getOrdersByAccountId(accountId);
    }

    @GetMapping("/orders/{id}/validate")
    public ResponseEntity<String> validateOrderExists(@PathVariable Long id) {
        try {
            this.orderService.getOrderById(id);
            return ResponseEntity.ok()
                .header("X-Order-Status", "VALID")
                .body("Order exists and is valid");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Order not found with id: " + id);
        }
    }

    @GetMapping("/orders/{id}/status")
    public ResponseEntity<String> checkOrderStatus(@PathVariable Long id) {
        try {
            OrderDTO order = this.orderService.getOrderById(id);
            return ResponseEntity.ok()
                .body("Order status: " + order.getStatusName());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body("Unable to retrieve order status: " + e.getMessage());
        }
    }
}
