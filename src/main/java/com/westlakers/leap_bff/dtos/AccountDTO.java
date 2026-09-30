package com.westlakers.leap_bff.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.westlakers.leap_bff.entities.Account;
import com.westlakers.leap_bff.entities.AccountStatus;

/**
 * Lightweight DTO for Account list responses.
 * Contains only essential account information with status details.
 * Used for getAllAccounts() endpoint to avoid N+1 queries.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDTO {
    private Long accountId;
    
    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be a positive number")
    private Long userId;
    
    @NotNull(message = "Account type ID is required")
    @Positive(message = "Account type ID must be a positive number")
    private Long accountTypeId;
    
    @PastOrPresent(message = "Created date cannot be in the future")
    private LocalDateTime createdAt;
    
    @DecimalMin(value = "0.0", inclusive = true, message = "Settled cash cannot be negative")
    private BigDecimal settledCash;
    
    @NotBlank(message = "Status name is required")
    private String statusName;
    
    @Positive(message = "Status ID must be a positive number")
    private Long statusId;

    /**
     * Factory method to convert Account entity to AccountDTO.
     * Note: statusName must be fetched separately using AccountStatusMapper.
     */
    public static AccountDTO fromEntity(Account account, AccountStatus accountStatus) {
        return AccountDTO.builder()
                .accountId(account.getAccountId())
                .userId(account.getUserId())
                .accountTypeId(account.getAccountTypeId())
                .createdAt(account.getCreatedAt())
                .settledCash(account.getSettledCash())
                .statusName(accountStatus != null ? accountStatus.getStatusName() : null)
                .statusId(account.getAccountStatusId())
                .build();
    }
}
