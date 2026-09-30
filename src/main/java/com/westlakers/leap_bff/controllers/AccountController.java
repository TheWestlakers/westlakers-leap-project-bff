package com.westlakers.leap_bff.controllers;

import java.util.List;

import javax.validation.Valid;
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

    @PostMapping("/accounts")
    public ResponseEntity<AccountDTO> createAccount(@Valid @RequestBody Account account) {
        AccountDTO createdAccount = this.accountService.createAccount(account);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
    }

    @PatchMapping("/accounts/{id}")
    public ResponseEntity<AccountDTO> updateAccount(@PathVariable Long id, @Valid @RequestBody Account account) {
        AccountDTO updatedAccount = this.accountService.updateAccount(id, account);
        return ResponseEntity.ok(updatedAccount);
    }

    @DeleteMapping("/accounts/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        this.accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }
}
