package com.westlakers.leap_bff.controller;

import java.util.List;

import com.westlakers.leap_bff.dtos.AccountDTO;

public record CurrentUserResponse(
        Long userId,
        String email,
        String firstName,
        String lastName,
        Long roleId,
        List<AccountDTO> accounts) {
}