package com.westlakers.leap_bff.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.westlakers.leap_bff.entities.User;
import com.westlakers.leap_bff.entities.UserStatus;

/**
 * Lightweight DTO for User list responses.
 * Contains only essential user information with status details.
 * Used for getAllUsers() endpoint to avoid N+1 queries.
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
     * Note: statusName must be fetched separately using StatusMapper.
     */
    public static UserDTO fromEntity(User user, UserStatus userStatus) {
        return UserDTO.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .taxId(user.getTaxId())
                .dateOfBirth(user.getDateOfBirth())
                .createDate(user.getCreateDate())
                .statusName(userStatus != null ? userStatus.getStatusName() : null)
                .statusId(user.getStatusId())
                .build();
    }
}
