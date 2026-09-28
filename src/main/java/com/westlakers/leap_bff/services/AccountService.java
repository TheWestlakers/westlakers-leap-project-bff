package com.westlakers.leap_bff.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.westlakers.leap_bff.mappers.AccountMapper;
import com.westlakers.leap_bff.mappers.AccountStatusMapper;
import com.westlakers.leap_bff.mappers.AccountTypeMapper;
import com.westlakers.leap_bff.entities.Account;
import com.westlakers.leap_bff.entities.AccountStatus;
import com.westlakers.leap_bff.entities.AccountType;
import com.westlakers.leap_bff.dtos.AccountDTO;
import com.westlakers.leap_bff.dtos.AccountProfileDTO;

@Service
public class AccountService {

    private final AccountMapper accountMapper;
    private final AccountStatusMapper accountStatusMapper;
    private final AccountTypeMapper accountTypeMapper;

    public AccountService(AccountMapper accountMapper, AccountStatusMapper accountStatusMapper,
                         AccountTypeMapper accountTypeMapper) {
        this.accountMapper = accountMapper;
        this.accountStatusMapper = accountStatusMapper;
        this.accountTypeMapper = accountTypeMapper;
    }

    @Transactional(readOnly = true)
    public List<AccountDTO> getAllAccounts() {
        List<Account> accounts = this.accountMapper.findAll();

        if(accounts.size() == 0) {
            throw new RuntimeException("List was zero");
        }
        // Convert Account entities to DTOs, fetching status for each account
        return accounts.stream()
                .map(account -> {
                    AccountStatus accountStatus = accountStatusMapper.findById(account.getAccountStatusId());
                    return AccountDTO.fromEntity(account, accountStatus);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AccountDTO getAccountById(Long id) {
        Account account = this.accountMapper.findById(id);

        if(account == null) {
            throw new RuntimeException("Account not found with id: " + id);
        }

        AccountStatus accountStatus = accountStatusMapper.findById(account.getAccountStatusId());
        return AccountDTO.fromEntity(account, accountStatus);
    }

    @Transactional(readOnly = true)
    public List<AccountDTO> getAccountsByUserId(Long userId) {
        List<Account> accounts = this.accountMapper.findByUserId(userId);

        if(accounts.size() == 0) {
            throw new RuntimeException("No accounts found for user id: " + userId);
        }
        // Convert Account entities to DTOs, fetching status for each account
        return accounts.stream()
                .map(account -> {
                    AccountStatus accountStatus = accountStatusMapper.findById(account.getAccountStatusId());
                    return AccountDTO.fromEntity(account, accountStatus);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get complete account profile as a flattened DTO containing all account information
     * including account status and account type details.
     */
    @Transactional(readOnly = true)
    public AccountProfileDTO getAccountProfile(Long accountId) {
        Account account = this.accountMapper.findById(accountId);

        if(account == null) {
            throw new RuntimeException("Account not found with id: " + accountId);
        }

        AccountStatus accountStatus = accountStatusMapper.findById(account.getAccountStatusId());
        if(accountStatus == null) {
            throw new RuntimeException("Account status not found for account id: " + accountId);
        }

        AccountType accountType = accountTypeMapper.findById(account.getAccountTypeId());
        if(accountType == null) {
            throw new RuntimeException("Account type not found for account id: " + accountId);
        }

        return AccountProfileDTO.fromEntities(account, accountStatus, accountType);
    }
}
