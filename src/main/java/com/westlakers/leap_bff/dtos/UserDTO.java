package com.westlakers.leap_bff.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.westlakers.leap_bff.entities.User;
import com.westlakers.leap_bff.entities.UserStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
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
    
    private String statusName;
    
    @NotNull(message = "Status ID is required")
    @Positive(message = "Status ID must be positive")
    private Long statusId;

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
