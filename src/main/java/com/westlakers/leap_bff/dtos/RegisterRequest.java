package com.westlakers.leap_bff.dtos;

import lombok.Data;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RegisterRequest {
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @Null(message = "Username is not accepted; use email")
    private String username;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    private String taxId;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotNull(message = "Account type is required")
    @Positive(message = "Account type ID must be a positive number")
    private Long accountTypeId;

    @NotNull(message = "Starting settled cash is required")
    @DecimalMin(value = "0.00", message = "Starting settled cash cannot be negative")
    @Digits(integer = 15, fraction = 2, message = "Starting settled cash must have at most 2 decimal places")
    private BigDecimal settledCash;
}
