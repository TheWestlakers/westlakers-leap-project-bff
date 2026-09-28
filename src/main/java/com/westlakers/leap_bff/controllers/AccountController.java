package com.westlakers.leap_bff.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.westlakers.leap_bff.dtos.AccountDTO;
import com.westlakers.leap_bff.dtos.AccountProfileDTO;
import com.westlakers.leap_bff.entities.Account;
import com.westlakers.leap_bff.services.AccountService;


@RestController
@RequestMapping("/api")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/accounts")
    public List<AccountDTO> getAllAccounts() {
        return this.accountService.getAllAccounts();
    }

    @GetMapping("/accounts/{id}")
    public AccountDTO getAccountById(@PathVariable Long id) {
        return this.accountService.getAccountById(id);
    }

    @GetMapping("/accounts/{id}/profile")
    public AccountProfileDTO getAccountProfile(@PathVariable Long id) {
        return this.accountService.getAccountProfile(id);
    }

    @GetMapping("/users/{userId}/accounts")
    public List<AccountDTO> getAccountsByUserId(@PathVariable Long userId) {
        return this.accountService.getAccountsByUserId(userId);
    }

    @GetMapping("/accounts/{id}/validate")
    public ResponseEntity<String> validateAccountExists(@PathVariable Long id) {
        try {
            this.accountService.getAccountById(id);
            return ResponseEntity.ok()
                .header("X-Account-Status", "VALID")
                .body("Account exists and is valid");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Account not found with id: " + id);
        }
    }

    @GetMapping("/accounts/{id}/status")
    public ResponseEntity<String> checkAccountStatus(@PathVariable Long id) {
        try {
            AccountDTO account = this.accountService.getAccountById(id);
            return ResponseEntity.ok()
                .body("Account status: " + account.getStatusName());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body("Unable to retrieve account status: " + e.getMessage());
        }
    }

    @PostMapping("/accounts")
    public ResponseEntity<String> createAccount(@RequestBody Account account) {
        try {
            AccountDTO createdAccount = this.accountService.createAccount(account);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body("Account created successfully with ID: " + createdAccount.getAccountId());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body("Failed to create account: " + e.getMessage());
        }
    }

    @PatchMapping("/accounts/{id}")
    public ResponseEntity<String> updateAccount(@PathVariable Long id, @RequestBody Account account) {
        try {
            AccountDTO updatedAccount = this.accountService.updateAccount(id, account);
            return ResponseEntity.ok()
                .body("Account updated successfully with ID: " + updatedAccount.getAccountId());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body("Failed to update account: " + e.getMessage());
        }
    }

    @DeleteMapping("/accounts/{id}")
    public ResponseEntity<String> deleteAccount(@PathVariable Long id) {
        try {
            this.accountService.deleteAccount(id);
            return ResponseEntity.ok()
                .body("Account deleted successfully with ID: " + id);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body("Failed to delete account: " + e.getMessage());
        }
    }
}
