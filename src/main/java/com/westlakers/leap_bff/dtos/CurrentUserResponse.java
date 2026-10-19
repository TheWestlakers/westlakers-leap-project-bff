package com.westlakers.leap_bff.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Profile of the authenticated user, resolved from the JWT subject.
 * Lets the client discover its own userId and accounts without hardcoding them.
 */
@Data
@AllArgsConstructor
public class CurrentUserResponse {
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private List<AccountDTO> accounts;
}
