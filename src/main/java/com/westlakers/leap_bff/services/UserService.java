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
import com.westlakers.leap_bff.exceptions.ApiException;
import com.westlakers.leap_bff.exceptions.ErrorCode;

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
        
        if(users.isEmpty()) {
            throw new ApiException(ErrorCode.EMPTY_RESULTS);
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
        if(id == null || id <= 0) {
            throw new ApiException(ErrorCode.INVALID_INPUT);
        }
        
        User user = this.userMapper.findById(id);

        if(user == null) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND, "User not found with id: " + id);
        }
        
        UserStatus userStatus = userStatusMapper.findById(user.getStatusId());
        return UserDTO.fromEntity(user, userStatus);
    }

    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfile(Long userId) {
        if(userId == null || userId <= 0) {
            throw new ApiException(ErrorCode.INVALID_INPUT);
        }
        
        User user = this.userMapper.findById(userId);
        
        if(user == null) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND, "User not found with id: " + userId);
        }
        
        UserCredentials credentials = credentialsMapper.findByUserId(userId);
        if(credentials == null) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Credentials not found for user id: " + userId);
        }
        
        Role role = roleMapper.findById(credentials.getRoleId());
        if(role == null) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Role not found with id: " + credentials.getRoleId());
        }
        
        UserStatus userStatus = userStatusMapper.findById(user.getStatusId());
        if(userStatus == null) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "User status not found with id: " + user.getStatusId());
        }
        
        return UserProfileDTO.fromEntities(user, credentials, userStatus, role);
    }

    @Transactional
    public UserDTO createUser(UserDTO userDTO) {        
        User user = new User();
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setTaxId(userDTO.getTaxId());
        user.setDateOfBirth(userDTO.getDateOfBirth());
        user.setStatusId(userDTO.getStatusId());
        
        int result = this.userMapper.insert(user);
        if(result == 0) {
            throw new ApiException(ErrorCode.CREATION_FAILED);
        }
        
        UserStatus userStatus = userStatusMapper.findById(user.getStatusId());
        return UserDTO.fromEntity(user, userStatus);
    }

    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        if(id == null || id <= 0) {
            throw new ApiException(ErrorCode.INVALID_INPUT);
        }
        
        User existingUser = this.userMapper.findById(id);
        
        if(existingUser == null) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND, "User not found with id: " + id);
        }
        
        existingUser.setFirstName(userDTO.getFirstName());
        existingUser.setLastName(userDTO.getLastName());
        existingUser.setPhoneNumber(userDTO.getPhoneNumber());
        existingUser.setTaxId(userDTO.getTaxId());
        existingUser.setDateOfBirth(userDTO.getDateOfBirth());
        existingUser.setStatusId(userDTO.getStatusId());
        
        int result = this.userMapper.update(existingUser);
        if(result == 0) {
            throw new ApiException(ErrorCode.UPDATE_FAILED);
        }
        
        UserStatus userStatus = userStatusMapper.findById(existingUser.getStatusId());
        return UserDTO.fromEntity(existingUser, userStatus);
    }

    @Transactional
    public void deleteUser(Long id) {
        if(id == null || id <= 0) {
            throw new ApiException(ErrorCode.INVALID_INPUT);
        }
        
        User user = this.userMapper.findById(id);
        
        if(user == null) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND, "User not found with id: " + id);
        }
        
        int result = this.userMapper.delete(id);
        if(result == 0) {
            throw new ApiException(ErrorCode.DELETE_FAILED);
        }
    }
}
