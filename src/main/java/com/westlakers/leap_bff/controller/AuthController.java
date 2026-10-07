package com.westlakers.leap_bff.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import com.westlakers.leap_bff.services.AuthenticationService;
import com.westlakers.leap_bff.security.JwtService;
import com.westlakers.leap_bff.dtos.LoginRequest;
import com.westlakers.leap_bff.dtos.LoginResponse;
import com.westlakers.leap_bff.dtos.RegisterRequest;
import com.westlakers.leap_bff.dtos.RegisterResponse;



@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationService authenticationService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        String token = authenticationService.authenticate(request);
        return new LoginResponse(token);
    }

    @PostMapping("/register")
    public RegisterResponse register(@RequestBody RegisterRequest request) {
        return authenticationService.register(request);
    }

    @GetMapping("/validate")
    public boolean validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }
        String token = authHeader.substring(7);
        return jwtService.validateToken(token);
    }
}


