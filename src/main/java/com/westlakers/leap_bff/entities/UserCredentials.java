package com.westlakers.leap_bff.entities;

import java.time.LocalDateTime;

import com.westlakers.leap_bff.entities.User;

import com.westlakers.leap_bff.entities.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name="user_credentials")
@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor 
public class UserCredentials {
    @Id 
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="credential_id")
    private Long credentialId;

    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="role_id", nullable =false)
    private Role role;

    @Column(name="email", nullable = false, unique = true)
    private String email;

    @Column(name="username", nullable = false, unique = true)
    private String username;

    @Column(name="password_hash", nullable = false)
    private String passwordHash;

    @Column(name="is_active")
    private boolean isActive;

    @Column(name="created_at")
    private LocalDateTime createdAt;
}