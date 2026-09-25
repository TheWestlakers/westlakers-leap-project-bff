package com.westlakers.leap_bff.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.westlakers.leap_bff.dtos.AccountDTO;
import com.westlakers.leap_bff.dtos.AccountProfileDTO;
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
}
