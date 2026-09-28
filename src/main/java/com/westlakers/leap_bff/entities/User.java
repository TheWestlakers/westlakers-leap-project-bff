package com.westlakers.leap_bff.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class User {
    private Long userId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String taxId;
    private LocalDate dateOfBirth;
    private LocalDateTime createDate;
    private Long statusId;
}