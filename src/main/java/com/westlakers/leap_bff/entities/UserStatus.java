package com.westlakers.leap_bff.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name="user_status")
@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor 
public class UserStatus {
    @Id 
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long userStatusId;

    @Column(name="status_name", length = 50, nullable = false, unique = true)
    private String statusName;

    @Column(name="description", length = 255)
    private String description;
}