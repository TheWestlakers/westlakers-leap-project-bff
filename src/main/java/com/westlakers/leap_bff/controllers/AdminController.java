package com.westlakers.leap_bff.controllers;

import com.westlakers.leap_bff.dtos.AccountDTO;
import com.westlakers.leap_bff.dtos.UserDTO;
import com.westlakers.leap_bff.services.AccountService;
import com.westlakers.leap_bff.services.AdminAuthorizationService;
import com.westlakers.leap_bff.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AccountService accountService;
    private final AdminAuthorizationService adminAuthorizationService;
    private final UserService userService;

    public AdminController(AccountService accountService,
                           AdminAuthorizationService adminAuthorizationService,
                           UserService userService) {
        this.accountService = accountService;
        this.adminAuthorizationService = adminAuthorizationService;
        this.userService = userService;
    }

    @GetMapping("/users")
    public List<UserDTO> getUsers(Authentication authentication) {
        adminAuthorizationService.requireAdmin(authentication);
        return userService.getAllUsers();
    }

    @GetMapping("/accounts")
    public List<AccountDTO> getAccounts(Authentication authentication) {
        adminAuthorizationService.requireAdmin(authentication);
        return accountService.getAllAccounts();
    }

    @PatchMapping("/accounts/{accountId}/freeze")
    public AccountDTO freezeAccount(@PathVariable Long accountId, Authentication authentication) {
        adminAuthorizationService.requireAdmin(authentication);
        return accountService.setTradingFrozen(accountId, true);
    }

    @PatchMapping("/accounts/{accountId}/unfreeze")
    public AccountDTO unfreezeAccount(@PathVariable Long accountId, Authentication authentication) {
        adminAuthorizationService.requireAdmin(authentication);
        return accountService.setTradingFrozen(accountId, false);
    }
}
