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

    @Transactional
    public AccountDTO createAccount(Account account) {
        // Validate required fields
        if(account.getUserId() == null) {
            throw new RuntimeException("User ID is required");
        }
        if(account.getAccountTypeId() == null) {
            throw new RuntimeException("Account Type ID is required");
        }
        if(account.getAccountStatusId() == null) {
            throw new RuntimeException("Account Status ID is required");
        }

        // Insert the account
        int result = this.accountMapper.insert(account);
        if(result == 0) {
            throw new RuntimeException("Failed to create account");
        }

        // Fetch and return the created account
        AccountStatus accountStatus = accountStatusMapper.findById(account.getAccountStatusId());
        return AccountDTO.fromEntity(account, accountStatus);
    }

    @Transactional
    public AccountDTO updateAccount(Long accountId, Account updatedAccount) {
        // Verify account exists
        Account existingAccount = this.accountMapper.findById(accountId);
        if(existingAccount == null) {
            throw new RuntimeException("Account not found with id: " + accountId);
        }

        // Set the account ID to ensure we're updating the correct record
        updatedAccount.setAccountId(accountId);

        // Update the account
        int result = this.accountMapper.update(updatedAccount);
        if(result == 0) {
            throw new RuntimeException("Failed to update account with id: " + accountId);
        }

        // Fetch and return the updated account
        Account updated = this.accountMapper.findById(accountId);
        AccountStatus accountStatus = accountStatusMapper.findById(updated.getAccountStatusId());
        return AccountDTO.fromEntity(updated, accountStatus);
    }

    @Transactional
    public void deleteAccount(Long accountId) {
        // Verify account exists
        Account account = this.accountMapper.findById(accountId);
        if(account == null) {
            throw new RuntimeException("Account not found with id: " + accountId);
        }

        // Delete the account
        int result = this.accountMapper.delete(accountId);
        if(result == 0) {
            throw new RuntimeException("Failed to delete account with id: " + accountId);
        }
    }
}
