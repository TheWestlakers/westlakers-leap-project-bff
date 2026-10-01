package com.westlakers.leap_bff.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.westlakers.leap_bff.dtos.OrderDTO;
import com.westlakers.leap_bff.dtos.OrderProfileDTO;
import com.westlakers.leap_bff.entities.Order;
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
    public ResponseEntity<OrderDTO> validateOrderExists(@PathVariable Long id) {
        OrderDTO order = this.orderService.getOrderById(id);
        return ResponseEntity.ok()
            .header("X-Order-Status", "VALID")
            .body(order);
    }

    @GetMapping("/orders/{id}/status")
    public ResponseEntity<OrderDTO> checkOrderStatus(@PathVariable Long id) {
        OrderDTO order = this.orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/orders")
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody Order order) {
        OrderDTO createdOrder = this.orderService.createOrder(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @PatchMapping("/orders/{id}")
    public ResponseEntity<OrderDTO> updateOrder(@PathVariable Long id, @Valid @RequestBody Order order) {
        OrderDTO updatedOrder = this.orderService.updateOrder(id, order);
        return ResponseEntity.ok(updatedOrder);
    }

    @DeleteMapping("/orders/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        this.orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
