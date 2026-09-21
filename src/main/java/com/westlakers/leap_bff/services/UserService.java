package com.westlakers.leap_bff.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.westlakers.leap_bff.repositories.UserRepository;
import com.westlakers.leap_bff.entities.User;
import com.westlakers.leap_bff.entities.UserCredentials;
import com.westlakers.leap_bff.entities.Role;
import com.westlakers.leap_bff.repositories.UserCredentialsRepository;
import com.westlakers.leap_bff.dtos.UserDTO;
import com.westlakers.leap_bff.dtos.UserProfileDTO;

@Service 
public class UserService {

    private final UserRepository userRepository;
    private final UserCredentialsRepository credentialsRepository;

    public UserService(UserRepository userRepository, UserCredentialsRepository credentialsRepository) {
        this.userRepository = userRepository;
        this.credentialsRepository = credentialsRepository;
    }


    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        List<User> users = this.userRepository.findAll();
        
        if(users.size() == 0) {
            throw new RuntimeException("List was zero");
        }
        // Convert User entities to DTOs
        return users.stream().map(UserDTO::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        Optional<User> result = this.userRepository.findById(id);

        if(!result.isPresent()) {
            throw new RuntimeException("User not found with id: " + id);
        }

        return UserDTO.fromEntity(result.get());
    }

    /**
     * Get complete user profile as a flattened DTO containing all user information
     * including credentials and role details.
     */
    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfile(Long userId) {
        Optional<User> userResult = this.userRepository.findById(userId);
        
        if(!userResult.isPresent()) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        
        Optional<UserCredentials> credResult = credentialsRepository.findByUserUserId(userId);
        if(!credResult.isPresent()) {
            throw new RuntimeException("Credentials not found for user id: " + userId);
        }
                
        User user = userResult.get();
        UserCredentials credentials = credResult.get();
        Role role = credentials.getRole();
        
        return UserProfileDTO.fromEntities(user, credentials, role);
    }
}
