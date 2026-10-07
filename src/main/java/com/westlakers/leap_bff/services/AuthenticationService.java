package com.westlakers.leap_bff.services;

import java.time.LocalDate;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.westlakers.leap_bff.dtos.LoginRequest;
import com.westlakers.leap_bff.dtos.RegisterRequest;
import com.westlakers.leap_bff.dtos.RegisterResponse;
import com.westlakers.leap_bff.entities.User;
import com.westlakers.leap_bff.entities.UserCredentials;
import com.westlakers.leap_bff.exceptions.ApiException;
import com.westlakers.leap_bff.exceptions.ErrorCode;
import com.westlakers.leap_bff.mappers.UserCredentialsMapper;
import com.westlakers.leap_bff.mappers.UserMapper;
import com.westlakers.leap_bff.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    
    private final UserCredentialsMapper credentialsMapper;
    private final UserMapper userMapper;
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

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        // Validate input
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new ApiException(ErrorCode.INVALID_INPUT, "Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new ApiException(ErrorCode.INVALID_INPUT, "Password is required");
        }
        if (request.getFirstName() == null || request.getFirstName().isEmpty()) {
            throw new ApiException(ErrorCode.INVALID_INPUT, "First name is required");
        }
        if (request.getLastName() == null || request.getLastName().isEmpty()) {
            throw new ApiException(ErrorCode.INVALID_INPUT, "Last name is required");
        }
        if (request.getPhoneNumber() == null || request.getPhoneNumber().isEmpty()) {
            throw new ApiException(ErrorCode.INVALID_INPUT, "Phone number is required");
        }

        // Check if email already exists
        UserCredentials existingCredentials = credentialsMapper.findByEmail(request.getEmail());
        if (existingCredentials != null) {
            throw new ApiException(ErrorCode.INVALID_INPUT, "Email already registered");
        }

        // Create new user
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setTaxId(request.getTaxId());
        user.setDateOfBirth(LocalDate.now().minusYears(30)); // Default to 30 years old
        user.setStatusId(1L); // Default to ONLINE status

        int userInsertResult = userMapper.insert(user);
        if (userInsertResult == 0) {
            throw new ApiException(ErrorCode.CREATION_FAILED, "Failed to create user");
        }

        // Create credentials with hashed password
        UserCredentials credentials = new UserCredentials();
        credentials.setUserId(user.getUserId());
        credentials.setEmail(request.getEmail());
        credentials.setUsername(request.getEmail()); // Use email as username
        credentials.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        credentials.setRoleId(2L); // Default to USER role
        credentials.setActive(true);

        int credentialsInsertResult = credentialsMapper.insert(credentials);
        if (credentialsInsertResult == 0) {
            throw new ApiException(ErrorCode.CREATION_FAILED, "Failed to create credentials");
        }

        return new RegisterResponse(request.getEmail(), "User registered successfully");
    }
}
