package com.westlakers.leap_bff.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.westlakers.leap_bff.dtos.UserDTO;
import com.westlakers.leap_bff.dtos.UserProfileDTO;

import com.westlakers.leap_bff.services.UserService;


@RestController 
@RequestMapping("/api")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public List<UserDTO> getAllUsers() {
        return this.userService.getAllUsers();
    }

    @GetMapping("/users/{id}")
    public UserDTO getUserById(@PathVariable Long id) {
        return this.userService.getUserById(id);
    }

    @GetMapping("/users/{id}/profile")
    public UserProfileDTO getUserProfile(@PathVariable Long id) {
        return this.userService.getUserProfile(id);
    }
}