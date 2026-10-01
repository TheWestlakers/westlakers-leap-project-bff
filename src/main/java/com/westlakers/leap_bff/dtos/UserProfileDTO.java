package com.westlakers.leap_bff.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.westlakers.leap_bff.entities.Role;
import com.westlakers.leap_bff.entities.User;
import com.westlakers.leap_bff.entities.UserCredentials;
import com.westlakers.leap_bff.entities.UserStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDTO {
    // User fields
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

    // UserStatus fields
    @NotBlank(message = "Status is required")
    private String status;
    
    @NotNull(message = "Status ID is required")
    @Positive(message = "Status ID must be positive")
    private Long statusId;

    // UserCredentials fields
    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    private String email;
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    private boolean isActive;
    
    @PastOrPresent(message = "Credentials created date must be in the past or present")
    private LocalDateTime credentialsCreatedAt;

    // Role fields
    @NotBlank(message = "Role is required")
    private String role;
    
    @NotNull(message = "Role ID is required")
    @Positive(message = "Role ID must be positive")
    private Long roleId;

    public static UserProfileDTO fromEntities(User user, UserCredentials credentials, UserStatus userStatus, Role role) {
        return UserProfileDTO.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .taxId(user.getTaxId())
                .dateOfBirth(user.getDateOfBirth())
                .createDate(user.getCreateDate())
                .status(userStatus != null ? userStatus.getStatusName() : null)
                .statusId(user.getStatusId())
                .email(credentials.getEmail())
                .username(credentials.getUsername())
                .isActive(credentials.isActive())
                .credentialsCreatedAt(credentials.getCreatedAt())
                .role(role.getRoleName())
                .roleId(role.getRoleId())
                .build();
    }
}
