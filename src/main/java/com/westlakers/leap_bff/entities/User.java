package com.westlakers.leap_bff.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class User {
    @Positive(message = "User ID must be positive")
    private Long userId;
    
    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    private String lastName;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be 10-15 digits")
    private String phoneNumber;
    
    @NotBlank(message = "Tax ID is required")
    @Pattern(regexp = "^[0-9]{9}$", message = "Tax ID must be exactly 9 digits")
    private String taxId;
    
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
    
    @PastOrPresent(message = "Create date must be in the past or present")
    private LocalDateTime createDate;
    
    @NotNull(message = "Status ID is required")
    @Positive(message = "Status ID must be positive")
    private Long statusId;
}