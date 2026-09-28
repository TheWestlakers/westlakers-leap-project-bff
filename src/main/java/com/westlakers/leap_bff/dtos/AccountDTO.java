package com.westlakers.leap_bff.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Long userId;
    private Long accountTypeId;
    private LocalDateTime createdAt;
    private BigDecimal settledCash;
    private String statusName;
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
