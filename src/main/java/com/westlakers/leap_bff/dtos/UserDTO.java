package com.westlakers.leap_bff.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.westlakers.leap_bff.entities.User;

/**
 * Lightweight DTO for User list responses.
 * Contains only essential user information with status details.
 * Used for getAllUsers() endpoint to avoid lazy-loading issues.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long userId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String taxId;
    private LocalDate dateOfBirth;
    private LocalDateTime createDate;
    private String statusName;
    private Long statusId;

    /**
     * Factory method to convert User entity to UserDTO.
     * Safely accesses the lazy-loaded status within the session context.
     */
    public static UserDTO fromEntity(User user) {
        return UserDTO.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .taxId(user.getTaxId())
                .dateOfBirth(user.getDateOfBirth())
                .createDate(user.getCreateDate())
                .statusName(user.getStatus() != null ? user.getStatus().getStatusName() : null)
                .statusId(user.getStatus() != null ? user.getStatus().getUserStatusId() : null)
                .build();
    }
}
