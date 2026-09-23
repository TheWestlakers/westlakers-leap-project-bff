package com.westlakers.leap_bff.entities;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor 
public class UserCredentials {
    private Long credentialId;
    private Long userId;
    private Long roleId;
    private String email;
    private String username;
    private String passwordHash;
    private boolean isActive;
    private LocalDateTime createdAt;
}