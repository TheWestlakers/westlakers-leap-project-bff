package com.westlakers.leap_bff.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.westlakers.leap_bff.entities.User;

public interface UserRepository extends JpaRepository<User, Long>{

    
} 