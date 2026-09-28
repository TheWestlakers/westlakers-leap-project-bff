package com.westlakers.leap_bff.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.westlakers.leap_bff.entities.Account;
import com.westlakers.leap_bff.entities.AccountStatus;
import com.westlakers.leap_bff.entities.AccountType;

/**
 * DTO that flattens Account, AccountStatus, and AccountType into a single
 * response object. This allows API endpoints to return the complete account profile
 * in a single object, matching the domain model diagram expectations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountProfileDTO {
    // Account fields
    private Long accountId;
    private Long userId;
    private LocalDateTime createdAt;
    private BigDecimal settledCash;

    // AccountStatus fields
    private String status;
    private Long statusId;

    // AccountType fields
    private String accountType;
    private Long accountTypeId;

    /**
     * Convenience factory method to build an AccountProfileDTO from related entities.
     * This demonstrates the flattening of separated entities for API responses.
     */
    public static AccountProfileDTO fromEntities(Account account, AccountStatus accountStatus, AccountType accountType) {
        return AccountProfileDTO.builder()
                .accountId(account.getAccountId())
                .userId(account.getUserId())
                .createdAt(account.getCreatedAt())
                .settledCash(account.getSettledCash())
                .status(accountStatus != null ? accountStatus.getStatusName() : null)
                .statusId(account.getAccountStatusId())
                .accountType(accountType != null ? accountType.getTypeName() : null)
                .accountTypeId(account.getAccountTypeId())
                .build();
    }
}
