package com.westlakers.leap_bff.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.westlakers.leap_bff.dtos.LoginRequest;
import com.westlakers.leap_bff.entities.UserCredentials;
import com.westlakers.leap_bff.exceptions.ApiException;
import com.westlakers.leap_bff.exceptions.ErrorCode;
import com.westlakers.leap_bff.mappers.UserCredentialsMapper;
import com.westlakers.leap_bff.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    
    private final UserCredentialsMapper credentialsMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public String authenticate(LoginRequest request) {
        // Validate input
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new ApiException(ErrorCode.INVALID_INPUT, "Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new ApiException(ErrorCode.INVALID_INPUT, "Password is required");
        }

        // Find user by email
        UserCredentials credentials = credentialsMapper.findByEmail(request.getEmail());
        if (credentials == null) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS, "Invalid email or password");
        }

        // Check if user is active
        if (!credentials.isActive()) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "User account is inactive");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), credentials.getPasswordHash())) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS, "Invalid email or password");
        }

        // Generate and return JWT token
        return jwtService.generateToken(credentials.getEmail());
    }
}
