package com.westlakers.leap_bff.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.westlakers.leap_bff.mappers.UserMapper;
import com.westlakers.leap_bff.mappers.UserCredentialsMapper;
import com.westlakers.leap_bff.mappers.RoleMapper;
import com.westlakers.leap_bff.mappers.UserStatusMapper;
import com.westlakers.leap_bff.entities.User;
import com.westlakers.leap_bff.entities.UserCredentials;
import com.westlakers.leap_bff.entities.Role;
import com.westlakers.leap_bff.entities.UserStatus;
import com.westlakers.leap_bff.dtos.UserDTO;
import com.westlakers.leap_bff.dtos.UserProfileDTO;

@Service 
public class UserService {

    private final UserMapper userMapper;
    private final UserCredentialsMapper credentialsMapper;
    private final RoleMapper roleMapper;
    private final UserStatusMapper userStatusMapper;

    public UserService(UserMapper userMapper, UserCredentialsMapper credentialsMapper, 
                      RoleMapper roleMapper, UserStatusMapper userStatusMapper) {
        this.userMapper = userMapper;
        this.credentialsMapper = credentialsMapper;
        this.roleMapper = roleMapper;
        this.userStatusMapper = userStatusMapper;
    }


    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        List<User> users = this.userMapper.findAll();
        
        if(users.size() == 0) {
            throw new RuntimeException("List was zero");
        }
        // Convert User entities to DTOs, fetching status for each user
        return users.stream()
                .map(user -> {
                    UserStatus userStatus = userStatusMapper.findById(user.getStatusId());
                    return UserDTO.fromEntity(user, userStatus);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = this.userMapper.findById(id);

        if(user == null) {
            throw new RuntimeException("User not found with id: " + id);
        }
        
        UserStatus userStatus = userStatusMapper.findById(user.getStatusId());
        return UserDTO.fromEntity(user, userStatus);
    }

    /**
     * Get complete user profile as a flattened DTO containing all user information
     * including credentials and role details.
     */
    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfile(Long userId) {
        User user = this.userMapper.findById(userId);
        
        if(user == null) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        
        UserCredentials credentials = credentialsMapper.findByUserId(userId);
        if(credentials == null) {
            throw new RuntimeException("Credentials not found for user id: " + userId);
        }
        
        Role role = roleMapper.findById(credentials.getRoleId());
        UserStatus userStatus = userStatusMapper.findById(user.getStatusId());
        
        return UserProfileDTO.fromEntities(user, credentials, userStatus, role);
    }
}
