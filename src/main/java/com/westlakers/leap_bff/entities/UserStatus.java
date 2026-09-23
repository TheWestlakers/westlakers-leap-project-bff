package com.westlakers.leap_bff.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor 
public class UserStatus {
    private Long userStatusId;
    private String statusName;
    private String description;
}