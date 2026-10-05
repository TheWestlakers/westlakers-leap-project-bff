package com.westlakers.leap_bff.dtos;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}