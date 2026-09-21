package com.westlakers.leap_bff.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.westlakers.leap_bff.entities.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(String roleName);
}
