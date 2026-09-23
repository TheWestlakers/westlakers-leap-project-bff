package com.westlakers.leap_bff.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.westlakers.leap_bff.entities.Role;
import com.westlakers.leap_bff.entities.User;
import com.westlakers.leap_bff.entities.UserCredentials;
import com.westlakers.leap_bff.entities.UserStatus;

/**
 * DTO that flattens User, UserCredentials, Role, and UserStatus into a single
 * response object. This allows API endpoints to return the complete user profile
 * in a single object, matching the domain model diagram expectations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDTO {
    // User fields
    private Long userId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String taxId;
    private LocalDate dateOfBirth;
    private LocalDateTime createDate;

    // UserStatus fields
    private String status;
    private Long statusId;

    // UserCredentials fields
    private String email;
    private String username;
    private boolean isActive;
    private LocalDateTime credentialsCreatedAt;

    // Role fields
    private String role;
    private Long roleId;

    /**
     * Convenience factory method to build a UserProfileDTO from related entities.
     * This demonstrates the flattening of separated entities for API responses.
     */
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
