package com.westlakers.leap_bff.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import jakarta.validation.Valid;
import com.westlakers.leap_bff.dtos.RegisterRequest;
import com.westlakers.leap_bff.dtos.RegisterResponse;
import com.westlakers.leap_bff.services.AuthService;
import com.westlakers.leap_bff.security.JwtService;
import com.westlakers.leap_bff.dtos.LoginRequest;
import com.westlakers.leap_bff.dtos.LoginResponse;
import com.westlakers.leap_bff.dtos.RefreshTokenRequest;
import com.westlakers.leap_bff.exceptions.ApiException;
import com.westlakers.leap_bff.exceptions.ErrorCode;
import com.westlakers.leap_bff.dtos.CurrentUserResponse;
import org.springframework.security.core.Authentication;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        String token = authService.authenticateAndIssueToken(request);
        return new LoginResponse(token, jwtService.generateRefreshToken(request.getEmail()));
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        if (!jwtService.validateRefreshToken(request.getRefreshToken())) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS);
        }
        String email = jwtService.getEmailFromToken(request.getRefreshToken());
        return new LoginResponse(jwtService.generateToken(email), request.getRefreshToken());
    }

    @PostMapping("/register")
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @GetMapping("/me")
    public CurrentUserResponse me(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        return authService.getCurrentUser(authentication.getName());
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
