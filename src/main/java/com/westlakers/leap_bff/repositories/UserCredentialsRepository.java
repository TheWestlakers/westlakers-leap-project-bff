package com.westlakers.leap_bff.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.westlakers.leap_bff.entities.UserCredentials;

import java.util.Optional;

public interface UserCredentialsRepository extends JpaRepository<UserCredentials, Long> {
    Optional<UserCredentials> findByUserUserId(Long userId);
    Optional<UserCredentials> findByEmail(String email);
    Optional<UserCredentials> findByUsername(String username);
}
